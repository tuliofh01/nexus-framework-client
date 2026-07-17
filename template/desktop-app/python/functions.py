"""Safe vectorized evaluation for user-entered equations.

Architecture:
  - C++ owns UI + ImPlot and only ships the raw expression + range + sample
    count through PythonEngine/pybind11.
  - This module normalizes the expression (regex / light string processing),
    then evaluates it with a NumPy-backed AST whitelist over
    ``np.linspace(xmin, xmax, samples)`` in one vectorized call.
  - Domain errors become per-sample NaN (curve gaps), never a hard failure.
"""

from __future__ import annotations

import ast
import re

import numpy as np


_BINARY = {
    ast.Add: np.add,
    ast.Sub: np.subtract,
    ast.Mult: np.multiply,
    ast.Div: np.divide,
    ast.Pow: np.power,
    ast.Mod: np.mod,
}
_UNARY = {ast.UAdd: np.positive, ast.USub: np.negative}

# Unary NumPy callables (exactly one argument).
_UNARY_FUNCTIONS = {
    "abs": np.abs,
    "acos": np.arccos,
    "asin": np.arcsin,
    "atan": np.arctan,
    "ceil": np.ceil,
    "cos": np.cos,
    "cosh": np.cosh,
    "exp": np.exp,
    "floor": np.floor,
    "log": np.log,
    "log10": np.log10,
    "log2": np.log2,
    "sin": np.sin,
    "sinh": np.sinh,
    "sqrt": np.sqrt,
    "tan": np.tan,
    "tanh": np.tanh,
}

# Multi-argument NumPy callables (exact arity).
_NARY_FUNCTIONS = {
    "atan2": (2, np.arctan2),
    "max": (2, np.maximum),
    "min": (2, np.minimum),
    "pow": (2, np.power),
}

_FUNCTIONS = {**_UNARY_FUNCTIONS, **{k: v[1] for k, v in _NARY_FUNCTIONS.items()}}
_CONSTANTS = {"e": float(np.e), "pi": float(np.pi)}
_MAX_EXPRESSION_LENGTH = 256
_MAX_AST_NODES = 128

# Longest-first so ``log10`` / ``log2`` win over ``log``.
_FUNC_NAMES = sorted(_FUNCTIONS.keys(), key=len, reverse=True)
_FUNC_ALT = "|".join(re.escape(name) for name in _FUNC_NAMES)
_FUNC_NAME_SET = frozenset(_FUNCTIONS)

# Atom after a bare call: number, name, or a single nesting level of (...).
_ATOM = (
    r"(?:"
    r"\d+(?:\.\d+)?(?:e[+-]?\d+)?"
    r"|[a-z_]\w*"
    r"|\((?:[^()]|\([^()]*\))*\)"
    r")"
)
_BARE_CALL_RE = re.compile(rf"\b({_FUNC_ALT})\s+({_ATOM})", re.IGNORECASE)


def normalize_expression(equation: str) -> str:
    """Normalize user math into a Python/NumPy-safe expression string.

    Handles ``y=`` / ``Y =`` prefixes, ``^`` powers, bare calls like
    ``sin x``, and implicit multiplication such as ``2x``, ``2(x+1)``,
    ``x(x+1)``, ``(x+1)(x-1)``, and ``2sin(x)``.
    """
    if not isinstance(equation, str):
        raise ValueError("equation must be a string")

    expression = equation.strip()
    expression = re.sub(r"^[yY]\s*=\s*", "", expression).strip()
    if not expression:
        raise ValueError("enter an equation after y=")
    if len(expression) > _MAX_EXPRESSION_LENGTH:
        raise ValueError("expression is too long")

    # Case-fold identifiers/functions; math input is ASCII-oriented.
    expression = expression.lower()
    expression = expression.replace("^", "**")

    # ``sin x`` / ``sin 2`` / ``log10 (x)`` → ``sin(x)`` / ``sin(2)`` / ``log10(x)``
    previous = None
    while previous != expression:
        previous = expression
        expression = _BARE_CALL_RE.sub(r"\1(\2)", expression)

    # Collapse whitespace so implicit-mul rules stay simple.
    expression = re.sub(r"\s+", "", expression)

    # Implicit multiplication, applied until stable.
    # Digits that are part of a function name (log10, atan2, log2) must not
    # gain a ``*`` before ``(``.
    previous = None
    while previous != expression:
        previous = expression
        expression = _insert_number_implicit_mul(expression)
        expression = re.sub(r"(\))(?=[\d(a-z_])", r"\1*", expression)
        expression = re.sub(
            r"([a-z_]\w*)(?=\()",
            lambda match: match.group(0)
            if match.group(1) in _FUNC_NAME_SET
            else f"{match.group(1)}*",
            expression,
        )

    return expression


def _insert_number_implicit_mul(expression: str) -> str:
    """Insert ``*`` after a number before a name or ``(``, unless it finishes a function name."""

    def replacer(match: re.Match[str]) -> str:
        number = match.group(1)
        start = match.start(1)
        letter_start = start
        while letter_start > 0 and expression[letter_start - 1].isalpha():
            letter_start -= 1
        candidate = expression[letter_start:start] + number
        if candidate in _FUNC_NAME_SET:
            return number
        return f"{number}*"

    return re.sub(
        r"(\d+(?:\.\d+)?(?:e[+-]?\d+)?)(?=[a-z_(])",
        replacer,
        expression,
    )


def _evaluate_node(node: ast.AST, x: np.ndarray) -> np.ndarray | float:
    """Progressively solve the expression tree with NumPy, honoring PEMDAS.

    ``ast.parse`` builds a tree whose *shape* already encodes PEMDAS:

        P — parentheses become nested subtrees, solved first (deepest wins)
        E — ``**`` binds tighter than * / and is right-associative
            (``2**3**2`` parses as ``2**(3**2)``)
        MD — ``*`` ``/`` ``%`` bind tighter than + and -
        AS — ``+`` ``-`` sit nearest the root, solved last

    This function walks that tree bottom-up: each recursive call solves one
    sub-expression into a NumPy array (or scalar), and the parent then
    combines the partial results with a vectorized ufunc (np.add,
    np.multiply, np.power, ...). Innermost parentheses/exponents are
    therefore computed first and additions last — exactly the order a human
    applying PEMDAS would use, but over the whole x array at once.
    """
    if isinstance(node, ast.Expression):
        return _evaluate_node(node.body, x)
    if (
        isinstance(node, ast.Constant)
        and isinstance(node.value, (int, float))
        and not isinstance(node.value, bool)
    ):
        return float(node.value)
    if isinstance(node, ast.Name):
        if node.id == "x":
            return x
        if node.id in _CONSTANTS:
            return _CONSTANTS[node.id]
        raise ValueError(f"unknown name: {node.id}")
    if isinstance(node, ast.BinOp) and type(node.op) in _BINARY:
        return _BINARY[type(node.op)](
            _evaluate_node(node.left, x), _evaluate_node(node.right, x)
        )
    if isinstance(node, ast.UnaryOp) and type(node.op) in _UNARY:
        return _UNARY[type(node.op)](_evaluate_node(node.operand, x))
    if (
        isinstance(node, ast.Call)
        and isinstance(node.func, ast.Name)
        and not node.keywords
    ):
        name = node.func.id
        if name in _UNARY_FUNCTIONS and len(node.args) == 1:
            return _UNARY_FUNCTIONS[name](_evaluate_node(node.args[0], x))
        if name in _NARY_FUNCTIONS:
            arity, fn = _NARY_FUNCTIONS[name]
            if len(node.args) == arity:
                args = [_evaluate_node(arg, x) for arg in node.args]
                return fn(*args)
    raise ValueError("unsupported expression")


def validate(equation: str) -> str:
    """Normalize + parse an equation without sampling it.

    Returns the normalized expression on success so the UI can echo it.
    Raises ValueError with a human-readable message when the input cannot
    be plotted (syntax error, unknown name, unsupported construct).
    """
    expression = normalize_expression(equation)
    try:
        tree = ast.parse(expression, mode="eval")
    except SyntaxError as error:
        raise ValueError(f"invalid syntax: {expression!r}") from error
    if sum(1 for _ in ast.walk(tree)) > _MAX_AST_NODES:
        raise ValueError("expression is too complex")
    # Dry-run over a tiny probe array to catch unknown names / bad calls
    # up-front, so the UI can reject the input before adding a curve.
    probe = np.array([0.5, 1.5], dtype=np.float64)
    with np.errstate(all="ignore"):
        _evaluate_node(tree, probe)
    return expression


def evaluate(equation: str, x_min: float, x_max: float, samples: int):
    """Return NumPy x/y arrays for an equation such as ``y=sin(x)``.

    Evaluation is fully vectorized: one AST walk produces ufuncs applied to
    the entire ``x`` array. Non-finite samples are replaced with NaN so ImPlot
    can skip gaps instead of failing the whole curve.
    """
    if not 2 <= samples <= 4096:
        raise ValueError("samples must be between 2 and 4096")
    if not (np.isfinite(x_min) and np.isfinite(x_max)):
        raise ValueError("x range must be finite")
    if x_min >= x_max:
        raise ValueError("x_min must be less than x_max")

    expression = normalize_expression(equation)
    try:
        tree = ast.parse(expression, mode="eval")
    except SyntaxError as error:
        raise ValueError(f"invalid syntax: {expression!r}") from error
    if sum(1 for _ in ast.walk(tree)) > _MAX_AST_NODES:
        raise ValueError("expression is too complex")

    xs = np.linspace(x_min, x_max, samples, dtype=np.float64)
    with np.errstate(all="ignore"):
        result = _evaluate_node(tree, xs)
    ys = np.asarray(result, dtype=np.float64)
    if ys.ndim == 0:
        ys = np.full_like(xs, float(ys))
    else:
        ys = np.broadcast_to(ys, xs.shape).astype(np.float64, copy=True)
    ys[~np.isfinite(ys)] = np.nan
    return xs, ys


def greeting(name: str) -> str:
    return f"{name}: enter any equation in x"

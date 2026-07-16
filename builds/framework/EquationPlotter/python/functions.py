"""Safe vectorized evaluation for user-entered equations."""

from __future__ import annotations

import ast
import operator

import numpy as np


_BINARY = {
    ast.Add: operator.add,
    ast.Sub: operator.sub,
    ast.Mult: operator.mul,
    ast.Div: operator.truediv,
    ast.Pow: operator.pow,
    ast.Mod: operator.mod,
}
_UNARY = {ast.UAdd: operator.pos, ast.USub: operator.neg}
_FUNCTIONS = {
    "abs": np.abs,
    "cos": np.cos,
    "exp": np.exp,
    "log": np.log,
    "log10": np.log10,
    "sin": np.sin,
    "sqrt": np.sqrt,
    "tan": np.tan,
}
_CONSTANTS = {"e": np.e, "pi": np.pi}


def _evaluate_node(node: ast.AST, x: np.ndarray) -> np.ndarray | float:
    """Interpret only the arithmetic AST nodes supported by the plotter."""
    if isinstance(node, ast.Expression):
        return _evaluate_node(node.body, x)
    if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)):
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
        and node.func.id in _FUNCTIONS
        and len(node.args) == 1
        and not node.keywords
    ):
        return _FUNCTIONS[node.func.id](_evaluate_node(node.args[0], x))
    raise ValueError("unsupported expression")


def evaluate(equation: str, x_min: float, x_max: float, samples: int):
    """Return NumPy x/y arrays for an equation such as ``y=sin(x)``."""
    expression = equation.strip()
    if expression.lower().startswith("y="):
        expression = expression[2:].strip()
    if not expression:
        raise ValueError("enter an equation after y=")

    tree = ast.parse(expression.replace("^", "**"), mode="eval")
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

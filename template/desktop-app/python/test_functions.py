"""Tests for the restricted NumPy equation interpreter."""

from pathlib import Path
import sys
import unittest

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import functions


class NormalizeTests(unittest.TestCase):
    def test_strips_y_equals_and_spaces(self):
        self.assertEqual(functions.normalize_expression("Y = 2x"), "2*x")

    def test_caret_power(self):
        self.assertEqual(functions.normalize_expression("x^2"), "x**2")

    def test_bare_sin_call(self):
        self.assertEqual(functions.normalize_expression("sin x"), "sin(x)")

    def test_implicit_multiplication_forms(self):
        self.assertEqual(functions.normalize_expression("2(x+1)"), "2*(x+1)")
        self.assertEqual(functions.normalize_expression("x(x+1)"), "x*(x+1)")
        self.assertEqual(
            functions.normalize_expression("(x+1)(x-1)"), "(x+1)*(x-1)"
        )
        self.assertEqual(functions.normalize_expression("2sin(x)"), "2*sin(x)")


class PemdasTests(unittest.TestCase):
    """The solved tree must honor PEMDAS ordering."""

    def _value_at(self, expression, x_value):
        _, ys = functions.evaluate(
            expression, x_value, x_value + 1e-9, 2
        )
        return float(ys[0])

    def test_multiplication_before_addition(self):
        self.assertAlmostEqual(self._value_at("2+3*4", 0.0), 14.0)

    def test_exponent_before_multiplication(self):
        self.assertAlmostEqual(self._value_at("2*3^2", 0.0), 18.0)

    def test_power_is_right_associative(self):
        self.assertAlmostEqual(self._value_at("2^3^2", 0.0), 512.0)

    def test_parentheses_first(self):
        self.assertAlmostEqual(self._value_at("(2+3)*4", 0.0), 20.0)

    def test_unary_minus_with_power(self):
        # -3^2 == -(3^2) per standard math convention (and Python).
        self.assertAlmostEqual(self._value_at("-3^2", 0.0), -9.0)

    def test_division_left_associative(self):
        self.assertAlmostEqual(self._value_at("12/3/2", 0.0), 2.0)

    def test_mixed_with_x(self):
        # 1+2x^2 at x=3 → 1+2*9 = 19
        self.assertAlmostEqual(self._value_at("1+2x^2", 3.0), 19.0)


class ValidateTests(unittest.TestCase):
    def test_validate_returns_normalized_expression(self):
        self.assertEqual(functions.validate("y = 2x"), "2*x")

    def test_validate_rejects_syntax_error(self):
        with self.assertRaises(ValueError):
            functions.validate("y=2x+")

    def test_validate_rejects_unknown_function(self):
        with self.assertRaises(ValueError):
            functions.validate("frobnicate(x)")

    def test_validate_rejects_code_execution(self):
        with self.assertRaises(ValueError):
            functions.validate("__import__('os')")


class EvaluateTests(unittest.TestCase):
    def test_y_equals_linear(self):
        xs, ys = functions.evaluate("y=x+1", -1.0, 1.0, 3)
        self.assertAlmostEqual(float(xs[1]), 0.0)
        self.assertAlmostEqual(float(ys[1]), 1.0)  # x=0 → 1

    def test_spot_sin_pi_over_two(self):
        _, ys = functions.evaluate("y=sin(x)", 0.0, np.pi, 3)
        self.assertAlmostEqual(float(ys[1]), 1.0, places=6)

    def test_spot_square(self):
        _, ys = functions.evaluate("y=x^2", 3.0, 3.0 + 1e-9, 2)
        self.assertAlmostEqual(float(ys[0]), 9.0, places=6)

    def test_vector_expression(self):
        xs, ys = functions.evaluate("y=sin(x) + x^2", -2.0, 2.0, 9)
        np.testing.assert_allclose(ys, np.sin(xs) + xs**2)

    def test_implicit_mul_evaluation(self):
        xs, ys = functions.evaluate("y=2x", 0.0, 4.0, 5)
        np.testing.assert_allclose(ys, 2 * xs)

    def test_constant_expression_is_broadcast(self):
        xs, ys = functions.evaluate("y=pi", -1.0, 1.0, 5)
        np.testing.assert_allclose(ys, np.full_like(xs, np.pi))

    def test_non_finite_values_become_nan(self):
        _, ys = functions.evaluate("1 / x", -1.0, 1.0, 3)
        self.assertTrue(np.isnan(ys[1]))

    def test_scalar_division_by_zero_is_nan(self):
        _, ys = functions.evaluate("1 / 0", -1.0, 1.0, 3)
        self.assertTrue(np.isnan(ys).all())

    def test_log_domain_error_is_nan(self):
        xs, ys = functions.evaluate("log(x)", -1.0, 1.0, 5)
        # Negative x and x=0 should be NaN; positive should be finite.
        self.assertTrue(np.isnan(ys[0]))
        self.assertTrue(np.isnan(ys[2]))  # x ≈ 0
        self.assertTrue(np.isfinite(ys[-1]))

    def test_sqrt_domain_error_is_nan(self):
        _, ys = functions.evaluate("sqrt(x)", -1.0, 1.0, 3)
        self.assertTrue(np.isnan(ys[0]))
        self.assertAlmostEqual(float(ys[-1]), 1.0, places=6)

    def test_all_math_functions_evaluate(self):
        cases = {
            "abs(x)": np.abs,
            "sin(x)": np.sin,
            "cos(x)": np.cos,
            "tan(x)": np.tan,
            "asin(x)": np.arcsin,
            "acos(x)": np.arccos,
            "atan(x)": np.arctan,
            "sinh(x)": np.sinh,
            "cosh(x)": np.cosh,
            "tanh(x)": np.tanh,
            "exp(x)": np.exp,
            "log(x)": np.log,
            "log10(x)": np.log10,
            "log2(x)": np.log2,
            "sqrt(x)": np.sqrt,
            "floor(x)": np.floor,
            "ceil(x)": np.ceil,
            "pow(x,2)": lambda x: np.power(x, 2),
            "min(x,0)": lambda x: np.minimum(x, 0),
            "max(x,0)": lambda x: np.maximum(x, 0),
            "atan2(x,1)": lambda x: np.arctan2(x, 1),
        }
        for expression, expected in cases.items():
            with self.subTest(expression=expression):
                # Keep domain inside where the function is defined for most.
                x_min, x_max = 0.25, 0.75
                if expression.startswith("asin") or expression.startswith("acos"):
                    x_min, x_max = -0.5, 0.5
                xs, ys = functions.evaluate(f"y={expression}", x_min, x_max, 8)
                with np.errstate(all="ignore"):
                    want = np.asarray(expected(xs), dtype=np.float64)
                want[~np.isfinite(want)] = np.nan
                np.testing.assert_allclose(ys, want, equal_nan=True)

    def test_constants_e_and_pi(self):
        _, ys = functions.evaluate("e", 0.0, 1.0, 3)
        np.testing.assert_allclose(ys, np.full(3, np.e))
        _, ys = functions.evaluate("pi", 0.0, 1.0, 3)
        np.testing.assert_allclose(ys, np.full(3, np.pi))

    def test_rejects_code_execution(self):
        with self.assertRaises(ValueError):
            functions.evaluate(
                "__import__('os').system('echo unsafe')", 0, 1, 2
            )

    def test_rejects_attribute_access(self):
        with self.assertRaises(ValueError):
            functions.evaluate("x.__class__", 0, 1, 2)

    def test_rejects_unknown_name(self):
        with self.assertRaises(ValueError):
            functions.evaluate("y=foo(x)", 0, 1, 2)

    def test_rejects_unbounded_sample_count(self):
        with self.assertRaises(ValueError):
            functions.evaluate("x", 0, 1, 1_000_000)


if __name__ == "__main__":
    unittest.main()

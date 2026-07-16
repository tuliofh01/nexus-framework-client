"""Tests for the restricted equation interpreter."""

from pathlib import Path
import sys
import unittest

import numpy as np

sys.path.insert(0, str(Path(__file__).resolve().parent))
import functions


class EvaluateTests(unittest.TestCase):
    def test_vector_expression(self):
        xs, ys = functions.evaluate("y=sin(x) + x^2", -2.0, 2.0, 9)

        np.testing.assert_allclose(ys, np.sin(xs) + xs**2)

    def test_constant_expression_is_broadcast(self):
        xs, ys = functions.evaluate("y=pi", -1.0, 1.0, 5)

        np.testing.assert_allclose(ys, np.full_like(xs, np.pi))

    def test_non_finite_values_become_nan(self):
        _, ys = functions.evaluate("1 / x", -1.0, 1.0, 3)

        self.assertTrue(np.isnan(ys[1]))

    def test_rejects_code_execution(self):
        with self.assertRaises(ValueError):
            functions.evaluate("__import__('os').system('echo unsafe')", 0, 1, 2)

    def test_rejects_attribute_access(self):
        with self.assertRaises(ValueError):
            functions.evaluate("x.__class__", 0, 1, 2)

    def test_rejects_unbounded_sample_count(self):
        with self.assertRaises(ValueError):
            functions.evaluate("x", 0, 1, 1_000_000)


if __name__ == "__main__":
    unittest.main()

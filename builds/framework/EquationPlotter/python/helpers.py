"""Generic helpers for EquationPlotter.

Evaluated in-process by the C++ core:
  * Desktop: imported through pybind11 embed.
  * Android: executed by Chaquopy via the Zig JNI bridge.

Replace or extend this module for your own logic. The optional plotter
Plot helpers can be added in python/functions.py.
"""


def greeting(name: str) -> str:
    return f"Hello, {name}!"

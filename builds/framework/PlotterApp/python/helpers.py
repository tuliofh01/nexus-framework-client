"""Generic helpers for PlotterApp.

Evaluated in-process by the C++ core:
  * Desktop: imported through pybind11 embed.
  * Android: executed by Chaquopy via the Djinni PythonBridge.

Replace or extend this module for your own logic. The optional plotter
example lives under examples/plotter/ with python/functions.py.
"""


def greeting(name: str) -> str:
    return f"Hello, {name}!"

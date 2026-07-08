"""Function library for the {{projectName}} plotter.

Evaluated in-process by the C++ core:
  * Desktop: imported through pybind11 embed. The returned numpy arrays are
    read via the buffer protocol, so samples never leave native memory.
  * Android: the identical file is executed by Chaquopy; the Djinni-bridged
    PythonEngine converts results to double[] on the JNI boundary.

Add a function by writing `def my_func(x): ...` (vectorized over a numpy
array) and listing it in REGISTRY; the C++ FunctionRegistry catalog refers
to these names.
"""

import numpy as np


def sine(x):
    return np.sin(x)


def cosine(x):
    return np.cos(x)


def gaussian(x, mu=0.0, sigma=1.5):
    return np.exp(-((x - mu) ** 2) / (2.0 * sigma**2))


def polynomial(x):
    """Cubic with visible turning points inside the default [-10, 10] range."""
    return 0.02 * x**3 - 0.3 * x + 1.0


def damped(x):
    """Damped oscillation, mirrored so it decays away from the origin."""
    return np.exp(-0.3 * np.abs(x)) * np.cos(3.0 * x)


def sinc(x):
    return np.sinc(x / np.pi)  # np.sinc is normalized; undo for the classic sin(x)/x


REGISTRY = {
    "sine": sine,
    "cosine": cosine,
    "gaussian": gaussian,
    "polynomial": polynomial,
    "damped": damped,
    "sinc": sinc,
}


def evaluate(name, x_min, x_max, samples):
    """Entry point called from C++.

    Returns (xs, ys) as contiguous float64 arrays. Raises KeyError for an
    unknown name — the C++ side surfaces the exception text in the UI.
    """
    fn = REGISTRY[name]
    xs = np.linspace(float(x_min), float(x_max), int(samples), dtype=np.float64)
    ys = np.ascontiguousarray(fn(xs), dtype=np.float64)
    return xs, ys

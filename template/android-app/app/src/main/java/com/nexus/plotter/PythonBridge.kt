package com.nexus.plotter

/**
 * Kotlin side of the Djinni bridge. Implemented via Chaquopy to call the shared
 * Python module (functions.py).
 */
abstract class PythonBridge {
    abstract fun evaluate(name: String, xMin: Double, xMax: Double, samples: Int): EvalResult
}

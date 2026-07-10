package com.nexus.{{packageName}}

import com.chaquo.python.Python

/** Kotlin-side [PythonBridge] that calls app/src/main/python/helpers.py via Chaquopy. */
class ChaquopyPythonBridge : PythonBridge() {
    override fun greeting(name: String): String {
        return try {
            val mod = Python.getInstance().getModule("helpers")
            mod.callAttr("greeting", name).toString()
        } catch (e: Exception) {
            "Hello, $name!"
        }
    }

    override fun evaluate(
        functionName: String,
        xMin: Double,
        xMax: Double,
        samples: Int,
    ): EvalResult {
        return EvalResult(false, "evaluate() reserved for examples/plotter/", ArrayList(), ArrayList())
    }
}

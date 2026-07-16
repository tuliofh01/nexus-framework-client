package com.nexus.{{packageName}}

abstract class PythonBridge {
    abstract fun greeting(name: String): String
    abstract fun evaluate(
        functionName: String,
        xMin: Double,
        xMax: Double,
        samples: Int,
    ): EvalResult
}

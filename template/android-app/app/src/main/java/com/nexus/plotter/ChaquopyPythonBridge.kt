package com.nexus.plotter

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import java.util.ArrayList

/** Kotlin-side [PythonBridge] that calls app/src/main/python/functions.py via Chaquopy. */
class ChaquopyPythonBridge : PythonBridge() {
    override fun evaluate(
        functionName: String,
        xMin: Double,
        xMax: Double,
        samples: Int,
    ): EvalResult {
        return try {
            val mod = Python.getInstance().getModule("functions")
            val result: PyObject = mod.callAttr("evaluate", functionName, xMin, xMax, samples)
            val pair = result.asList()
            EvalResult(
                true,
                "",
                toDoubleList(pair[0]),
                toDoubleList(pair[1]),
            )
        } catch (e: Exception) {
            EvalResult(false, e.message ?: e.toString(), ArrayList(), ArrayList())
        }
    }

    private fun toDoubleList(array: PyObject): ArrayList<Double> {
        @Suppress("UNCHECKED_CAST")
        return array.callAttr("tolist").toJava(ArrayList::class.java) as ArrayList<Double>
    }
}

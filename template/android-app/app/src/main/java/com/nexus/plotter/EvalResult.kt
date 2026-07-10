package com.nexus.plotter

data class EvalResult(
    val ok: Boolean,
    val error: String = "",
    val xs: ArrayList<Double> = arrayListOf(),
    val ys: ArrayList<Double> = arrayListOf(),
)

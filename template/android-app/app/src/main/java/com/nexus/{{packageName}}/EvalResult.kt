package com.nexus.{{packageName}}

data class EvalResult(
    val ok: Boolean,
    val error: String,
    val xs: List<Double>,
    val ys: List<Double>,
)

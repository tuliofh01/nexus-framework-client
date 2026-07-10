package com.nexus.{{packageName}}

/**
 * Kotlin stub for optional runtime flows — actual execution lives in native
 * FlowRunner (src/service/FlowRunner.cpp), wired from SDL_main.
 */
object FlowRunner {
    @JvmStatic
    fun notifyManualTrigger(flowId: String) {
        android.util.Log.d("FlowRunner", "manual trigger stub: $flowId")
    }
}

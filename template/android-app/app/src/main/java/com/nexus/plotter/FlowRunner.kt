package com.nexus.plotter

/**
 * Kotlin stub for optional runtime flows — actual execution lives in native
 * [nxs::service::FlowRunner] (see src/service/FlowRunner.cpp), wired from SDL_main.
 *
 * Disable flows by deleting flows/flows.json or setting `"flows": { "enabled": false }`
 * in nxs_config.json.
 */
object FlowRunner {
    /** Reserved for v1.1 manual flow triggers from the JVM UI. */
    @JvmStatic
    fun notifyManualTrigger(flowId: String) {
        // Native FlowRunner handles manual triggers in-process today.
        android.util.Log.d("FlowRunner", "manual trigger stub: $flowId")
    }
}

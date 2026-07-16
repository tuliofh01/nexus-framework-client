package com.nexus.{{packageName}}

object AppCore {
    init {
        System.loadLibrary("{{projectName}}")
    }

    external fun installPythonBridgeNative(bridge: PythonBridge)

    fun installPythonBridge(bridge: PythonBridge) {
        installPythonBridgeNative(bridge)
    }
}

package com.nexus.{{packageName}}

import android.os.Bundle
import org.libsdl.app.SDLActivity

/**
 * SDL3 host activity. Boots Chaquopy (via [NexusApplication]), installs the
 * Chaquopy-backed [PythonBridge] into the native core, then hands off to
 * SDL_main (see src/main.cpp).
 */
class MainActivity : SDLActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCore.installPythonBridge(ChaquopyPythonBridge())
        FlowRunner.notifyManualTrigger("app.ready")
        super.onCreate(savedInstanceState)
    }

    override fun getLibraries(): Array<String> {
        return arrayOf("SDL3", "{{projectName}}")
    }
}

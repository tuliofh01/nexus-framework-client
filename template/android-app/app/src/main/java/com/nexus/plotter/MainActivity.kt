package com.nexus.plotter

import android.os.Bundle
import org.libsdl.app.SDLActivity

/**
 * SDL3 host activity. Boots Chaquopy (via [NexusApplication]), installs the
 * Chaquopy-backed [PythonBridge] into the native core, then hands off to
 * SDL_main (see src/main.cpp). Rendering is handled entirely by SDL/ImGui.
 */
class MainActivity : SDLActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        PlotterCore.installPythonBridge(ChaquopyPythonBridge())
        super.onCreate(savedInstanceState)
    }

    override fun getLibraries(): Array<String> {
        return arrayOf("SDL3", "{{projectName}}")
    }
}

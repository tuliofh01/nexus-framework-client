package com.nexus.plotter

import android.os.Bundle
import org.libsdl.app.SDLActivity

/**
 * Boots Chaquopy (via [NexusApplication]) and installs the ChaquopyPythonBridge
 * into the native Plotter core. Rendering is handled entirely by the SDL/ImGui
 * native code.
 */
class MainActivity : SDLActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlotterCore.installPythonBridge(ChaquopyPythonBridge())
    }
}
package com.nexus.plotter

import android.os.Bundle
import org.libsdl.app.SDLActivity

/**
 * SDL3 host activity. Installs the Chaquopy-backed [PythonBridge] into the
 * native core, then hands off to SDL_main (see src/main.cpp).
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

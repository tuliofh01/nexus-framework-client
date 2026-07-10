package nexus.opensource

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nexus.opensource.controller.CounterController
import nexus.opensource.model.NexusBranding
import nexus.opensource.view.CounterScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = NexusBranding.windowTitle("Nexus Framework Client"),
    ) {
        val controller = remember { CounterController() }
        MaterialTheme {
            CounterScreen(controller)
        }
    }
}

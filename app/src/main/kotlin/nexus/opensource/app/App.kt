package nexus.opensource.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nexus.opensource.app.controller.CounterController
import nexus.opensource.app.view.CounterScreen
import nexus.opensource.utils.NexusBranding

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

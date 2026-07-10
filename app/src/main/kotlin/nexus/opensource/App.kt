package nexus.opensource

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nexus.opensource.controller.CounterController
import nexus.opensource.controller.GenerateController
import nexus.opensource.model.NexusBranding
import nexus.opensource.view.CounterScreen
import nexus.opensource.view.GenerateProjectScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = NexusBranding.windowTitle("Nexus Framework Client"),
    ) {
        var showGenerate by remember { mutableStateOf(false) }
        val counterController = remember { CounterController() }
        val generateController = remember { GenerateController() }
        MaterialTheme {
            if (showGenerate) {
                GenerateProjectScreen(
                    controller = generateController,
                    onBack = { showGenerate = false },
                )
            } else {
                CounterScreen(
                    controller = counterController,
                    onOpenGenerate = { showGenerate = true },
                )
            }
        }
    }
}

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
import nexus.opensource.view.BlueprintEditorScreen
import nexus.opensource.view.GenerateProjectScreen

enum class AppScreen {
    Counter,
    Generate,
    BlueprintEditor,
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = NexusBranding.windowTitle("Nexus Framework Client"),
    ) {
        var screen by remember { mutableStateOf(AppScreen.Counter) }
        val counterController = remember { CounterController() }
        val generateController = remember { GenerateController() }
        MaterialTheme {
            when (screen) {
                AppScreen.Counter -> CounterScreen(
                    controller = counterController,
                    onOpenGenerate = { screen = AppScreen.Generate },
                )
                AppScreen.Generate -> GenerateProjectScreen(
                    controller = generateController,
                    onBack = { screen = AppScreen.Counter },
                    onEditBlueprint = { screen = AppScreen.BlueprintEditor },
                )
                AppScreen.BlueprintEditor -> BlueprintEditorScreen(
                    controller = generateController.blueprintEditor,
                    onBack = { screen = AppScreen.Generate },
                )
            }
        }
    }
}

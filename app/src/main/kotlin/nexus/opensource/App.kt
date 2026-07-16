package nexus.opensource

import androidx.compose.material.MaterialTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.controller.LoadingController

import nexus.opensource.framework.model.DebuggerService
import nexus.opensource.framework.model.TestRunner
import nexus.opensource.framework.core.model.NexusBranding

import nexus.opensource.framework.view.BlueprintEditorScreen
import nexus.opensource.framework.view.DashboardScreen
import nexus.opensource.framework.view.DebuggerPanel
import nexus.opensource.framework.view.FlowsEditorScreen
import nexus.opensource.framework.view.GenerateProjectScreen
import nexus.opensource.framework.view.LoadingScreen
import nexus.opensource.framework.view.TestRunnerPanel

sealed interface AppScreen {
    data object Loading : AppScreen
    data object Dashboard : AppScreen
    data object Generate : AppScreen
    data object BlueprintEditor : AppScreen
    data object FlowsEditor : AppScreen
    data object Debugger : AppScreen
    data object TestRunner : AppScreen
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = NexusBranding.windowTitle("Nexus Framework Client"),
    ) {
        var screen: AppScreen by remember { mutableStateOf(AppScreen.Loading) }
        val loadingController = remember { LoadingController() }
        val generateController = remember { GenerateController() }
        val debuggerService = remember { DebuggerService() }
        val testRunner = remember { TestRunner() }
        MaterialTheme {
            when (screen) {
                AppScreen.Loading -> LoadingScreen(
                    controller = loadingController,
                    onComplete = { screen = AppScreen.Dashboard },
                )
                AppScreen.Dashboard -> DashboardScreen(
                    onNavigate = { screen = it },
                )
                AppScreen.Generate -> GenerateProjectScreen(
                    controller = generateController,
                    onBack = { screen = AppScreen.Dashboard },
                    onEditBlueprint = { screen = AppScreen.BlueprintEditor },
                    onEditFlows = { screen = AppScreen.FlowsEditor },
                )
                AppScreen.BlueprintEditor -> BlueprintEditorScreen(
                    controller = generateController.blueprintEditor,
                    onBack = { screen = AppScreen.Generate },
                )
                AppScreen.FlowsEditor -> FlowsEditorScreen(
                    controller = generateController.flowsEditor,
                    onBack = { screen = AppScreen.Generate },
                )
                AppScreen.Debugger -> DebuggerPanel(
                    debugger = debuggerService,
                    onBack = { screen = AppScreen.Dashboard },
                )
                AppScreen.TestRunner -> TestRunnerPanel(
                    runner = testRunner,
                    onBack = { screen = AppScreen.Dashboard },
                )
            }
        }
    }
}

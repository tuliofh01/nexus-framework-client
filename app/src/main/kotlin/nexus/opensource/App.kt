package nexus.opensource

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.controller.LoadingController
import nexus.opensource.framework.core.model.NexusBranding
import nexus.opensource.framework.model.DebuggerService
import nexus.opensource.framework.model.RecentProjectsStore
import nexus.opensource.framework.model.TestRunner
import nexus.opensource.framework.view.*

sealed interface AppScreen {
    data object Loading : AppScreen
    /** Main dashboard — generated apps, create / open / Langflow, branding. */
    data object Home : AppScreen
    /**
     * Deprecated alias of [Home]. Prefer [Home].
     * [normalizeScreen] maps Welcome and Dashboard → Home.
     */
    data object Welcome : AppScreen
    /**
     * Deprecated alias of [Home] for older navigation call sites.
     */
    data object Dashboard : AppScreen
    data object Generate : AppScreen
    data object BlueprintEditor : AppScreen
    data object FlowsEditor : AppScreen
    data object Debugger : AppScreen
    data object TestRunner : AppScreen
}

private val homeScreen = AppScreen.Home

/** Collapse Welcome/Dashboard aliases into Home so there is a single hub. */
private fun normalizeScreen(screen: AppScreen): AppScreen =
    when (screen) {
        AppScreen.Welcome, AppScreen.Dashboard -> AppScreen.Home
        else -> screen
    }

@Composable
private fun AppContent(
    screen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    showWhatsNew: Boolean,
    onDismissWhatsNew: () -> Unit,
    onShowWhatsNew: () -> Unit,
    loadingController: LoadingController,
    generateController: GenerateController,
    debuggerService: DebuggerService,
    testRunner: TestRunner,
    recentProjectsStore: RecentProjectsStore,
) {
    var transitionOverlay by remember { mutableStateOf(false) }
    val resolved = normalizeScreen(screen)

    LaunchedEffect(resolved) {
        if (resolved != AppScreen.Loading) {
            transitionOverlay = true
            delay(280L)
            transitionOverlay = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = resolved,
            transitionSpec = {
                fadeIn(tween(260)) togetherWith fadeOut(tween(220))
            },
            label = "screenTransition",
        ) { target ->
            when (target) {
                AppScreen.Loading -> LoadingScreen(
                    controller = loadingController,
                    onComplete = { onNavigate(AppScreen.Home) },
                )
                AppScreen.Home, AppScreen.Welcome, AppScreen.Dashboard -> {
                    HomeScreen(
                        recentProjectsStore = recentProjectsStore,
                        generateController = generateController,
                        onNavigate = onNavigate,
                        onShowWhatsNew = onShowWhatsNew,
                        onProjectOpened = { onNavigate(AppScreen.BlueprintEditor) },
                        onLangflowImported = { onNavigate(AppScreen.BlueprintEditor) },
                    )
                    if (showWhatsNew) {
                        WhatsNewDialog(onDismiss = onDismissWhatsNew)
                    }
                }
                AppScreen.Generate -> GenerateProjectScreen(
                    controller = generateController,
                    onBack = { onNavigate(homeScreen) },
                    onEditBlueprint = { onNavigate(AppScreen.BlueprintEditor) },
                    onEditFlows = { onNavigate(AppScreen.FlowsEditor) },
                )
                AppScreen.BlueprintEditor -> BlueprintEditorScreen(
                    controller = generateController.blueprintEditor,
                    onBack = { onNavigate(homeScreen) },
                )
                AppScreen.FlowsEditor -> FlowsEditorScreen(
                    controller = generateController.flowsEditor,
                    onBack = { onNavigate(homeScreen) },
                )
                AppScreen.Debugger -> DebuggerPanel(
                    debugger = debuggerService,
                    onBack = { onNavigate(homeScreen) },
                )
                AppScreen.TestRunner -> TestRunnerPanel(
                    runner = testRunner,
                    onBack = { onNavigate(homeScreen) },
                )
            }
        }

        FlamingoTransitionOverlay(
            visible = transitionOverlay && resolved != AppScreen.Loading,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

fun main() = application {
    val windowIcon = rememberFlamingoWindowIcon()

    Window(
        onCloseRequest = ::exitApplication,
        title = NexusBranding.windowTitle("Nexus Framework Client"),
        icon = windowIcon?.let { BitmapPainter(it) },
    ) {
        var screen by remember { mutableStateOf<AppScreen>(AppScreen.Loading) }
        var showWhatsNew by remember { mutableStateOf(true) }
        val loadingController = remember { LoadingController() }
        val generateController = remember { GenerateController() }
        val debuggerService = remember { DebuggerService() }
        val testRunner = remember { TestRunner() }
        val recentProjectsStore = remember { RecentProjectsStore() }

        MaterialTheme {
            AppContent(
                screen = screen,
                onNavigate = { screen = normalizeScreen(it) },
                showWhatsNew = showWhatsNew && normalizeScreen(screen) == AppScreen.Home,
                onDismissWhatsNew = { showWhatsNew = false },
                onShowWhatsNew = { showWhatsNew = true },
                loadingController = loadingController,
                generateController = generateController,
                debuggerService = debuggerService,
                testRunner = testRunner,
                recentProjectsStore = recentProjectsStore,
            )
        }
    }
}

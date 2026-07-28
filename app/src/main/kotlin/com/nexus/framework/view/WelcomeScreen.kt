package com.nexus.framework.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nexus.framework.AppScreen
import com.nexus.framework.controller.GenerateController
import com.nexus.framework.model.RecentProjectsStore

/**
 * Deprecated alias — use [HomeScreen] / [AppScreen.Home].
 *
 * Kept so older call sites and docs that still say "Welcome" compile.
 */
@Deprecated(
    message = "HomeScreen is the main dashboard; use HomeScreen / AppScreen.Home",
    replaceWith = ReplaceWith(
        "HomeScreen(recentProjectsStore, generateController, onNavigate, onShowWhatsNew, onProjectOpened, onLangflowImported)",
        "com.nexus.framework.view.HomeScreen",
    ),
)
@Composable
fun WelcomeScreen(
    recentProjectsStore: RecentProjectsStore = remember { RecentProjectsStore() },
    generateController: GenerateController? = null,
    onNavigate: (AppScreen) -> Unit,
    onShowWhatsNew: () -> Unit = {},
    onProjectOpened: () -> Unit = { onNavigate(AppScreen.BlueprintEditor) },
    onLangflowImported: () -> Unit = { onNavigate(AppScreen.BlueprintEditor) },
) {
    HomeScreen(
        recentProjectsStore = recentProjectsStore,
        generateController = generateController,
        onNavigate = onNavigate,
        onShowWhatsNew = onShowWhatsNew,
        onProjectOpened = onProjectOpened,
        onLangflowImported = onLangflowImported,
    )
}

package com.nexus.framework.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nexus.framework.AppScreen
import com.nexus.framework.ui.generate.GenerateController
import com.nexus.framework.shared.RecentProjectsStore

/**
 * Deprecated alias — use [HomeScreen] / [AppScreen.Home].
 *
 * Kept so older call sites and docs that still say "Welcome" compile.
 */
@Deprecated(
    message = "HomeScreen is the main dashboard; use HomeScreen / AppScreen.Home",
    replaceWith = ReplaceWith(
        "HomeScreen(recentProjectsStore, generateController, onNavigate, onShowWhatsNew, onProjectOpened, onLangflowImported)",
        "com.nexus.framework.ui.home.HomeScreen",
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

package com.nexus.framework.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.nexus.framework.AppScreen
import com.nexus.framework.ui.generate.GenerateController
import com.nexus.framework.shared.RecentProjectsStore

/**
 * Deprecated alias — [HomeScreen] is the main Home dashboard.
 *
 * Prefer [AppScreen.Home]. [AppScreen.Dashboard] still normalizes to Home in [App.kt].
 *
 * Layout mockup: `docs/assets/examples/mockup-welcome.svg` (Home hub)
 */
@Deprecated(
    message = "HomeScreen is the home dashboard; use HomeScreen / AppScreen.Home",
    replaceWith = ReplaceWith(
        "HomeScreen(recentProjectsStore, generateController, onNavigate, onShowWhatsNew, onProjectOpened, onLangflowImported)",
        "com.nexus.framework.ui.home.HomeScreen",
    ),
)
@Composable
fun DashboardScreen(
    onNavigate: (AppScreen) -> Unit,
    recentProjectsStore: RecentProjectsStore = remember { RecentProjectsStore() },
    generateController: GenerateController? = null,
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

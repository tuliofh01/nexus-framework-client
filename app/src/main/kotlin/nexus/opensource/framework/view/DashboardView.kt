package nexus.opensource.framework.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nexus.opensource.AppScreen
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.model.RecentProjectsStore

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
        "nexus.opensource.framework.view.HomeScreen",
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

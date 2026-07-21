package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.AppScreen
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.core.model.NexusBranding
import nexus.opensource.framework.model.RecentProject
import nexus.opensource.framework.model.RecentProjectsStore
import nexus.opensource.framework.util.NativeFileDialogs
import java.nio.file.Path

/**
 * Home hub — flamingo branding, recent generated apps, create / open / Langflow import.
 *
 * This is the primary landing screen after Loading (`AppScreen.Home`).
 * `AppScreen.Dashboard` / deprecated `WelcomeScreen` resolve to the same UI.
 *
 * Layout mockup: `docs/assets/examples/mockup-welcome.svg` (Home hub)
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    recentProjectsStore: RecentProjectsStore = remember { RecentProjectsStore() },
    generateController: GenerateController? = null,
    onNavigate: (AppScreen) -> Unit,
    onShowWhatsNew: () -> Unit = {},
    onProjectOpened: () -> Unit = { onNavigate(AppScreen.BlueprintEditor) },
    onLangflowImported: () -> Unit = { onNavigate(AppScreen.BlueprintEditor) },
) {
    var recentProjects by remember { mutableStateOf(recentProjectsStore.loadRecent()) }
    var showAbout by remember { mutableStateOf(false) }
    var showAnalyzeMenu by remember { mutableStateOf(false) }
    var homeStatus by remember { mutableStateOf("") }

    fun refreshRecents() {
        recentProjectsStore.refresh()
        recentProjects = recentProjectsStore.loadRecent()
    }

    fun openGenerated(path: Path, display: RecentProject? = null) {
        val controller = generateController
        if (controller == null) {
            homeStatus = "Generate controller unavailable"
            return
        }
        val err = controller.openGeneratedProject(path)
        if (err != null) {
            homeStatus = err
            return
        }
        val project = display ?: RecentProject(
            name = path.fileName.toString(),
            path = path.toAbsolutePath().normalize().toString(),
            appType = controller.appType.label,
            lastOpenedEpochMs = System.currentTimeMillis(),
        )
        recentProjectsStore.markOpened(project)
        refreshRecents()
        homeStatus = controller.statusMessage
        onProjectOpened()
    }

    fun importLangflow() {
        val controller = generateController
        if (controller == null) {
            homeStatus = "Generate controller unavailable"
            return
        }
        val path = NativeFileDialogs.pickOpenFile("Import Langflow export JSON") ?: return
        val err = controller.importLangflow(path)
        if (err != null) {
            homeStatus = err
            return
        }
        homeStatus = controller.statusMessage
        onLangflowImported()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = NexusTheme.DarkBg) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedFlamingoLogo(
                    modifier = Modifier.size(112.dp),
                    style = FlamingoAnimationStyle.Idle,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = NexusBranding.FRAMEWORK_NAME,
                    style = MaterialTheme.typography.h3.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = NexusTheme.TextPrimary,
                    ),
                )

                Text(
                    text = NexusBranding.versionLabel(),
                    style = MaterialTheme.typography.body1.copy(
                        color = NexusTheme.TextSecondary,
                        fontSize = 14.sp,
                    ),
                )

                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Generated apps",
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = NexusTheme.TextPrimary,
                        ),
                    )
                    TextButton(onClick = { refreshRecents() }) {
                        Text("Refresh", color = NexusTheme.AccentCyan, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (recentProjects.isEmpty()) {
                    EmptyRecentProjectsCard()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false,
                    ) {
                        items(recentProjects) { project ->
                            RecentProjectCard(
                                project = project,
                                onClick = {
                                    openGenerated(Path.of(project.path), project)
                                },
                            )
                        }
                    }
                }

                if (homeStatus.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = homeStatus,
                        style = MaterialTheme.typography.caption.copy(color = NexusTheme.AccentOrange),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "SDL3 · ImGui · Lua · Python · TypeScript/XHTML · Zig",
                    style = MaterialTheme.typography.caption.copy(
                        color = NexusTheme.TextMuted,
                        fontSize = 11.sp,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.75f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HomeActionCard(
                    title = "Create project",
                    description = "Scaffold a new native app from Nexus templates",
                    accent = NexusTheme.AccentCyan,
                    icon = { NexusCreationIcon(modifier = Modifier.size(36.dp)) },
                    onClick = { onNavigate(AppScreen.Generate) },
                )

                HomeActionCard(
                    title = "Open generated app",
                    description = "Load blueprint.json + flows from builds/framework",
                    accent = NexusTheme.AccentGreen,
                    icon = { FlamingoGlyph(modifier = Modifier.size(32.dp)) },
                    onClick = {
                        val path = NativeFileDialogs.pickOpenFile(
                            "Select blueprint.json inside a generated app",
                            "*.json",
                        ) ?: return@HomeActionCard
                        val root = when {
                            path.fileName.toString() == "blueprint.json" -> path.parent
                            path.fileName.toString() == "flows.json" -> path.parent?.parent
                            else -> path.parent
                        } ?: path.parent
                        if (root != null) openGenerated(root)
                    },
                )

                HomeActionCard(
                    title = "Import Langflow JSON",
                    description = "Open Blueprint editor with flow nodes + disabled stubs",
                    accent = NexusTheme.AccentOrange,
                    icon = { FlamingoIcon(modifier = Modifier.size(36.dp)) },
                    onClick = { importLangflow() },
                )

                Box {
                    HomeActionCard(
                        title = "Analyze project",
                        description = "Debug logs, inspect flows, and run tests",
                        accent = NexusTheme.AccentPink,
                        icon = { FlamingoGlyph(modifier = Modifier.size(32.dp)) },
                        onClick = { showAnalyzeMenu = true },
                    )
                    DropdownMenu(
                        expanded = showAnalyzeMenu,
                        onDismissRequest = { showAnalyzeMenu = false },
                    ) {
                        DropdownMenuItem(onClick = {
                            showAnalyzeMenu = false
                            onNavigate(AppScreen.Debugger)
                        }) {
                            Text("Debugger — pattern log scanner")
                        }
                        DropdownMenuItem(onClick = {
                            showAnalyzeMenu = false
                            onNavigate(AppScreen.FlowsEditor)
                        }) {
                            Text("Flows Editor — automation services")
                        }
                        DropdownMenuItem(onClick = {
                            showAnalyzeMenu = false
                            onNavigate(AppScreen.TestRunner)
                        }) {
                            Text("Test Runner — in-memory unit tests")
                        }
                    }
                }

                HomeActionCard(
                    title = "Blueprint editor",
                    description = "Design the app structure DAG",
                    accent = NexusTheme.BrandPurple,
                    icon = { FlamingoIcon(modifier = Modifier.size(36.dp)) },
                    onClick = { onNavigate(AppScreen.BlueprintEditor) },
                )

                Spacer(Modifier.weight(1f))

                QuickLinksRow(
                    onAbout = { showAbout = true },
                    onWhatsNew = onShowWhatsNew,
                    onDocs = {
                        java.awt.Desktop.getDesktop().browse(java.net.URI(NexusBranding.DOCS_URL))
                    },
                    onRepo = {
                        java.awt.Desktop.getDesktop().browse(java.net.URI(NexusBranding.REPO_URL))
                    },
                )
            }
        }
    }

    if (showAbout) {
        AboutNexusDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun EmptyRecentProjectsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = NexusTheme.CardBg,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "No generated apps found",
                style = MaterialTheme.typography.subtitle1.copy(color = NexusTheme.TextPrimary),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Create a project or open a folder under builds/framework/ — items appear here after refresh.",
                style = MaterialTheme.typography.body2.copy(color = NexusTheme.TextSecondary),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RecentProjectCard(
    project: RecentProject,
    onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .shadow(if (hovered) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clickable(onClick = onClick),
        color = NexusTheme.CardBg,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlamingoGlyph(modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.subtitle2.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = NexusTheme.TextPrimary,
                    ),
                )
                Text(
                    text = project.appType,
                    style = MaterialTheme.typography.caption.copy(color = NexusTheme.AccentCyan),
                )
                Text(
                    text = project.path,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.caption.copy(color = NexusTheme.TextSecondary),
                )
            }
            Text(
                text = project.formattedLastOpened(),
                style = MaterialTheme.typography.caption.copy(
                    color = NexusTheme.TextMuted,
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeActionCard(
    title: String,
    description: String,
    accent: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (hovered) 10.dp else 4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clickable(onClick = onClick),
        color = NexusTheme.CardBg,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.35f)))),
            )
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                icon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Bold,
                            color = NexusTheme.TextPrimary,
                        ),
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.body2.copy(color = NexusTheme.TextSecondary),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLinksRow(
    onAbout: () -> Unit,
    onWhatsNew: () -> Unit,
    onDocs: () -> Unit,
    onRepo: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickLinkChip("About Nexus", onAbout)
        QuickLinkChip("What's new", onWhatsNew)
        QuickLinkChip("Docs", onDocs)
        QuickLinkChip("GitHub", onRepo)
    }
}

@Composable
private fun QuickLinkChip(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(NexusTheme.SurfaceBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        style = MaterialTheme.typography.caption.copy(
            color = NexusTheme.FlamingoPink,
            fontWeight = FontWeight.Medium,
        ),
    )
}

/** Single About dialog for the Home hub (do not redeclare in Welcome/Dashboard aliases). */
@Composable
fun AboutNexusDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        backgroundColor = NexusTheme.CardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FlamingoIcon(modifier = Modifier.size(36.dp))
                Text(
                    text = "About ${NexusBranding.FRAMEWORK_NAME}",
                    color = NexusTheme.TextPrimary,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = NexusBranding.versionLabel(),
                    color = NexusTheme.BrandPurple,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Open-source scaffolder and Compose Desktop client for building cross-platform native apps with C++20 modules, Lua, Python, and TypeScript/XHTML surfaces.",
                    color = NexusTheme.TextSecondary,
                    lineHeight = 20.sp,
                )
                Text(
                    text = "Open generated apps from the home hub or import Langflow JSON to edit flows as blueprint nodes. Runtime stubs stay disabled until you enable them.",
                    color = NexusTheme.TextSecondary,
                    lineHeight = 20.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NexusTheme.BrandPurple)
            }
        },
    )
}

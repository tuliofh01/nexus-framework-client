package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.model.DebuggerService
import nexus.opensource.framework.util.NativeFileDialogs
import java.nio.file.Files

/**
 * Debugger panel that displays regex-based pattern matches from log scanning.
 * Shows each detected issue with severity, category, and the matched line.
 */
@Composable
fun DebuggerPanel(debugger: DebuggerService, onBack: () -> Unit = {}) {
    val state by debugger.state.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var exportPreview by remember { mutableStateOf<String?>(null) }

    // CUSTOMIZE: wire filter rules — category/severity/label substring filters
    var filterCategory by remember { mutableStateOf<DebuggerService.Category?>(null) }
    var filterSeverity by remember { mutableStateOf<DebuggerService.Severity?>(null) }
    val filteredLog = remember(state.log, filterCategory, filterSeverity) {
        state.log.filter { match ->
            (filterCategory == null || match.pattern.category == filterCategory) &&
                (filterSeverity == null || match.pattern.severity == filterSeverity)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Debugger",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${filteredLog.size} match(es)",
                    style = MaterialTheme.typography.caption,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled", fontSize = 11.sp)
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { debugger.toggleEnabled() },
                        modifier = Modifier.height(24.dp),
                    )
                }
                TextButton(onClick = onBack) { Text("\u2190 Back") }
                Button(
                    onClick = { debugger.clearLog() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text("Clear", fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Log input + scan
        Text("Paste log or source to scan", style = MaterialTheme.typography.subtitle2)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 120.dp),
            placeholder = { Text("println(\"debug\"); TODO(); password = \"secret\" …") },
            textStyle = MaterialTheme.typography.body2.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    debugger.clearLog()
                    val lines = inputText.lines().filter { it.isNotBlank() }
                    if (lines.isNotEmpty()) {
                        debugger.scanLines(lines)
                    } else if (inputText.isNotBlank()) {
                        debugger.scan(inputText)
                    }
                },
                enabled = state.enabled && inputText.isNotBlank(),
            ) {
                Text("Scan")
            }
            OutlinedButton(
                onClick = { inputText = "" },
                enabled = inputText.isNotBlank(),
            ) {
                Text("Clear input")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Skeleton: filter rules & severity toggles (minimal wiring — expand in CUSTOMIZE blocks)
        DebuggerFilterBar(
            filterCategory = filterCategory,
            filterSeverity = filterSeverity,
            onCategoryChange = { filterCategory = it },
            onSeverityChange = { filterSeverity = it },
            onClearFilters = {
                filterCategory = null
                filterSeverity = null
            },
        )

        Spacer(Modifier.height(8.dp))

        // Pattern legend (collapsible)
        var showPatterns by remember { mutableStateOf(false) }
        TextButton(onClick = { showPatterns = !showPatterns }) {
            Text(if (showPatterns) "Hide patterns ▲" else "Show patterns ▼", fontSize = 12.sp)
        }

        if (showPatterns) {
            PatternList(debugger)
        }

        Spacer(Modifier.height(8.dp))

        DebuggerExportSection(
            onExportPreview = {
                exportPreview = debugger.exportSummary()
            },
            onExportToFile = {
                val summary = debugger.exportSummary()
                exportPreview = summary
                val path = NativeFileDialogs.pickSaveFile(
                    "Save debugger export",
                    "nexus-debug-export.txt",
                )
                if (path != null) {
                    runCatching {
                        Files.writeString(path, summary)
                    }.onFailure {
                        exportPreview = "Export failed: ${it.message}\n\n$summary"
                    }
                }
            },
            exportPreview = exportPreview,
            onDismissExport = { exportPreview = null },
        )

        Spacer(Modifier.height(8.dp))

        // Matches list
        if (filteredLog.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.log.isEmpty()) "No issues detected — paste content and Scan"
                        else "No matches for current filters",
                        style = MaterialTheme.typography.body1.copy(color = Color.Gray),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${debugger.allPatterns.size} active patterns",
                        style = MaterialTheme.typography.caption.copy(color = Color.Gray),
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(filteredLog.reversed()) { match ->
                    MatchCard(match)
                }
            }
        }
    }
}

/** CUSTOMIZE: extend with label regex, exclude rules, saved filter presets. */
@Composable
private fun DebuggerFilterBar(
    filterCategory: DebuggerService.Category?,
    filterSeverity: DebuggerService.Severity?,
    onCategoryChange: (DebuggerService.Category?) -> Unit,
    onSeverityChange: (DebuggerService.Severity?) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Filters (skeleton)", style = MaterialTheme.typography.caption, color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Severity:", fontSize = 11.sp)
            DebuggerService.Severity.entries.forEach { severity ->
                val selected = filterSeverity == severity
                // CUSTOMIZE: swap for Material3 FilterChip when migrating themes
                DebuggerFilterToggle(
                    label = severity.name,
                    selected = selected,
                    onClick = { onSeverityChange(if (selected) null else severity) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Category:", fontSize = 11.sp)
            // TODO: replace with dropdown when many categories — stub chips for common ones
            listOf(
                DebuggerService.Category.Security,
                DebuggerService.Category.NullSafety,
                DebuggerService.Category.Performance,
            ).forEach { category ->
                val selected = filterCategory == category
                DebuggerFilterToggle(
                    label = category.name,
                    selected = selected,
                    onClick = { onCategoryChange(if (selected) null else category) },
                )
            }
            TextButton(onClick = onClearFilters, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text("Clear", fontSize = 10.sp)
            }
        }
    }
}

/** Minimal Material filter toggle — CUSTOMIZE: Material3 FilterChip or custom Chip. */
@Composable
private fun DebuggerFilterToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFF3A3A5C) else Color(0xFF2A2A3A)
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = bg,
            contentColor = if (selected) Color(0xFF6BCBFF) else Color(0xFFD0D0D0),
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
    ) {
        Text(label, fontSize = 10.sp)
    }
}

/** Export scan summary to preview and/or a file on disk. */
@Composable
private fun DebuggerExportSection(
    onExportPreview: () -> Unit,
    onExportToFile: () -> Unit,
    exportPreview: String?,
    onDismissExport: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onExportPreview) {
            Text("Preview summary", fontSize = 11.sp)
        }
        OutlinedButton(onClick = onExportToFile) {
            Text("Save to file…", fontSize = 11.sp)
        }
    }
    exportPreview?.let { summary ->
        Spacer(Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(max = 100.dp),
            backgroundColor = Color(0xFF1E1E2E),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Export preview", fontSize = 10.sp, color = Color.Gray)
                    TextButton(onClick = onDismissExport, contentPadding = PaddingValues(0.dp)) {
                        Text("Dismiss", fontSize = 10.sp)
                    }
                }
                Text(
                    summary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun PatternList(debugger: DebuggerService) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Active patterns:", style = MaterialTheme.typography.subtitle2)
        for (pattern in debugger.allPatterns) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeverityDot(pattern.severity)
                    Text(pattern.label, fontSize = 12.sp)
                }
                Text(
                    text = "${pattern.category}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun MatchCard(match: DebuggerService.DebugMatch) {
    val bgColor = when (match.pattern.severity) {
        DebuggerService.Severity.Error -> Color(0xFF3D1A1A)
        DebuggerService.Severity.Warning -> Color(0xFF3D3A1A)
        DebuggerService.Severity.Info -> Color(0xFF1A2D3D)
    }
    val textColor = when (match.pattern.severity) {
        DebuggerService.Severity.Error -> Color(0xFFFF6B6B)
        DebuggerService.Severity.Warning -> Color(0xFFFFD93D)
        DebuggerService.Severity.Info -> Color(0xFF6BCBFF)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        backgroundColor = bgColor,
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    SeverityDot(match.pattern.severity)
                    Text(
                        text = match.pattern.label,
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                        ),
                    )
                }
                Text(
                    text = match.pattern.category.name,
                    fontSize = 10.sp,
                    color = Color.Gray,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = match.matchedGroup,
                fontSize = 11.sp,
                color = Color(0xFFD0D0D0),
                maxLines = 2,
            )
            Text(
                text = match.line.take(120),
                fontSize = 10.sp,
                color = Color(0xFF808080),
                maxLines = 1,
            )
            Text(
                text = match.pattern.description,
                fontSize = 10.sp,
                color = Color(0xFF707070),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SeverityDot(severity: DebuggerService.Severity) {
    val color = when (severity) {
        DebuggerService.Severity.Error -> Color(0xFFFF4444)
        DebuggerService.Severity.Warning -> Color(0xFFFFAA00)
        DebuggerService.Severity.Info -> Color(0xFF4488FF)
    }
    Box(
        modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(50)),
    )
}

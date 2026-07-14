package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.model.DebuggerService

/**
 * Debugger panel that displays regex-based pattern matches from log scanning.
 * Shows each detected issue with severity, category, and the matched line.
 */
@Composable
fun DebuggerPanel(debugger: DebuggerService, onBack: () -> Unit = {}) {
    val state by debugger.state.collectAsState()

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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${state.log.size} match(es)",
                    style = MaterialTheme.typography.caption,
                )
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

        // Pattern legend (collapsible)
        var showPatterns by remember { mutableStateOf(false) }
        TextButton(onClick = { showPatterns = !showPatterns }) {
            Text(if (showPatterns) "Hide patterns ▲" else "Show patterns ▼", fontSize = 12.sp)
        }

        if (showPatterns) {
            PatternList(debugger)
        }

        Spacer(Modifier.height(8.dp))

        // Matches list
        if (state.log.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No issues detected",
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
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.log.reversed()) { match ->
                    MatchCard(match)
                }
            }
        }
    }
}

@Composable
private fun PatternList(debugger: DebuggerService) {
    val state by debugger.state.collectAsState()

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

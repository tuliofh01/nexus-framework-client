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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nexus.opensource.framework.model.TestRunner

private val CardBg = Color(0xFF1F2B47)
private val DarkBg = Color(0xFF1A1A2E)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9090A0)
private val PassGreen = Color(0xFF00E676)
private val FailRed = Color(0xFFFF5252)

@Composable
fun TestRunnerPanel(
    runner: TestRunner,
    onBack: () -> Unit = {},
) {
    val state by runner.state.collectAsState()
    val scope = rememberCoroutineScope()

    // Register built-in tests once on first composition
    LaunchedEffect(runner) {
        runner.registerBuiltinTests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Test Runner",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onBack) {
                    Text("\u2190 Back", color = TextSecondary)
                }
            }
        }

        // Summary bar
        val s = state.summary
        val passRatio = if (s.total > 0) s.passed.toFloat() / s.total.toFloat() else 1f
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "${s.passed} / ${s.total} passed",
                    style = MaterialTheme.typography.h6.copy(
                        color = if (s.failed == 0) PassGreen else FailRed,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "${s.totalDurationMs}ms total",
                    style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                )
            }
            LinearProgressIndicator(
                progress = passRatio,
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (s.failed == 0) PassGreen else FailRed,
                backgroundColor = Color(0xFF2D2D4A),
            )
        }

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { scope.launch { runner.runAll() } },
                enabled = !state.isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
            ) {
                Text(
                    if (state.isRunning) "Running..." else "Run All",
                    color = Color.White,
                )
            }
            Button(
                onClick = { runner.clearResults() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF424242)),
            ) {
                Text("Clear Results", color = TextPrimary)
            }
            Button(
                onClick = { runner.clearAll() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4A1A1A)),
            ) {
                Text("Reset All", color = Color(0xFFFF6B6B))
            }
        }

        // Running indicator
        if (state.isRunning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = Color(0xFF6C63FF),
                backgroundColor = Color(0xFF2D2D4A),
            )
        }

        // Test results list
        if (state.runs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No tests run yet",
                        style = MaterialTheme.typography.body1.copy(color = TextSecondary),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${runner.state.value.summary.total} registered test(s) ready",
                        style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.runs.reversed(), key = { "${it.timestamp}_${it.testName}" }) { run ->
                    val passed = run.result.passed
                    val cardColor = if (passed) Color(0xFF1B3A2A) else Color(0xFF3D1A1A)
                    val accentColor = if (passed) PassGreen else FailRed

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = cardColor,
                        elevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Status badge
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        accentColor.copy(alpha = 0.2f),
                                        RoundedCornerShape(50),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (passed) "\u2713" else "\u2717",
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = run.testName,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = run.category,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                    )
                                    Text(
                                        text = "${run.result.durationMs}ms",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                    )
                                }
                            }

                            if (!passed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(FailRed.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = run.result.message.take(60),
                                        color = FailRed,
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

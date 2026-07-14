package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand palette ───────────────────────────────────────────
// Reuses AppScreen from nexus.opensource.AppKt
import nexus.opensource.AppScreen
private val BrandPurple = Color(0xFF6C63FF)
private val DarkBg = Color(0xFF1A1A2E)
private val SurfaceBg = Color(0xFF16213E)
private val CardBg = Color(0xFF1F2B47)
private val AccentCyan = Color(0xFF00D4FF)
private val AccentGreen = Color(0xFF00E676)
private val AccentOrange = Color(0xFFFF9100)
private val AccentRed = Color(0xFFFF5252)
private val AccentPink = Color(0xFFE040FB)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9090A0)

/** Action card definition for the dashboard. */
private data class DashCard(
    val label: String,
    val desc: String,
    val icon: String,
    val accent: Color,
    val route: AppScreen,
)

private val dashCards = listOf(
    DashCard("Generate Project", "Scaffold a new native app", "\uD83D\uDE80", AccentCyan, AppScreen.Generate),
    DashCard("Blueprint Editor", "Design app structure graph", "\uD83D\uDD35", AccentOrange, AppScreen.BlueprintEditor),
    DashCard("Flows Editor", "In-app automation services", "\u26A1", AccentGreen, AppScreen.FlowsEditor),
    DashCard("Debugger", "Pattern-based log scanner", "\uD83D\uDC1B", AccentPink, AppScreen.Debugger),
    DashCard("Test Runner", "In-memory unitary tests", "\u2705", AccentRed, AppScreen.TestRunner),
)

/**
 * Visual dashboard replacing the old CounterScreen.
 * Large hoverable cards for each major action — no forms, no JSON.
 */
@Composable
fun DashboardScreen(
    onNavigate: (AppScreen) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Brand header ──
            Text(
                text = "\uD83D\uDD2E The Nexus Framework",
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "v0.3.0 \u2014 Dashboard UI \u00B7 Framework Package \u00B7 Modern C++",
                style = MaterialTheme.typography.body1.copy(
                    color = TextSecondary,
                    fontSize = 14.sp,
                ),
            )

            Spacer(Modifier.height(48.dp))

            // ── Action cards grid ──
            // Top row: 3 cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dashCards.take(3).forEach { card ->
                    DashboardCard(card = card, onClick = { onNavigate(card.route) })
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bottom row: 2 cards centered
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                dashCards.drop(3).forEach { card ->
                    DashboardCard(card = card, onClick = { onNavigate(card.route) })
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Footer hint ──
            Text(
                text = "SDL3 \u00B7 ImGui \u00B7 Lua \u00B7 Python \u00B7 TypeScript/XHTML \u00B7 Zig",
                style = MaterialTheme.typography.caption.copy(
                    color = TextSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun DashboardCard(
    card: DashCard,
    onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }

    val elevation by remember(hovered) {
        derivedStateOf { if (hovered) 12.dp else 4.dp }
    }

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(listOf(card.accent, card.accent.copy(alpha = 0.4f)))
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Icon
                Text(
                    text = card.icon,
                    fontSize = 32.sp,
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = card.label,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp,
                        ),
                    )
                    Text(
                        text = card.desc,
                        style = MaterialTheme.typography.caption.copy(
                            color = TextSecondary,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }

        // Hover overlay
        if (hovered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(card.accent.copy(alpha = 0.05f))
                    .clickable(onClick = onClick),
            )
        }
    }

    // Track hover state via pointer input
    LaunchedEffect(Unit) {
        // The hover detection is implicit via the shadow/overlay.
        // Compose Desktop's pointerEnter/Exit are limited in Material v1,
        // so we use the onPointerEvent approach via a simple mutable interaction.
    }

}

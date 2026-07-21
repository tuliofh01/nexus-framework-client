package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import nexus.opensource.framework.controller.LoadingController
import nexus.opensource.framework.core.model.NexusBranding

/**
 * Loading/splash screen shown before the main UI.
 * Displays animated initialization steps with a progress bar.
 * Auto-transitions when [onComplete] fires.
 */
@Composable
fun LoadingScreen(
    controller: LoadingController = remember { LoadingController() },
    onComplete: () -> Unit = {},
) {
    val state by controller.state.collectAsState()

    LaunchedEffect(Unit) {
        controller.runSequence()
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            delay(300L)
            onComplete()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NexusTheme.DarkBg,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedFlamingoLogo(
                modifier = Modifier.size(112.dp),
                style = FlamingoAnimationStyle.Loading,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = NexusBranding.FRAMEWORK_NAME,
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.TextPrimary,
                    fontSize = 32.sp,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = NexusBranding.versionLabel(),
                style = MaterialTheme.typography.body1.copy(
                    color = NexusTheme.TextSecondary,
                    fontSize = 14.sp,
                ),
            )

            Spacer(Modifier.height(48.dp))

            val progress = if (state.totalSteps > 0) {
                state.currentStep.toFloat() / state.totalSteps.toFloat()
            } else {
                0f
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 360.dp),
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = NexusTheme.BrandPurple,
                    backgroundColor = NexusTheme.ProgressTrack,
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = state.message,
                    style = MaterialTheme.typography.body2.copy(
                        color = NexusTheme.TextPrimary.copy(alpha = 0.82f),
                        fontSize = 14.sp,
                    ),
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in 0 until state.totalSteps) {
                    val isActive = i < state.currentStep
                    val isCurrent = i == state.currentStep - 1 && !state.isComplete
                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 14.dp else 10.dp)
                            .background(
                                color = when {
                                    isActive && state.isComplete -> NexusTheme.AccentGreen
                                    isActive -> NexusTheme.BrandPurple
                                    else -> NexusTheme.Divider
                                },
                                shape = RoundedCornerShape(50),
                            ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Zig bootstrap · Cross-platform services · Code generation pipeline",
                style = MaterialTheme.typography.caption.copy(
                    color = NexusTheme.TextMuted,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

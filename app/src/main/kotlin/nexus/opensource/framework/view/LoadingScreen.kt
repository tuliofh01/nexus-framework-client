package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nexus.opensource.framework.controller.LoadingController
import nexus.opensource.framework.model.NexusBranding

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
    val scope = rememberCoroutineScope()

    // Start the loading sequence on first composition
    LaunchedEffect(Unit) {
        controller.runSequence()
    }

    // Transition when loading completes
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            delay(300L)
            onComplete()
        }
    }

    val brandColor = Color(0xFF6C63FF) // Nexus brand purple
    val bgColor = Color(0xFF1A1A2E)    // Dark background

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Title
            Text(
                text = "The Nexus Framework",
                style = MaterialTheme.typography.h3.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 32.sp,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "v0.3.0 — Dashboard UI · Framework Package · Modern C++",
                style = MaterialTheme.typography.body1.copy(
                    color = Color(0xFFB0B0B0),
                    fontSize = 14.sp,
                ),
            )

            Spacer(Modifier.height(48.dp))

            // Progress bar
            val progress = if (state.totalSteps > 0)
                state.currentStep.toFloat() / state.totalSteps.toFloat()
            else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 360.dp),
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = brandColor,
                    backgroundColor = Color(0xFF2D2D4A),
                )

                Spacer(Modifier.height(16.dp))

                // Current step label
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.body2.copy(
                        color = Color(0xFFD0D0D0),
                        fontSize = 14.sp,
                    ),
                )
            }

            Spacer(Modifier.height(32.dp))

            // Step indicators
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
                                    isActive && state.isComplete -> Color(0xFF00C853)
                                    isActive -> brandColor
                                    else -> Color(0xFF3A3A5C)
                                },
                                shape = RoundedCornerShape(50),
                            ),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Subtitle
            Text(
                text = "Zig bootstrap · Cross-platform services · Code generation pipeline",
                style = MaterialTheme.typography.caption.copy(
                    color = Color(0xFF707070),
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

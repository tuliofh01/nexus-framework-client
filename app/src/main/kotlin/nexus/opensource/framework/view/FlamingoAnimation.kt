package nexus.opensource.framework.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

enum class FlamingoAnimationStyle {
    Idle,
    Loading,
    Transition,
}

@Composable
fun AnimatedFlamingoLogo(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    style: FlamingoAnimationStyle = FlamingoAnimationStyle.Idle,
    tint: Color = NexusTheme.FlamingoPink,
) {
    val resolvedModifier = if (size != null) modifier.size(size) else modifier
    val infinite = rememberInfiniteTransition(label = "flamingo")

    val wingFlap by infinite.animateFloat(
        initialValue = 0f,
        targetValue = when (style) {
            FlamingoAnimationStyle.Loading -> 1f
            FlamingoAnimationStyle.Transition -> 0.6f
            FlamingoAnimationStyle.Idle -> 0.25f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (style) {
                    FlamingoAnimationStyle.Loading -> 420
                    FlamingoAnimationStyle.Transition -> 320
                    FlamingoAnimationStyle.Idle -> 1400
                },
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wingFlap",
    )

    val neckSway by infinite.animateFloat(
        initialValue = -0.4f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (style == FlamingoAnimationStyle.Loading) 900 else 2200,
                easing = EaseInOutSine,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neckSway",
    )

    val bodyBob by infinite.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bodyBob",
    )

    val legPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (style == FlamingoAnimationStyle.Loading) 600 else 2400,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "legPhase",
    )

    val pose = FlamingoPose(
        wingFlap = wingFlap,
        neckSway = neckSway,
        bodyBob = bodyBob,
        legPhase = if (style == FlamingoAnimationStyle.Idle) 0f else legPhase,
    )

    Box(modifier = resolvedModifier, contentAlignment = Alignment.Center) {
        if (style == FlamingoAnimationStyle.Transition) {
            FlamingoIcon(
                modifier = Modifier.fillMaxSize().alpha(0.25f),
                tint = NexusTheme.BrandPurple,
                pose = pose.copy(wingFlap = wingFlap * 0.5f),
            )
        }
        FlamingoIcon(
            modifier = Modifier.fillMaxSize(),
            tint = tint,
            pose = pose,
        )
    }
}

@Composable
fun FlamingoLoadingIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    message: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedFlamingoLogo(
            modifier = Modifier.size(64.dp),
            style = FlamingoAnimationStyle.Loading,
        )
        if (progress != null) {
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth().height(4.dp),
                color = NexusTheme.BrandPurple,
                backgroundColor = NexusTheme.ProgressTrack,
            )
        }
        if (message != null) {
            Text(
                text = message,
                color = NexusTheme.TextSecondary,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun FlamingoTransitionOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.85f, animationSpec = tween(220)),
        exit = fadeOut(tween(160)) + scaleOut(targetScale = 1.05f, animationSpec = tween(180)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedFlamingoLogo(
                modifier = Modifier.size(72.dp),
                style = FlamingoAnimationStyle.Transition,
            )
        }
    }
}

@Composable
fun AnimatedFlamingoCreationIcon(
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "creation")
    val sparkle by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "sparkle",
    )
    FlamingoCreationIcon(modifier = modifier, sparkleAlpha = sparkle)
}

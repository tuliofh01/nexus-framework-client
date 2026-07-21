package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import nexus.opensource.framework.core.model.NexusBranding

@Composable
fun WhatsNewDialog(onDismiss: () -> Unit) {
    var dismissEnabled by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(6) }
    val version = NexusBranding.FRAMEWORK_VERSION

    LaunchedEffect(Unit) {
        for (i in 6 downTo 1) {
            secondsLeft = i
            delay(1000L)
        }
        secondsLeft = 0
        dismissEnabled = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = dismissEnabled, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .heightIn(max = 640.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = false) { },
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = NexusTheme.CardBg,
                shape = RoundedCornerShape(20.dp),
                elevation = 24.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "What's New",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.Bold,
                            color = NexusTheme.TextPrimary,
                            fontSize = 22.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Version $version",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Light,
                            color = NexusTheme.BrandPurple,
                            fontSize = 16.sp,
                        ),
                    )

                    Spacer(Modifier.height(20.dp))

                    AnimatedFlamingoLogo(
                        size = 80.dp,
                        style = FlamingoAnimationStyle.Idle,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = NexusBranding.SUBTITLE,
                        style = MaterialTheme.typography.body2.copy(
                            color = NexusTheme.TextSecondary,
                            fontSize = 11.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(24.dp))

                    Divider(color = NexusTheme.Divider, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    RepoPleaSection()

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (dismissEnabled) NexusTheme.BrandPurple else NexusTheme.BrandPurple.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledBackgroundColor = NexusTheme.BrandPurple.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f),
                        ),
                        enabled = dismissEnabled,
                        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = if (dismissEnabled) "Got it!" else "Hold on... $secondsLeft s",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RepoPleaSection() {
    Text(
        text = "Nexus Framework is open source and built by the community.",
        style = MaterialTheme.typography.body1.copy(
            color = NexusTheme.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(16.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.BrandPurple.copy(alpha = 0.15f),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val blinkAlpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse,
                ),
            )

            Text(
                text = "Star & Share",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.BrandPurple.copy(alpha = blinkAlpha),
                    fontSize = 18.sp,
                ),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = NexusBranding.REPO_URL,
                style = MaterialTheme.typography.body2.copy(
                    color = NexusTheme.FlamingoPink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.clickable {
                    java.awt.Desktop.getDesktop().browse(java.net.URI(NexusBranding.REPO_URL))
                },
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Every star and share helps more developers discover the framework. Thank you!",
                style = MaterialTheme.typography.body2.copy(
                    color = NexusTheme.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

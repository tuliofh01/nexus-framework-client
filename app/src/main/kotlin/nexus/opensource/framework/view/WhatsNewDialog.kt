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

private const val VERSION = "1.0.1"
private val FlamingoPink = Color(0xFFF38BA8)
private val BrandPurple = Color(0xFF6C63FF)
private val CardBg = Color(0xFF1F2B47)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9090A0)
private val DividerColor = Color(0xFF3A3A5C)
private val AccentGreen = Color(0xFF00E676)

@Composable
fun WhatsNewDialog(onDismiss: () -> Unit) {
    var dismissEnabled by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(6) }

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
                backgroundColor = CardBg,
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
                        text = "✨ What's New",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 22.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Version $VERSION",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Light,
                            color = BrandPurple,
                            fontSize = 16.sp,
                        ),
                    )

                    Spacer(Modifier.height(20.dp))

                    FlamingoIcon(
                        modifier = Modifier.size(80.dp),
                        tint = FlamingoPink,
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "C++20 Modernization · Module Consolidation · Zig JNI Bridge",
                        style = MaterialTheme.typography.body2.copy(
                            color = TextSecondary,
                            fontSize = 11.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(24.dp))

                    Divider(color = DividerColor, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    RepoPleaSection()

                    Spacer(Modifier.height(28.dp))
                    Divider(color = DividerColor, thickness = 1.dp)
                    Spacer(Modifier.height(20.dp))

                    SalesPitchSection()

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (dismissEnabled) BrandPurple else BrandPurple.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledBackgroundColor = BrandPurple.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f),
                        ),
                        enabled = dismissEnabled,
                        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp),
                    ) {
                        Text(
                            text = if (dismissEnabled) "Got it! \uD83D\uDE80" else "Hold on... $secondsLeft s",
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
        text = "❤️ Nexus Framework is open source and built by the community.",
        style = MaterialTheme.typography.body1.copy(
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(16.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = BrandPurple.copy(alpha = 0.15f),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val blinkAlpha by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse,
                ),
            )

            Text(
                text = "⭐ Star & Share",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandPurple.copy(alpha = blinkAlpha),
                    fontSize = 18.sp,
                ),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "https://github.com/anomalyco/Framework",
                style = MaterialTheme.typography.body2.copy(
                    color = FlamingoPink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.clickable {
                    java.awt.Desktop.getDesktop().browse(
                        java.net.URI("https://github.com/anomalyco/Framework")
                    )
                },
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Liking and sharing the repo helps more people discover it. As an open-source project, your support is what keeps new updates coming. Every star and share matters \u2014 thank you! \uD83D\uDE80",
                style = MaterialTheme.typography.body2.copy(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SalesPitchSection() {
    val bodyColor = TextSecondary.copy(alpha = 0.92f)
    val headingColor = TextPrimary
    val accentColor = AccentGreen

    Text(
        text = "22 self-contained C++20 modules, zero boilerplate \u2014 every module is its own documentation.",
        style = MaterialTheme.typography.body1.copy(
            color = bodyColor,
            fontSize = 14.sp,
        ),
        textAlign = TextAlign.Start,
    )

    Spacer(Modifier.height(20.dp))

    FeatureBlock(
        emoji = "\uD83E\uDDBA",
        title = "Modern C++20 Throughout",
        body = "Every non-modular .cpp now uses trailing return types, [[nodiscard]] on every getter, constexpr on compile-time constants, noexcept on move operations, and std::ranges::copy replacing raw memcpy. The generated code looks like a CppCon talk \u2014 and compiles with the same guarantees on every platform.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    FeatureBlock(
        emoji = "\uD83D\uDCE6",
        title = "Self-Contained Modules",
        body = "All 7 shared runtime modules (font config, theme, paths, script archive, crypto, protection, zig allocator) are now single .cppm files \u2014 no .hpp, no separate .cpp, no module implementation file. One file, one module, one place to edit. Each includes 40\u201360 lines of educational comments explaining the architecture decisions, so you learn why \u2014 not just what.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    FeatureBlock(
        emoji = "\uD83E\uDDF9",
        title = "RAII Resource Management",
        body = "SDL_Window and SDL_GLContext are now managed through std::unique_ptr with custom deleters. Resources clean up automatically when they go out of scope \u2014 no manual SDL_DestroyWindow calls, no goto cleanup labels, no resource leaks. The cleanup path is the same no matter how your code branches.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    FeatureBlock(
        emoji = "\uD83D\uDD17",
        title = "Simplified Build Files",
        body = "Both build.zig files (desktop and Android) have been cleaned up: shared_module_impl_sources removed since all runtime modules are now self-contained. The standalone pack_archive tool (builds lua.dat / python.dat) keeps its legacy files \u2014 we didn't break it, we isolated it properly.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    FeatureBlock(
        emoji = "\uD83D\uDC9B",
        title = "Stale Files Purged",
        body = "7 obsolete files deleted \u2014 module implementation .cpp files and orphaned legacy pairs that no longer had matching headers. The shared runtime directory went from 17 entries to 12 clean, self-documenting files.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    FeatureBlock(
        emoji = "\u26A1",
        title = "Pure-Zig JNI Bridge",
        body = "Android\u2019s JNI bridge rewritten from 7 C++ files (jni_bridge.cpp, app_core.cpp/.hpp, NativePythonBridge.cpp/.hpp, python_bridge.hpp, eval_result.hpp) into 1 pure-Zig file (python_bridge.zig). Call chain simplified from 5 layers (Kotlin \u2192 Djinni \u2192 C++ singleton \u2192 virtual dispatch \u2192 JNI) to 3 (Kotlin \u2192 Zig C ABI \u2192 JNI). Zig exports 5 C ABI functions with explicit null-safety and manually managed memory (std.c.malloc + zig_free_*). No virtual dispatch, no shared_ptr, no inheritance.",
        headingColor = headingColor,
        bodyColor = bodyColor,
        accentColor = accentColor,
    )

    Spacer(Modifier.height(16.dp))
    Divider(color = DividerColor, thickness = 1.dp)
    Spacer(Modifier.height(16.dp))

    Text(
        text = "\u201CThe generated code wouldn\u2019t look out of place in a CppCon talk. Every file is self-documenting, every module is self-contained, every resource is RAII-managed.\u201D",
        style = MaterialTheme.typography.body2.copy(
            color = BrandPurple.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        ),
        textAlign = TextAlign.Center,
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = "v$VERSION \u2014 stable, documented, and ready to ship.",
        style = MaterialTheme.typography.body2.copy(
            color = TextSecondary,
            fontSize = 11.sp,
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FeatureBlock(
    emoji: String,
    title: String,
    body: String,
    headingColor: Color,
    bodyColor: Color,
    accentColor: Color,
) {
    Spacer(Modifier.height(16.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2.copy(
                fontWeight = FontWeight.Bold,
                color = headingColor,
                fontSize = 15.sp,
            ),
        )
    }

    Spacer(Modifier.height(6.dp))

    Text(
        text = body,
        style = MaterialTheme.typography.body2.copy(
            color = bodyColor,
            fontSize = 13.sp,
            lineHeight = 20.sp,
        ),
        textAlign = TextAlign.Start,
    )
}

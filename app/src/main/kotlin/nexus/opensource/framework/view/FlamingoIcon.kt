package nexus.opensource.framework.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sin

data class FlamingoPose(
    val wingFlap: Float = 0f,
    val neckSway: Float = 0f,
    val bodyBob: Float = 0f,
    val legPhase: Float = 0f,
)

@Composable
fun FlamingoIcon(
    modifier: Modifier = Modifier,
    tint: Color = NexusTheme.FlamingoPink,
    pose: FlamingoPose = FlamingoPose(),
) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height) / 200f
        val cx = size.width / 2f
        val cy = size.height / 2f + pose.bodyBob * s
        drawFlamingo(cx, cy, s, tint, pose)
    }
}

/** Compact flamingo mark for cards and list rows. */
@Composable
fun FlamingoGlyph(
    modifier: Modifier = Modifier,
    tint: Color = NexusTheme.FlamingoPink,
) {
    NexusLogoMark(modifier = modifier, tint = tint)
}

@Composable
fun FlamingoCreationIcon(
    modifier: Modifier = Modifier,
    tint: Color = NexusTheme.FlamingoPink,
    sparkleAlpha: Float = 1f,
) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height) / 200f
        val cx = size.width / 2f - 8f * s
        val cy = size.height / 2f + 4f * s
        drawFlamingo(cx, cy, s * 0.88f, tint, FlamingoPose(wingFlap = 0.35f))

        val badgeCx = cx + 52f * s
        val badgeCy = cy - 58f * s
        drawCircle(NexusTheme.BrandPurple, 14f * s, Offset(badgeCx, badgeCy))
        drawCircle(Color.White.copy(alpha = 0.15f * sparkleAlpha), 14f * s, Offset(badgeCx, badgeCy))
        val stroke = 2.5f * s
        drawLine(Color.White, Offset(badgeCx - 6f * s, badgeCy), Offset(badgeCx + 6f * s, badgeCy), stroke)
        drawLine(Color.White, Offset(badgeCx, badgeCy - 6f * s), Offset(badgeCx, badgeCy + 6f * s), stroke)

        if (sparkleAlpha > 0f) {
            drawCircle(
                NexusTheme.AccentCyan.copy(alpha = 0.8f * sparkleAlpha),
                3f * s,
                Offset(badgeCx + 18f * s, badgeCy - 14f * s),
            )
        }
    }
}

/** Alias used by Home action cards. */
@Composable
fun NexusCreationIcon(modifier: Modifier = Modifier) {
    AnimatedFlamingoCreationIcon(modifier = modifier)
}

@Composable
fun NexusLogoMark(
    modifier: Modifier = Modifier,
    tint: Color = NexusTheme.FlamingoPink,
) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height) / 120f
        val cx = size.width / 2f
        val cy = size.height / 2f + 10f * s
        drawFlamingoHeadNeck(cx, cy, s * 1.1f, tint)
    }
}

fun renderFlamingoImageBitmap(size: Int = 128): ImageBitmap {
    val bitmap = ImageBitmap(size, size)
    androidx.compose.ui.graphics.drawscope.CanvasDrawScope().draw(
        Density(1f),
        LayoutDirection.Ltr,
        androidx.compose.ui.graphics.Canvas(bitmap),
        androidx.compose.ui.geometry.Size(size.toFloat(), size.toFloat()),
    ) {
        val s = size / 200f
        drawFlamingo(size / 2f, size / 2f + 4f, s, NexusTheme.FlamingoPink, FlamingoPose(wingFlap = 0.2f))
    }
    return bitmap
}

internal fun DrawScope.drawFlamingo(
    cx: Float,
    cy: Float,
    s: Float,
    tint: Color,
    pose: FlamingoPose = FlamingoPose(),
) {
    val pink = tint
    val light = NexusTheme.FlamingoLight
    val wing = NexusTheme.FlamingoWing
    val beak = NexusTheme.BeakOrange
    val eye = Color(0xFF1E1E2E)

    val neckOffset = pose.neckSway * 6f * s
    val legSwing = sin(pose.legPhase * Math.PI.toFloat() * 2f) * 8f * s

    drawLine(
        pink,
        Offset(cx - 8f * s + legSwing * 0.3f, cy + 30f * s),
        Offset(cx - 12f * s + legSwing, cy + 80f * s),
        strokeWidth = 3.5f * s,
    )
    drawLine(
        pink,
        Offset(cx + 8f * s - legSwing * 0.3f, cy + 27f * s),
        Offset(cx + 12f * s - legSwing, cy + 80f * s),
        strokeWidth = 3.5f * s,
    )

    withTransform({ translate(neckOffset, 0f) }) {
        val body = Path().apply {
            moveTo(cx - 30f * s, cy + 25f * s)
            cubicTo(cx - 45f * s, cy + 20f * s, cx - 50f * s, cy, cx - 40f * s, cy - 10f * s)
            cubicTo(cx - 30f * s, cy - 20f * s, cx, cy - 25f * s, cx + 10f * s, cy - 20f * s)
            cubicTo(cx + 20f * s, cy - 18f * s, cx + 30f * s, cy - 30f * s, cx + 28f * s, cy - 40f * s)
            cubicTo(cx + 26f * s, cy - 55f * s, cx + 35f * s, cy - 65f * s, cx + 25f * s, cy - 80f * s)
            cubicTo(cx + 20f * s, cy - 88f * s, cx + 12f * s, cy - 93f * s, cx + 8f * s, cy - 98f * s)
            cubicTo(cx + 6f * s, cy - 105f * s, cx + 18f * s, cy - 105f * s, cx + 20f * s, cy - 98f * s)
            cubicTo(cx + 22f * s, cy - 90f * s, cx + 30f * s, cy - 80f * s, cx + 32f * s, cy - 65f * s)
            cubicTo(cx + 35f * s, cy - 50f * s, cx + 40f * s, cy - 38f * s, cx + 30f * s, cy - 22f * s)
            cubicTo(cx + 25f * s, cy - 15f * s, cx + 20f * s, cy - 5f * s, cx + 18f * s, cy + 5f * s)
            cubicTo(cx + 15f * s, cy + 18f * s, cx - 10f * s, cy + 28f * s, cx - 30f * s, cy + 25f * s)
            close()
        }
        drawPath(body, pink, style = Fill)

        val wingPivot = Offset(cx + 5f * s, cy - 5f * s)
        rotate(pose.wingFlap * -18f, wingPivot) {
            val wingPath = Path().apply {
                moveTo(cx - 10f * s, cy - 12f * s)
                cubicTo(cx + 5f * s, cy - 22f * s - pose.wingFlap * 8f * s, cx + 25f * s, cy - 8f * s, cx + 22f * s, cy)
                cubicTo(cx + 18f * s, cy + 5f * s, cx, cy + 8f * s, cx, cy + 5f * s)
                cubicTo(cx - 5f * s, cy, cx - 12f * s, cy - 8f * s, cx - 10f * s, cy - 12f * s)
                close()
            }
            drawPath(wingPath, wing.copy(alpha = 0.65f), style = Fill)
        }

        val tail1 = Path().apply {
            moveTo(cx - 38f * s, cy + 3f * s)
            cubicTo(cx - 55f * s, cy + 8f * s, cx - 48f * s, cy + 20f * s, cx - 45f * s, cy + 18f * s)
            cubicTo(cx - 38f * s, cy + 12f * s, cx - 32f * s, cy + 8f * s, cx - 30f * s, cy + 8f * s)
            close()
        }
        drawPath(tail1, pink, style = Fill)

        val tail2 = Path().apply {
            moveTo(cx - 40f * s, cy)
            cubicTo(cx - 58f * s, cy + 2f * s, cx - 52f * s, cy + 14f * s, cx - 50f * s, cy + 12f * s)
            cubicTo(cx - 44f * s, cy + 6f * s, cx - 36f * s, cy + 3f * s, cx - 34f * s, cy + 3f * s)
            close()
        }
        drawPath(tail2, light.copy(alpha = 0.5f), style = Fill)

        val beakPath = Path().apply {
            moveTo(cx + 22f * s, cy - 96f * s)
            cubicTo(cx + 32f * s, cy - 95f * s, cx + 35f * s, cy - 89f * s, cx + 30f * s, cy - 83f * s)
            cubicTo(cx + 27f * s, cy - 80f * s, cx + 22f * s, cy - 84f * s, cx + 20f * s, cy - 90f * s)
            close()
        }
        drawPath(beakPath, beak, style = Fill)

        drawCircle(eye, 2.5f * s, Offset(cx + 15f * s, cy - 99f * s))
        drawCircle(Color.White.copy(alpha = 0.7f), 1f * s, Offset(cx + 15.5f * s, cy - 99.5f * s))
    }
}

private fun DrawScope.drawFlamingoHeadNeck(cx: Float, cy: Float, s: Float, tint: Color) {
    val beak = NexusTheme.BeakOrange
    val eye = Color(0xFF1E1E2E)

    val neck = Path().apply {
        moveTo(cx - 8f * s, cy + 20f * s)
        cubicTo(cx - 4f * s, cy - 10f * s, cx + 18f * s, cy - 40f * s, cx + 12f * s, cy - 70f * s)
        cubicTo(cx + 10f * s, cy - 82f * s, cx + 16f * s, cy - 88f * s, cx + 14f * s, cy - 92f * s)
        cubicTo(cx + 20f * s, cy - 88f * s, cx + 28f * s, cy - 78f * s, cx + 26f * s, cy - 55f * s)
        cubicTo(cx + 24f * s, cy - 35f * s, cx + 8f * s, cy - 5f * s, cx + 6f * s, cy + 18f * s)
        close()
    }
    drawPath(neck, tint, style = Fill)

    drawCircle(tint, 12f * s, Offset(cx + 10f * s, cy - 95f * s))
    val beakPath = Path().apply {
        moveTo(cx + 18f * s, cy - 93f * s)
        lineTo(cx + 32f * s, cy - 88f * s)
        lineTo(cx + 18f * s, cy - 84f * s)
        close()
    }
    drawPath(beakPath, beak, style = Fill)
    drawCircle(eye, 2f * s, Offset(cx + 14f * s, cy - 97f * s))
}

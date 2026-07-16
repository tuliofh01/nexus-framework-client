package nexus.opensource.framework.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill

private val FlamingoPink = Color(0xFFF38BA8)
private val FlamingoLight = Color(0xFFF5A0B5)
private val FlamingoWing = Color(0xFFEBA0AC)
private val BeakOrange = Color(0xFFFAB387)
private val EyeDark = Color(0xFF1E1E2E)

@Composable
fun FlamingoIcon(
    modifier: Modifier = Modifier,
    tint: Color = FlamingoPink,
) {
    Canvas(modifier = modifier) {
        val s = minOf(size.width, size.height) / 200f
        val cx = size.width / 2f
        val cy = size.height / 2f
        drawFlamingo(cx, cy, s, tint)
    }
}

private fun DrawScope.drawFlamingo(cx: Float, cy: Float, s: Float, tint: Color) {
    val pink = tint
    val light = FlamingoLight
    val wing = FlamingoWing
    val beak = BeakOrange
    val eye = EyeDark

    // Legs
    drawLine(pink, Offset(cx - 8f * s, cy + 30f * s), Offset(cx - 12f * s, cy + 80f * s), strokeWidth = 3.5f * s)
    drawLine(pink, Offset(cx + 8f * s, cy + 27f * s), Offset(cx + 12f * s, cy + 80f * s), strokeWidth = 3.5f * s)

    // Body + neck silhouette (single filled path)
    val body = Path().apply {
        // Start at bottom of body
        moveTo(cx - 30f * s, cy + 25f * s)
        // Bottom curve of body
        cubicTo(cx - 45f * s, cy + 20f * s, cx - 50f * s, cy * s, cx - 40f * s, cy - 10f * s)
        // Back/top of body
        cubicTo(cx - 30f * s, cy - 20f * s, cx * s, cy - 25f * s, cx + 10f * s, cy - 20f * s)
        // Transition to neck
        cubicTo(cx + 20f * s, cy - 18f * s, cx + 30f * s, cy - 30f * s, cx + 28f * s, cy - 40f * s)
        // Neck going up
        cubicTo(cx + 26f * s, cy - 55f * s, cx + 35f * s, cy - 65f * s, cx + 25f * s, cy - 80f * s)
        cubicTo(cx + 20f * s, cy - 88f * s, cx + 12f * s, cy - 93f * s, cx + 8f * s, cy - 98f * s)
        // Head top
        cubicTo(cx + 6f * s, cy - 105f * s, cx + 18f * s, cy - 105f * s, cx + 20f * s, cy - 98f * s)
        // Front of neck coming down
        cubicTo(cx + 22f * s, cy - 90f * s, cx + 30f * s, cy - 80f * s, cx + 32f * s, cy - 65f * s)
        cubicTo(cx + 35f * s, cy - 50f * s, cx + 40f * s, cy - 38f * s, cx + 30f * s, cy - 22f * s)
        // Front of body
        cubicTo(cx + 25f * s, cy - 15f * s, cx + 20f * s, cy - 5f * s, cx + 18f * s, cy + 5f * s)
        // Underbelly
        cubicTo(cx + 15f * s, cy + 18f * s, cx - 10f * s, cy + 28f * s, cx - 30f * s, cy + 25f * s)
        close()
    }
    drawPath(body, pink, style = Fill)

    // Wing overlay
    val wingPath = Path().apply {
        moveTo(cx - 10f * s, cy - 12f * s)
        cubicTo(cx + 5f * s, cy - 22f * s, cx + 25f * s, cy - 8f * s, cx + 22f * s, cy * s)
        cubicTo(cx + 18f * s, cy + 5f * s, cx * s, cy + 8f * s, cx * s, cy + 5f * s)
        cubicTo(cx - 5f * s, cy * s, cx - 12f * s, cy - 8f * s, cx - 10f * s, cy - 12f * s)
        close()
    }
    drawPath(wingPath, wing.copy(alpha = 0.6f), style = Fill)

    // Tail feathers
    val tail1 = Path().apply {
        moveTo(cx - 38f * s, cy + 3f * s)
        cubicTo(cx - 55f * s, cy + 8f * s, cx - 48f * s, cy + 20f * s, cx - 45f * s, cy + 18f * s)
        cubicTo(cx - 38f * s, cy + 12f * s, cx - 32f * s, cy + 8f * s, cx - 30f * s, cy + 8f * s)
        close()
    }
    drawPath(tail1, pink, style = Fill)

    val tail2 = Path().apply {
        moveTo(cx - 40f * s, cy * s)
        cubicTo(cx - 58f * s, cy + 2f * s, cx - 52f * s, cy + 14f * s, cx - 50f * s, cy + 12f * s)
        cubicTo(cx - 44f * s, cy + 6f * s, cx - 36f * s, cy + 3f * s, cx - 34f * s, cy + 3f * s)
        close()
    }
    drawPath(tail2, light.copy(alpha = 0.5f), style = Fill)

    // Beak
    val beakPath = Path().apply {
        moveTo(cx + 22f * s, cy - 96f * s)
        cubicTo(cx + 32f * s, cy - 95f * s, cx + 35f * s, cy - 89f * s, cx + 30f * s, cy - 83f * s)
        cubicTo(cx + 27f * s, cy - 80f * s, cx + 22f * s, cy - 84f * s, cx + 20f * s, cy - 90f * s)
        close()
    }
    drawPath(beakPath, beak, style = Fill)

    // Eye
    drawCircle(eye, 2.5f * s, Offset(cx + 15f * s, cy - 99f * s))
    drawCircle(Color.White.copy(alpha = 0.7f), 1f * s, Offset(cx + 15.5f * s, cy - 99.5f * s))
}

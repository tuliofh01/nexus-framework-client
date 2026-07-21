package nexus.opensource.framework.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Renders a flamingo bitmap for the Compose Desktop window icon.
 * CUSTOMIZE: replace with a packaged .ico / .png if you prefer static assets.
 */
@Composable
fun rememberFlamingoWindowIcon(size: Int = 64): ImageBitmap? {
    return remember(size) {
        runCatching { renderFlamingoImageBitmap(size) }.getOrNull()
    }
}

package nexus.opensource.framework.view

import androidx.compose.ui.graphics.Color
import nexus.opensource.framework.core.model.NexusBranding

/** Shared Nexus client palette — sourced from [NexusBranding]. */
object NexusTheme {
    val BrandPurple = Color(NexusBranding.BRAND_PURPLE_HEX)
    val FlamingoPink = Color(NexusBranding.FLAMINGO_PINK_HEX)
    val FlamingoLight = Color(0xFFF5A0B5)
    val FlamingoWing = Color(0xFFEBA0AC)
    val BeakOrange = Color(0xFFFAB387)

    val DarkBg = Color(NexusBranding.DARK_BG_HEX)
    val SurfaceBg = Color(0xFF16213E)
    val CardBg = Color(0xFF1F2B47)
    val CardBgHover = Color(0xFF263556)
    val Divider = Color(0xFF3A3A5C)
    val ProgressTrack = Color(0xFF2D2D4A)

    val AccentCyan = Color(0xFF00D4FF)
    val AccentGreen = Color(0xFF00E676)
    val AccentOrange = Color(0xFFFF9100)
    val AccentRed = Color(0xFFFF5252)
    val AccentPink = Color(0xFFE040FB)

    val TextPrimary = Color(0xFFE8E8E8)
    val TextSecondary = Color(0xFF9090A0)
    val TextMuted = Color(0xFF707070)
    val SuccessGreen = Color(0xFF00C853)
}

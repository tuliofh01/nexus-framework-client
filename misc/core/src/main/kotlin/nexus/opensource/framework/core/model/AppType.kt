package nexus.opensource.framework.core.model

enum class AppType(val id: String, val label: String, val templateFolder: String) {
    DESKTOP("desktop", "Desktop App", "desktop-app"),
    ANDROID("android", "Android App", "android-app"),
    ;

    companion object {
        fun fromId(id: String): AppType? = entries.find { it.id == id }

        fun fromCliArg(raw: String): AppType {
            val normalized = raw.lowercase()
            return when (normalized) {
                "desktop", "desktop-app", "simple", "basic" -> DESKTOP
                "android", "android-app" -> ANDROID
                else -> fromId(normalized)
                    ?: error("Unknown app type '$raw'. Use desktop or android.")
            }
        }
    }
}

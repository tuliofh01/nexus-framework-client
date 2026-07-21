package nexus.opensource.framework.core.model

/**
 * Central branding constants for Nexus Framework — desktop client, CLI, and generated apps.
 *
 * // CUSTOMIZE: Keep [FRAMEWORK_VERSION] in sync with `nexusFramework` in gradle/libs.versions.toml.
 */
object NexusBranding {
    const val FRAMEWORK_NAME: String = "The Nexus Framework"
    const val FRAMEWORK_VERSION: String = "1.0.2"

    const val TAGLINE: String = "Cross-platform native app generation"
    const val SUBTITLE: String = "Dashboard UI · Framework Package · Modern C++"

    /** Primary brand purple used in UI accents. */
    const val BRAND_PURPLE_HEX: Long = 0xFF6C63FF
    /** Flamingo mascot pink. */
    const val FLAMINGO_PINK_HEX: Long = 0xFFF38BA8
    /** Dark background for client screens. */
    const val DARK_BG_HEX: Long = 0xFF1A1A2E

    const val DOCS_URL: String = "https://github.com/tuliofh01/nexus-framework-client#readme"
    const val REPO_URL: String = "https://github.com/tuliofh01/nexus-framework-client"
    const val ISSUES_URL: String = "https://github.com/tuliofh01/nexus-framework-client/issues"

    fun windowTitle(projectName: String): String = "$projectName - built with $FRAMEWORK_NAME"

    fun versionLabel(): String = "v$FRAMEWORK_VERSION — $SUBTITLE"
}

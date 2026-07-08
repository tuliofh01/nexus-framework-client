package nexus.opensource.utils

/**
 * Shared branding helpers used by the client application.
 */
object NexusBranding {

    const val FRAMEWORK_NAME: String = "The Nexus Framework"

    /**
     * Window title convention: `{projectName} - built with The Nexus Framework`.
     */
    fun windowTitle(projectName: String): String = "$projectName - built with $FRAMEWORK_NAME"
}

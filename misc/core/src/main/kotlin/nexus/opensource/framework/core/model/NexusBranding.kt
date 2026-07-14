package nexus.opensource.framework.core.model

object NexusBranding {
    const val FRAMEWORK_NAME: String = "The Nexus Framework"

    fun windowTitle(projectName: String): String = "$projectName - built with $FRAMEWORK_NAME"
}

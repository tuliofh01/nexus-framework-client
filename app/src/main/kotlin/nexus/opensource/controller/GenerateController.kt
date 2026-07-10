package nexus.opensource.controller

import nexus.opensource.core.RepoRoot
import nexus.opensource.core.model.AppType
import nexus.opensource.core.model.ProjectSpec
import nexus.opensource.core.service.GenerateOptions
import nexus.opensource.core.service.ProjectGenerator

/**
 * Minimal generation controller for the v1 Compose client stub.
 */
class GenerateController {
    var projectName: String = "MyApp"
    var appType: AppType = AppType.DESKTOP
    var statusMessage: String = ""
    var isGenerating: Boolean = false

    fun generate(onProgress: (String) -> Unit = {}): String? {
        if (isGenerating) return "Generation already in progress"
        isGenerating = true
        statusMessage = ""
        return try {
            val spec = ProjectSpec(
                projectName = projectName,
                outputPath = ProjectGenerator.defaultOutputPath(projectName),
                appType = appType,
            )
            val repoRoot = RepoRoot.resolve()
            val generator = ProjectGenerator(repoRoot)
            val path = generator.generate(
                spec,
                onProgress = { line ->
                    statusMessage = line
                    onProgress(line)
                },
                options = GenerateOptions(),
            )
            "Generated: $path"
        } catch (e: Exception) {
            val message = e.message ?: e.toString()
            statusMessage = message
            message
        } finally {
            isGenerating = false
        }
    }
}

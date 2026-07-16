package nexus.opensource.framework.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.ProjectSpec
import nexus.opensource.framework.core.service.GenerateOptions
import nexus.opensource.framework.core.service.ProjectGenerator

class GenerateController {
    var projectName: String by mutableStateOf("MyApp")
    var appType: AppType by mutableStateOf(AppType.DESKTOP)
    var statusMessage: String by mutableStateOf("")
    var isGenerating: Boolean by mutableStateOf(false)
    val blueprintEditor: BlueprintEditorController = BlueprintEditorController()
    val flowsEditor: FlowsEditorController = FlowsEditorController()

    fun syncBlueprintContext() {
        blueprintEditor.syncContext(projectName, appType)
    }

    fun syncFlowsContext() {
        flowsEditor.syncContext(projectName, appType)
    }

    fun syncAllContext() {
        syncBlueprintContext()
        syncFlowsContext()
    }

    fun generate(onProgress: (String) -> Unit = {}): String? {
        if (isGenerating) return "Generation already in progress"
        isGenerating = true
        statusMessage = ""
        return try {
            syncAllContext()
            val spec = ProjectSpec(
                projectName = projectName,
                outputPath = ProjectGenerator.defaultOutputPath(projectName),
                appType = appType,
                blueprint = blueprintEditor.blueprint,
                flows = flowsEditor.effectiveFlows(),
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

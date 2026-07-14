package nexus.opensource.framework.controller

import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowsFile
import nexus.opensource.framework.core.model.FlowsJson
import nexus.opensource.framework.core.service.FlowsValidator
import nexus.opensource.framework.core.service.ProjectGenerator
import java.nio.file.Files
import java.nio.file.Path

/**
 * Holds flows.json state for the v1 Compose JSON preview editor.
 *
 * flowsEnabled is derived from any enabled flow — matches nxs_config.json flows.enabled
 * semantics the native FlowRunner checks before loading flows/flows.json.
 */
class FlowsEditorController(
    private val validator: FlowsValidator = FlowsValidator(),
) {
    var flows: FlowsFile = FlowsJson.sampleApp()
        private set
    var appType: AppType = AppType.DESKTOP
        private set
    var projectName: String = "MyApp"
        private set
    var flowsEnabled: Boolean = true
    var statusMessage: String = ""
    var validationErrors: List<String> = emptyList()

    private var loadedFromPath: Path? = null

    fun syncContext(projectName: String, appType: AppType) {
        if (this.projectName == projectName && this.appType == appType && loadedFromPath == null) {
            validate()
            return
        }
        this.projectName = projectName
        this.appType = appType
        reloadFromTemplate()
    }

    fun reloadFromTemplate() {
        val repoRoot = RepoRoot.resolve()
        val generator = ProjectGenerator(repoRoot)
        val vars = generator.templateVars(
            nexus.opensource.framework.core.model.ProjectSpec(
                projectName = projectName,
                outputPath = ProjectGenerator.defaultOutputPath(projectName),
                appType = appType,
            ),
        )
        val templatePath = repoRoot.resolve(ProjectGenerator.TEMPLATE_DIR)
            .resolve(appType.templateFolder)
            .resolve(FlowsJson.DEFAULT_PATH)
        val rawText = Files.readString(templatePath)
        flows = FlowsJson.readRendered(rawText, vars)
        loadedFromPath = null
        flowsEnabled = flows.flows.any { it.enabled }
        validate()
        statusMessage = "Loaded template flows (${appType.label})"
    }

    fun loadFromFile(path: Path) {
        flows = FlowsJson.read(Files.readString(path))
        loadedFromPath = path
        flowsEnabled = flows.flows.any { it.enabled }
        validate()
        statusMessage = "Loaded ${path.fileName}"
    }

    fun toggleFlowEnabled(flowId: String, enabled: Boolean) {
        flows = flows.copy(
            flows = flows.flows.map { flow ->
                if (flow.id == flowId) flow.copy(enabled = enabled) else flow
            },
        )
        flowsEnabled = flows.flows.any { it.enabled }
        validate()
        statusMessage = if (enabled) "Enabled flow '$flowId'" else "Disabled flow '$flowId'"
    }

    fun setAllFlowsEnabled(enabled: Boolean) {
        flowsEnabled = enabled
        flows = flows.copy(
            flows = flows.flows.map { it.copy(enabled = enabled) },
        )
        validate()
        statusMessage = if (enabled) "All flows enabled" else "All flows disabled"
    }

    fun toJsonPreview(): String = FlowsJson.write(flows)

    fun effectiveFlows(): FlowsFile? =
        if (flowsEnabled && flows.flows.isNotEmpty()) flows else null

    private fun validate() {
        val result = validator.validate(flows)
        validationErrors = result.errors
    }
}

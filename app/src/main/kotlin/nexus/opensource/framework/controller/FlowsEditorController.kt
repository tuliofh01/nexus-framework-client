package nexus.opensource.framework.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowStep
import nexus.opensource.framework.core.model.FlowStepType
import nexus.opensource.framework.core.model.FlowsFile
import nexus.opensource.framework.core.model.FlowsJson
import nexus.opensource.framework.core.model.ProjectSpec
import nexus.opensource.framework.core.service.FlowsValidator
import nexus.opensource.framework.core.service.LangflowTransformationEngine
import nexus.opensource.framework.core.service.ProjectGenerator
import java.nio.file.Files
import java.nio.file.Path

/**
 * Holds flows.json state for the v1 Compose JSON preview editor.
 *
 * flowsEnabled is derived from any enabled flow — matches nxs_config.json flows.enabled
 * semantics the native FlowRunner checks before loading flows/flows.json.
 *
 * Langflow import maps export JSON → disabled [FlowDefinition] stubs via
 * [LangflowTransformationEngine] (flows only — not blueprint.json).
 */
class FlowsEditorController(
    private val validator: FlowsValidator = FlowsValidator(),
    private val langflowEngine: LangflowTransformationEngine = LangflowTransformationEngine(validator),
) {
    var flows: FlowsFile by mutableStateOf(FlowsJson.sampleApp())
        private set
    var appType: AppType by mutableStateOf(AppType.DESKTOP)
        private set
    var projectName: String by mutableStateOf("MyApp")
        private set
    var flowsEnabled: Boolean by mutableStateOf(true)
    var statusMessage: String by mutableStateOf("")
    var validationErrors: List<String> by mutableStateOf(emptyList())
    var importWarnings: List<String> by mutableStateOf(emptyList())
        private set
    var selectedFlowId: String? by mutableStateOf(null)
        private set

    private var loadedFromPath: Path? = null

    /** Path last loaded or saved, if any. */
    val sourcePath: Path? get() = loadedFromPath

    val selectedFlow: FlowDefinition?
        get() = selectedFlowId?.let { id -> flows.flows.find { it.id == id } }

    fun syncContext(projectName: String, appType: AppType) {
        this.projectName = projectName
        this.appType = appType
        if (loadedFromPath != null) {
            validate()
            return
        }
        reloadFromTemplate()
    }

    fun reloadFromTemplate() {
        val repoRoot = RepoRoot.resolve()
        val generator = ProjectGenerator(repoRoot)
        val vars = generator.templateVars(
            ProjectSpec(
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
        importWarnings = emptyList()
        flowsEnabled = flows.flows.any { it.enabled }
        validate()
        statusMessage = "Loaded template flows (${appType.label})"
    }

    fun loadFromFile(path: Path) {
        flows = FlowsJson.read(Files.readString(path))
        loadedFromPath = path
        importWarnings = emptyList()
        flowsEnabled = flows.flows.any { it.enabled }
        validate()
        statusMessage = "Loaded ${path.fileName}"
    }

    fun saveToFile(path: Path) {
        validate()
        if (validationErrors.isNotEmpty()) {
            error("Cannot save invalid flows: ${validationErrors.joinToString()}")
        }
        Files.createDirectories(path.parent ?: path)
        Files.writeString(path, FlowsJson.write(flows))
        loadedFromPath = path
        statusMessage = "Saved ${path.fileName}"
    }

    /**
     * Persist to [path], or to [sourcePath] when [path] is null.
     * @return false if no path is available (caller should open a save dialog).
     */
    fun save(path: Path? = loadedFromPath): Boolean {
        val target = path ?: loadedFromPath
        if (target == null) {
            validate()
            statusMessage = if (validationErrors.isEmpty()) {
                "Choose a file to save flows.json"
            } else {
                "Cannot save — fix validation errors first"
            }
            return false
        }
        return runCatching {
            saveToFile(target)
            true
        }.getOrElse {
            statusMessage = "Save failed: ${it.message}"
            false
        }
    }

    /**
     * Import a Langflow export JSON file as disabled Nexus flows.
     * @param replace when true, replaces current flows; otherwise merges by id.
     */
    fun importLangflow(path: Path, replace: Boolean = false) {
        val raw = Files.readString(path)
        val result = langflowEngine.transform(raw)
        importWarnings = result.warnings
        if (!result.hasFlows) {
            statusMessage = "Langflow import produced no flows"
            return
        }
        val imported = FlowsFile(version = 1, flows = result.flows)
        flows = if (replace || flows.flows.isEmpty()) {
            imported
        } else {
            val existingIds = flows.flows.map { it.id }.toSet()
            FlowsFile(
                version = flows.version.coerceAtLeast(1),
                flows = flows.flows + imported.flows.filter { it.id !in existingIds },
            )
        }
        flowsEnabled = flows.flows.any { it.enabled }
        selectedFlowId = result.flows.firstOrNull()?.id
        validate()
        val warnNote = if (result.warnings.isNotEmpty()) " (${result.warnings.size} warning(s))" else ""
        statusMessage =
            "Imported ${result.flows.size} flow(s) from Langflow${warnNote} — all disabled; review before enable"
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

    fun selectFlow(flowId: String?) {
        selectedFlowId = flowId
        statusMessage = flowId?.let { "Editing flow '$it'" } ?: ""
    }

    fun addStep(flowId: String, stepType: FlowStepType = FlowStepType.INVOKE) {
        updateFlow(flowId) { flow ->
            val step = FlowStep(
                type = stepType.id,
                target = when (stepType) {
                    FlowStepType.INVOKE -> "nxs.custom_action"
                    FlowStepType.CONDITION -> null
                    FlowStepType.DELAY -> null
                },
                ms = if (stepType == FlowStepType.DELAY) 1000L else null,
            )
            flow.copy(steps = flow.steps + step)
        }
        statusMessage = "Added ${stepType.label} step to '$flowId'"
    }

    fun removeStep(flowId: String, stepIndex: Int) {
        updateFlow(flowId) { flow ->
            flow.copy(steps = flow.steps.filterIndexed { index, _ -> index != stepIndex })
        }
        statusMessage = "Removed step from '$flowId'"
    }

    fun moveStep(flowId: String, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        updateFlow(flowId) { flow ->
            val steps = flow.steps.toMutableList()
            if (fromIndex !in steps.indices || toIndex !in steps.indices) return@updateFlow flow
            val step = steps.removeAt(fromIndex)
            steps.add(toIndex, step)
            flow.copy(steps = steps)
        }
        statusMessage = "Reordered steps in '$flowId'"
    }

    fun updateStepTarget(flowId: String, stepIndex: Int, target: String) {
        updateFlow(flowId) { flow ->
            flow.copy(
                steps = flow.steps.mapIndexed { index, step ->
                    if (index == stepIndex) step.copy(target = target.ifBlank { null }) else step
                },
            )
        }
    }

    fun updateStepWhenExpr(flowId: String, stepIndex: Int, whenExpr: String) {
        updateFlow(flowId) { flow ->
            flow.copy(
                steps = flow.steps.mapIndexed { index, step ->
                    if (index == stepIndex) step.copy(whenExpr = whenExpr.ifBlank { null }) else step
                },
            )
        }
    }

    fun updateStepDelayMs(flowId: String, stepIndex: Int, ms: Long?) {
        updateFlow(flowId) { flow ->
            flow.copy(
                steps = flow.steps.mapIndexed { index, step ->
                    if (index == stepIndex) step.copy(ms = ms) else step
                },
            )
        }
    }

    private fun updateFlow(flowId: String, transform: (FlowDefinition) -> FlowDefinition) {
        flows = flows.copy(
            flows = flows.flows.map { flow ->
                if (flow.id == flowId) transform(flow) else flow
            },
        )
        validate()
    }

    private fun validate() {
        val result = validator.validate(flows)
        validationErrors = result.errors
    }
}

package com.nexus.framework.ui.generate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nexus.framework.core.RepoRoot
import com.nexus.framework.core.model.AppType
import com.nexus.framework.core.model.BlueprintJson
import com.nexus.framework.core.model.FlowsJson
import com.nexus.framework.core.model.LangflowJson
import com.nexus.framework.core.model.ProjectSpec
import com.nexus.framework.core.service.GenerateOptions
import com.nexus.framework.core.service.LangflowBlueprintMapper
import com.nexus.framework.core.service.LangflowTransformationEngine
import com.nexus.framework.core.service.ProjectGenerator
import java.nio.file.Files
import java.nio.file.Path
import com.nexus.framework.ui.blueprint.BlueprintEditorController
import com.nexus.framework.ui.flows.FlowsEditorController

class GenerateController {
    /** Empty by default so the Generate screen placeholder can clear on focus/typing. */
    var projectName: String by mutableStateOf("")
    var appType: AppType by mutableStateOf(AppType.DESKTOP)
    var statusMessage: String by mutableStateOf("")
    var isGenerating: Boolean by mutableStateOf(false)
    /** Absolute path of the opened generated project root, if any. */
    var openedProjectPath: Path? by mutableStateOf(null)
        private set

    val blueprintEditor: BlueprintEditorController = BlueprintEditorController()
    val flowsEditor: FlowsEditorController = FlowsEditorController()

    private val langflowEngine = LangflowTransformationEngine()

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

    /**
     * Open an already-generated app directory (expects `blueprint.json` and optionally
     * `flows/flows.json`). Loads both editors and sets [projectName] / [appType].
     *
     * @return null on success, or an error message.
     */
    fun openGeneratedProject(projectRoot: Path): String? {
        val root = projectRoot.toAbsolutePath().normalize()
        if (!Files.isDirectory(root)) {
            return "Not a directory: $root"
        }

        val blueprintPath = root.resolve(BlueprintJson.FILE_NAME)
        val flowsPath = root.resolve(FlowsJson.DEFAULT_PATH)
        val hasBlueprint = Files.isRegularFile(blueprintPath)
        val hasFlows = Files.isRegularFile(flowsPath)
        if (!hasBlueprint && !hasFlows) {
            return "No blueprint.json or flows/flows.json under $root"
        }

        projectName = root.fileName.toString()
        appType = inferAppType(root)
        openedProjectPath = root

        if (hasBlueprint) {
            blueprintEditor.loadFromFile(blueprintPath)
        } else if (hasFlows) {
            val flows = FlowsJson.read(Files.readString(flowsPath))
            blueprintEditor.loadBlueprint(
                LangflowBlueprintMapper.fromFlowsFile(flows, projectName),
                sourceLabel = "flows from ${root.fileName}",
            )
        }

        // Keep editor projectName/appType in sync without reloading templates over the file.
        blueprintEditor.syncContext(projectName, appType)

        if (hasFlows) {
            flowsEditor.loadFromFile(flowsPath)
        }
        flowsEditor.syncContext(projectName, appType)

        // If blueprint had no flow.automation nodes but flows exist, overlay flow graph for visibility
        if (hasBlueprint && hasFlows) {
            val bp = blueprintEditor.blueprint
            val hasFlowNodes = bp.nodes.any { it.type == "flow.automation" }
            if (!hasFlowNodes && flowsEditor.flows.flows.isNotEmpty()) {
                val flowGraph = LangflowBlueprintMapper.fromFlowsFile(flowsEditor.flows, projectName)
                blueprintEditor.loadBlueprint(
                    bp.copy(
                        description = listOf(bp.description, flowGraph.description)
                            .filter { it.isNotBlank() }
                            .joinToString(" — "),
                        nodes = bp.nodes + flowGraph.nodes,
                        edges = bp.edges + flowGraph.edges,
                    ),
                    sourceLabel = "project + flows overlay",
                )
            }
        }

        statusMessage = "Opened $projectName (${root})"
        return null
    }

    /**
     * Import a Langflow export JSON: populate [flowsEditor] stubs and replace the
     * blueprint canvas with Langflow nodes so the Blueprint Editor can show the graph.
     *
     * @return null on success, or an error message.
     */
    fun importLangflow(path: Path): String? {
        return try {
            val raw = Files.readString(path)
            val export = LangflowJson.read(raw)
            val flowResult = langflowEngine.transform(raw)
            if (!flowResult.hasFlows && (export.data?.nodes.isNullOrEmpty())) {
                return "Langflow import produced no flows or nodes"
            }

            // Runtime stubs (always disabled)
            if (flowResult.hasFlows) {
                flowsEditor.importLangflow(path, replace = true)
            }

            // Visual graph on blueprint canvas
            val visual = LangflowBlueprintMapper.fromLangflowExport(export)
            if (visual.nodes.isEmpty() && flowResult.hasFlows) {
                blueprintEditor.loadBlueprint(
                    LangflowBlueprintMapper.fromFlowsFile(
                        flowsEditor.flows,
                        export.name.ifBlank { path.fileName.toString() },
                    ),
                    sourceLabel = "Langflow→flows visual",
                )
            } else {
                blueprintEditor.loadBlueprint(visual, sourceLabel = "Langflow ${path.fileName}")
            }

            if (projectName.isBlank()) {
                projectName = export.name.ifBlank { "LangflowImport" }
                    .replace(Regex("[^A-Za-z0-9_-]"), "")
                    .ifBlank { "LangflowImport" }
            }
            openedProjectPath = null
            statusMessage = "Imported Langflow ${path.fileName} — " +
                "${blueprintEditor.blueprint.nodes.size} canvas nodes, " +
                "${flowsEditor.flows.flows.size} flow stub(s) (disabled)"
            null
        } catch (e: Exception) {
            val message = e.message ?: e.toString()
            statusMessage = message
            message
        }
    }

    fun generate(onProgress: (String) -> Unit = {}): String? {
        if (isGenerating) return "Generation already in progress"
        val name = projectName.trim()
        if (name.isBlank()) {
            statusMessage = "Enter a project name before generating"
            return statusMessage
        }
        projectName = name
        isGenerating = true
        statusMessage = ""
        return try {
            syncAllContext()
            val output = openedProjectPath?.toString()
                ?: ProjectGenerator.defaultOutputPath(projectName)
            val spec = ProjectSpec(
                projectName = projectName,
                outputPath = output,
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
            openedProjectPath = path
            statusMessage = "Generated: $path"
            statusMessage
        } catch (e: Exception) {
            val message = e.message ?: e.toString()
            statusMessage = message
            message
        } finally {
            isGenerating = false
        }
    }

    private fun inferAppType(root: Path): AppType {
        if (Files.isDirectory(root.resolve("app/src/main"))) return AppType.ANDROID
        val config = root.resolve("nxs_config.json")
        if (Files.isRegularFile(config)) {
            val text = runCatching { Files.readString(config) }.getOrNull().orEmpty()
            if (text.contains("android", ignoreCase = true)) return AppType.ANDROID
        }
        return AppType.DESKTOP
    }
}

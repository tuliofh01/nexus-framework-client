package nexus.opensource.framework.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.BlueprintEdge
import nexus.opensource.framework.core.model.BlueprintFile
import nexus.opensource.framework.core.model.BlueprintJson
import nexus.opensource.framework.core.model.BlueprintNode
import nexus.opensource.framework.core.model.BlueprintNodeType
import nexus.opensource.framework.core.model.BlueprintPosition
import nexus.opensource.framework.core.model.ProjectSpec
import nexus.opensource.framework.core.service.BlueprintValidator
import nexus.opensource.framework.core.service.ProjectGenerator
import java.nio.file.Files
import java.nio.file.Path

/**
 * Validates whether an edge between two blueprint nodes is allowed.
 * CUSTOMIZE: Implement port/type compatibility checks for v1.1 imnodes integration.
 */
fun interface BlueprintConnectionValidator {
    fun validate(source: BlueprintNode, target: BlueprintNode): String?
}

/** No-op validator — all connections allowed until customized. */
object AllowAllBlueprintConnections : BlueprintConnectionValidator {
    override fun validate(source: BlueprintNode, target: BlueprintNode): String? = null
}

/**
 * Extension slot for imnodes v1.1 native canvas.
 * CUSTOMIZE: Implement via JNI/FFI and assign to [BlueprintEditorController.canvasExtension].
 */
interface BlueprintCanvasExtension {
    /** Return true when the native imnodes canvas owns rendering for this screen. */
    val useNativeCanvas: Boolean get() = false
}

object DefaultBlueprintCanvasExtension : BlueprintCanvasExtension

/**
 * Holds blueprint graph state for the v1 Compose JSON/canvas editor.
 * v1.1 will embed imnodes via native interop; this controller keeps the same schema.
 *
 * Reloading from template applies ProjectGenerator placeholder vars so the editor
 * matches what a fresh generate would produce — without writing to builds/framework/.
 */
class BlueprintEditorController(
    private val validator: BlueprintValidator = BlueprintValidator(),
    var connectionValidator: BlueprintConnectionValidator = AllowAllBlueprintConnections,
    var canvasExtension: BlueprintCanvasExtension = DefaultBlueprintCanvasExtension,
) {
    var blueprint: BlueprintFile by mutableStateOf(BlueprintJson.sampleApp("MyApp", AppType.DESKTOP))
        private set
    var appType: AppType by mutableStateOf(AppType.DESKTOP)
        private set
    var projectName: String by mutableStateOf("MyApp")
        private set
    var selectedNodeId: String? by mutableStateOf(null)
    var pendingEdgeSourceId: String? by mutableStateOf(null)
    var statusMessage: String by mutableStateOf("")
    var validationErrors: List<String> by mutableStateOf(emptyList())
    /** Show JSON preview pane beside the inspector. */
    var showJsonPreview: Boolean by mutableStateOf(false)
    /** Last toolbar action label for status strip. */
    var lastToolbarAction: String by mutableStateOf("")

    private var loadedFromPath: Path? = null

    /** Path last loaded or saved, if any (for Save without re-picking). */
    val sourcePath: Path? get() = loadedFromPath

    fun syncContext(projectName: String, appType: AppType) {
        if (loadedFromPath != null) {
            this.projectName = projectName
            this.appType = appType
            blueprint = blueprint.copy(name = "$projectName flow")
            validate()
            return
        }
        if (this.projectName == projectName && this.appType == appType) {
            blueprint = blueprint.copy(name = "$projectName flow")
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
            ProjectSpec(
                projectName = projectName,
                outputPath = ProjectGenerator.defaultOutputPath(projectName),
                appType = appType,
            ),
        )
        val templatePath = repoRoot.resolve(ProjectGenerator.TEMPLATE_DIR)
            .resolve(appType.templateFolder)
            .resolve(BlueprintJson.FILE_NAME)
        val rawText = Files.readString(templatePath)
        blueprint = BlueprintJson.readRendered(rawText, vars).copy(name = "$projectName flow")
        loadedFromPath = null
        selectedNodeId = null
        pendingEdgeSourceId = null
        validate()
        statusMessage = "Loaded template blueprint (${appType.label})"
    }

    fun loadFromFile(path: Path) {
        blueprint = BlueprintJson.read(Files.readString(path))
        loadedFromPath = path
        selectedNodeId = null
        pendingEdgeSourceId = null
        validate()
        statusMessage = "Loaded ${path.fileName}"
    }

    /** Replace the canvas with an already-built [BlueprintFile] (e.g. Langflow visual map). */
    fun loadBlueprint(file: BlueprintFile, sourceLabel: String = "graph") {
        blueprint = file
        loadedFromPath = null
        selectedNodeId = null
        pendingEdgeSourceId = null
        validate()
        statusMessage = "Loaded $sourceLabel (${file.nodes.size} nodes, ${file.edges.size} edges)"
    }

    fun saveToFile(path: Path) {
        validate()
        if (validationErrors.isNotEmpty()) {
            error("Cannot save invalid blueprint")
        }
        Files.createDirectories(path.parent ?: path)
        Files.writeString(path, BlueprintJson.write(blueprint))
        loadedFromPath = path
        statusMessage = "Saved ${path.fileName}"
    }

    fun selectNode(nodeId: String?) {
        selectedNodeId = nodeId
        if (nodeId != null) {
            pendingEdgeSourceId = null
        }
    }

    fun beginEdgeFrom(nodeId: String) {
        pendingEdgeSourceId = nodeId
        selectedNodeId = nodeId
        statusMessage = "Select target node for edge from '$nodeId'"
    }

    fun completeEdgeTo(targetId: String) {
        val sourceId = pendingEdgeSourceId ?: return
        if (!validateConnection(sourceId, targetId)) {
            statusMessage = "Connection rejected: $sourceId → $targetId"
            pendingEdgeSourceId = null
            return
        }
        if (sourceId == targetId) {
            statusMessage = "Edge source and target must differ"
            return
        }
        val sourceNode = blueprint.nodes.find { it.id == sourceId }
        val targetNode = blueprint.nodes.find { it.id == targetId }
        if (sourceNode == null || targetNode == null) {
            statusMessage = "Invalid edge endpoints"
            return
        }
        connectionValidator.validate(sourceNode, targetNode)?.let { error ->
            statusMessage = error
            pendingEdgeSourceId = null
            return
        }
        val edgeId = nextEdgeId()
        blueprint = blueprint.copy(
            edges = blueprint.edges + BlueprintEdge(edgeId, sourceId, targetId, "port"),
        )
        pendingEdgeSourceId = null
        validate()
        statusMessage = "Added edge $edgeId"
    }

    fun removeSelectedNode() {
        val id = selectedNodeId ?: return
        blueprint = blueprint.copy(
            nodes = blueprint.nodes.filterNot { it.id == id },
            edges = blueprint.edges.filter { it.source != id && it.target != id },
        )
        selectedNodeId = null
        validate()
        statusMessage = "Removed node '$id'"
    }

    fun removeSelectedEdge(edgeId: String) {
        blueprint = blueprint.copy(edges = blueprint.edges.filterNot { it.id == edgeId })
        validate()
        statusMessage = "Removed edge '$edgeId'"
    }

    fun addNode(type: BlueprintNodeType) {
        val id = nextNodeId(type)
        val position = BlueprintPosition(
            x = 80f + (blueprint.nodes.size % 4) * 180f,
            y = 80f + (blueprint.nodes.size / 4) * 120f,
        )
        val node = BlueprintJson.createNode(type, id, position, appType)
        blueprint = blueprint.copy(nodes = blueprint.nodes + node)
        selectedNodeId = id
        validate()
        statusMessage = "Added ${type.label} ($id)"
    }

    fun moveNode(nodeId: String, x: Float, y: Float) {
        blueprint = blueprint.copy(
            nodes = blueprint.nodes.map { node ->
                if (node.id == nodeId) node.copy(position = BlueprintPosition(x, y)) else node
            },
        )
    }

    fun updateNodeId(oldId: String, newId: String) {
        if (newId.isBlank() || blueprint.nodes.any { it.id == newId && it.id != oldId }) return
        blueprint = blueprint.copy(
            nodes = blueprint.nodes.map { if (it.id == oldId) it.copy(id = newId) else it },
            edges = blueprint.edges.map { edge ->
                edge.copy(
                    source = if (edge.source == oldId) newId else edge.source,
                    target = if (edge.target == oldId) newId else edge.target,
                )
            },
        )
        if (selectedNodeId == oldId) selectedNodeId = newId
        validate()
    }

    fun toJsonPreview(): String = BlueprintJson.write(blueprint)

    fun toggleJsonPreview() {
        showJsonPreview = !showJsonPreview
        lastToolbarAction = if (showJsonPreview) "JSON preview shown" else "JSON preview hidden"
        statusMessage = lastToolbarAction
    }

    /** Re-run schema validation and surface a mockup-friendly status. */
    fun runValidate() {
        validate()
        lastToolbarAction = "Validate"
        statusMessage = if (validationErrors.isEmpty()) {
            "Blueprint valid — ${blueprint.nodes.size} nodes, ${blueprint.edges.size} edges"
        } else {
            "Validation failed — ${validationErrors.size} issue(s)"
        }
    }

    /**
     * Persist to [path], or to [sourcePath] when [path] is null.
     * @return false if no path is available (caller should open a save dialog).
     */
    fun save(path: Path? = loadedFromPath): Boolean {
        lastToolbarAction = "Save"
        val target = path ?: loadedFromPath
        if (target == null) {
            validate()
            statusMessage = if (validationErrors.isEmpty()) {
                "Choose a file to save blueprint.json"
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

    /** Load blueprint.json from disk (caller supplies path from a file dialog). */
    fun load(path: Path) {
        lastToolbarAction = "Load"
        runCatching { loadFromFile(path) }
            .onFailure { statusMessage = "Load failed: ${it.message}" }
    }

    /**
     * Connection pre-filter before [connectionValidator].
     * Rejects self-loops and duplicate edges.
     */
    fun validateConnection(sourceId: String, targetId: String): Boolean {
        if (sourceId == targetId) return false
        if (blueprint.edges.any { it.source == sourceId && it.target == targetId }) return false
        val source = blueprint.nodes.find { it.id == sourceId } ?: return false
        val target = blueprint.nodes.find { it.id == targetId } ?: return false
        // TODO: enforce paradigm/type rules, DAG acyclicity
        return source.type.isNotBlank() && target.type.isNotBlank()
    }

    /** CUSTOMIZE: palette entries for drag-drop node creation in v1.1 imnodes. */
    fun paletteNodeTypes(): List<BlueprintNodeType> = BlueprintNodeType.ALL

    private fun validate() {
        val result = validator.validate(blueprint)
        validationErrors = result.errors
    }

    private fun nextNodeId(type: BlueprintNodeType): String {
        val prefix = type.id.substringAfterLast('.')
        var index = blueprint.nodes.size + 1
        var candidate = "$prefix-$index"
        while (blueprint.nodes.any { it.id == candidate }) {
            index++
            candidate = "$prefix-$index"
        }
        return candidate
    }

    private fun nextEdgeId(): String {
        var index = blueprint.edges.size + 1
        var candidate = "e$index"
        while (blueprint.edges.any { it.id == candidate }) {
            index++
            candidate = "e$index"
        }
        return candidate
    }
}

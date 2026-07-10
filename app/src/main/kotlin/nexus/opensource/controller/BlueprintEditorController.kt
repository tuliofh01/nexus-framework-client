package nexus.opensource.controller

import nexus.opensource.core.RepoRoot
import nexus.opensource.core.model.AppType
import nexus.opensource.core.model.BlueprintEdge
import nexus.opensource.core.model.BlueprintFile
import nexus.opensource.core.model.BlueprintJson
import nexus.opensource.core.model.BlueprintNode
import nexus.opensource.core.model.BlueprintNodeType
import nexus.opensource.core.model.BlueprintPosition
import nexus.opensource.core.service.BlueprintValidator
import nexus.opensource.core.service.ProjectGenerator
import java.nio.file.Files
import java.nio.file.Path

/**
 * Holds blueprint graph state for the v1 Compose JSON/canvas editor.
 * v1.1 will embed imnodes via native interop; this controller keeps the same schema.
 */
class BlueprintEditorController(
    private val validator: BlueprintValidator = BlueprintValidator(),
) {
    var blueprint: BlueprintFile = BlueprintJson.sampleApp("MyApp", AppType.DESKTOP)
        private set
    var appType: AppType = AppType.DESKTOP
        private set
    var projectName: String = "MyApp"
        private set
    var selectedNodeId: String? = null
    var pendingEdgeSourceId: String? = null
    var statusMessage: String = ""
    var validationErrors: List<String> = emptyList()

    private var loadedFromPath: Path? = null

    fun syncContext(projectName: String, appType: AppType) {
        if (this.projectName == projectName && this.appType == appType && loadedFromPath == null) {
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
            nexus.opensource.core.model.ProjectSpec(
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
        if (sourceId == targetId) {
            statusMessage = "Edge source and target must differ"
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

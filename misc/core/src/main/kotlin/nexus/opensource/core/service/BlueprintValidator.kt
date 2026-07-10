package nexus.opensource.core.service

import nexus.opensource.core.model.BlueprintFile
import nexus.opensource.core.model.BlueprintNodeType

data class BlueprintValidationResult(
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val isValid: Boolean get() = errors.isEmpty()
}

class BlueprintValidator {
    fun validate(blueprint: BlueprintFile): BlueprintValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (blueprint.name.isBlank()) {
            errors += "Blueprint name must not be blank"
        }

        val nodeIds = blueprint.nodes.map { it.id }
        val duplicateIds = nodeIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateIds.isNotEmpty()) {
            errors += "Duplicate node ids: ${duplicateIds.joinToString()}"
        }

        val nodeIdSet = nodeIds.toSet()
        for (node in blueprint.nodes) {
            if (node.id.isBlank()) {
                errors += "Node id must not be blank"
            }
            if (BlueprintNodeType.fromId(node.type) == null) {
                errors += "Unknown node type '${node.type}' on node '${node.id}'"
            }
        }

        val edgeIds = blueprint.edges.map { it.id }
        val duplicateEdgeIds = edgeIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateEdgeIds.isNotEmpty()) {
            errors += "Duplicate edge ids: ${duplicateEdgeIds.joinToString()}"
        }

        for (edge in blueprint.edges) {
            if (edge.id.isBlank()) {
                errors += "Edge id must not be blank"
            }
            if (edge.source !in nodeIdSet) {
                errors += "Edge '${edge.id}' references missing source '${edge.source}'"
            }
            if (edge.target !in nodeIdSet) {
                errors += "Edge '${edge.id}' references missing target '${edge.target}'"
            }
            if (edge.port.isBlank()) {
                warnings += "Edge '${edge.id}' has no port label"
            }
        }

        if (blueprint.nodes.isEmpty()) {
            warnings += "Blueprint has no nodes"
        }

        return BlueprintValidationResult(errors = errors, warnings = warnings)
    }

    fun requireValid(blueprint: BlueprintFile) {
        val result = validate(blueprint)
        if (!result.isValid) {
            error("Invalid blueprint: ${result.errors.joinToString("; ")}")
        }
    }
}

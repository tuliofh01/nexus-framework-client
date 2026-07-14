package nexus.opensource.framework.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Langflow export JSON — ReactFlow-style node/edge graph.
 * Schema: https://www.langflow.org/api/v1/flows/{id} or Export button.
 *
 * This file defines the read-only DTOs for consuming a Langflow export.
 * The [LangflowTransformationEngine] maps these to [FlowsFile].
 *
 * ## Langflow → Nexus trigger inference
 *
 * | Langflow node type    | Nexus trigger type | Flow mode |
 * |-----------------------|--------------------|-----------|
 * | ChatInput / TextInput | manual             | triggered |
 * | Webhook               | event              | triggered |
 * | Schedule              | interval (ms: 5000)| background |
 * | (no input node)       | startup            | triggered |
 * | Agent / LLM / Tool    | → becomes step     | (inherits)|
 */
@Serializable
data class LangflowExport(
    val id: String? = null,
    val name: String = "",
    val description: String = "",
    val data: LangflowGraph? = null,
)

@Serializable
data class LangflowGraph(
    val nodes: List<LangflowNode> = emptyList(),
    val edges: List<LangflowEdge> = emptyList(),
)

@Serializable
data class LangflowNode(
    val id: String,
    val type: String = "",
    @SerialName("data")
    val nodeData: JsonElement? = null,
    val position: LangflowPosition? = null,
)

@Serializable
data class LangflowPosition(
    val x: Double = 0.0,
    val y: Double = 0.0,
)

@Serializable
data class LangflowEdge(
    val id: String? = null,
    val source: String,
    val target: String,
    val sourceHandle: String? = null,
    val targetHandle: String? = null,
    val data: JsonElement? = null,
)

/**
 * Parsed Langflow node data — extracts the node template fields
 * from Langflow's nested JSON structure.
 */
data class LangflowNodeDetail(
    val id: String,
    val label: String,
    val nodeType: String,
    /** Inferred triggers based on Langflow node type */
    val inferredTrigger: FlowTriggerType,
    val inferredMode: FlowMode,
)

/**
 * Transient mapping result from a single Langflow node to a Nexus [FlowDefinition].
 */
data class LangflowMapping(
    val flow: FlowDefinition,
    val mappedNodeIds: List<String>,
)

object LangflowJson {
    val parser: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
    }

    fun read(text: String): LangflowExport = parser.decodeFromString(text)

    /**
     * Extract the node type from Langflow's nested [nodeData] JSON.
     * Langflow stores the type in `data.node.template.type` or `data.node.type`.
     */
    fun extractNodeType(node: LangflowNode): String {
        if (node.type.isNotBlank()) return node.type
        val data = node.nodeData ?: return "Unknown"
        return data.jsonObject["node"]
            ?.jsonObject?.get("template")
            ?.jsonObject?.get("type")
            ?.jsonPrimitive?.content
            ?: data.jsonObject["node"]
                ?.jsonObject?.get("type")
                ?.jsonPrimitive?.content
                ?: "Unknown"
    }

    /**
     * Extract the node label from Langflow data.
     */
    fun extractNodeLabel(node: LangflowNode): String {
        val data = node.nodeData ?: return node.type.ifBlank { node.id }
        return data.jsonObject["node"]
            ?.jsonObject?.get("display_name")
            ?.jsonPrimitive?.content
            ?: data.jsonObject["node"]
                ?.jsonObject?.get("template")
                ?.jsonObject?.get("_display_name")
                ?.jsonPrimitive?.content
                ?: node.type.ifBlank { node.id }
    }

    /**
     * Infer the Nexus [FlowTriggerType] from a Langflow node type.
     */
    fun inferTriggerType(langflowNodeType: String): FlowTriggerType = when {
        langflowNodeType.contains("ChatInput", ignoreCase = true) ||
        langflowNodeType.contains("TextInput", ignoreCase = true) ||
        langflowNodeType.contains("Prompt", ignoreCase = true) -> FlowTriggerType.MANUAL

        langflowNodeType.contains("Webhook", ignoreCase = true) ||
        langflowNodeType.contains("WebhookInput", ignoreCase = true) -> FlowTriggerType.EVENT

        langflowNodeType.contains("Schedule", ignoreCase = true) ||
        langflowNodeType.contains("Timer", ignoreCase = true) -> FlowTriggerType.INTERVAL

        langflowNodeType.contains("Agent", ignoreCase = true) ||
        langflowNodeType.contains("LLM", ignoreCase = true) ||
        langflowNodeType.contains("ChatOutput", ignoreCase = true) ||
        langflowNodeType.contains("TextOutput", ignoreCase = true) -> FlowTriggerType.MANUAL

        else -> FlowTriggerType.STARTUP
    }

    /**
     * Infer the Nexus [FlowMode] from a Langflow node type.
     */
    fun inferMode(langflowNodeType: String): FlowMode = when {
        langflowNodeType.contains("Schedule", ignoreCase = true) ||
        langflowNodeType.contains("Timer", ignoreCase = true) -> FlowMode.BACKGROUND
        else -> FlowMode.TRIGGERED
    }

    const val SCHEMA_HINT = "https://nexus.dev/schemas/langflow-import-1"
}

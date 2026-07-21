package nexus.opensource.framework.core.service

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import nexus.opensource.framework.core.model.BlueprintEdge
import nexus.opensource.framework.core.model.BlueprintFile
import nexus.opensource.framework.core.model.BlueprintNode
import nexus.opensource.framework.core.model.BlueprintNodeType
import nexus.opensource.framework.core.model.BlueprintPosition
import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowsFile
import nexus.opensource.framework.core.model.LangflowExport
import nexus.opensource.framework.core.model.LangflowJson

/**
 * Builds a [BlueprintFile] canvas graph from a Langflow export or Nexus [FlowsFile]
 * so the Compose Blueprint Editor can show flows as nodes.
 *
 * Compatible with Langflow export JSON (ReactFlow-style). Nexus does not bundle or
 * execute Langflow. Langflow is a trademark of its respective owners; unaffiliated.
 *
 * Runtime automation remains in `flows/flows.json` via [LangflowTransformationEngine].
 * This mapper is for **visual authoring only** (`flow.automation` nodes).
 */
object LangflowBlueprintMapper {
    private val TRIGGER_HINTS = setOf(
        "ChatInput", "TextInput", "Webhook", "Schedule", "Timer", "Prompt",
    )

    /**
     * Map Langflow graph nodes/edges onto a blueprint canvas (1:1 layout when positions exist).
     */
    fun fromLangflowExport(export: LangflowExport): BlueprintFile {
        val graph = export.data
        if (graph == null || graph.nodes.isEmpty()) {
            return BlueprintFile(
                name = export.name.ifBlank { "Langflow import" },
                description = "Empty Langflow export — no nodes to display.",
            )
        }

        val nodes = graph.nodes.mapIndexed { index, lf ->
            val lfType = LangflowJson.extractNodeType(lf)
            val label = LangflowJson.extractNodeLabel(lf)
            val role = if (TRIGGER_HINTS.any { lfType.contains(it, ignoreCase = true) }) {
                "trigger"
            } else {
                "step"
            }
            val x = lf.position?.x?.toFloat() ?: (80f + (index % 4) * 200f)
            val y = lf.position?.y?.toFloat() ?: (80f + (index / 4) * 140f)
            BlueprintNode(
                id = sanitizeId(lf.id.ifBlank { "lf-$index" }),
                type = BlueprintNodeType.FLOW_AUTOMATION.id,
                position = BlueprintPosition(x = x, y = y),
                data = buildJsonObject {
                    put("label", label)
                    put("langflowType", lfType)
                    put("role", role)
                    put("flowId", export.id.orEmpty())
                },
            )
        }

        val nodeIds = nodes.map { it.id }.toSet()
        val edges = graph.edges.mapIndexedNotNull { index, edge ->
            val source = sanitizeId(edge.source)
            val target = sanitizeId(edge.target)
            if (source !in nodeIds || target !in nodeIds) return@mapIndexedNotNull null
            BlueprintEdge(
                id = edge.id?.takeIf { it.isNotBlank() } ?: "lf-e$index",
                source = source,
                target = target,
                port = edge.sourceHandle?.takeIf { it.isNotBlank() } ?: "out",
            )
        }

        return BlueprintFile(
            name = export.name.ifBlank { "Langflow import" },
            description = "Imported from Langflow export (visual). " +
                "Runtime stubs live in flows.json — review before enabling. " +
                "Compatible with Langflow; unaffiliated.",
            nodes = nodes,
            edges = edges,
        )
    }

    /**
     * Fallback canvas when only Nexus [FlowsFile] is available (e.g. open generated app).
     * Each flow becomes a hub node; each step becomes a linked child node.
     */
    fun fromFlowsFile(flows: FlowsFile, projectName: String = "Flows"): BlueprintFile {
        if (flows.flows.isEmpty()) {
            return BlueprintFile(
                name = "$projectName flows",
                description = "No flows to display on the blueprint canvas.",
            )
        }

        val nodes = mutableListOf<BlueprintNode>()
        val edges = mutableListOf<BlueprintEdge>()
        var edgeIndex = 1

        flows.flows.forEachIndexed { flowIndex, flow ->
            val hubId = sanitizeId("flow-${flow.id}")
            val hubX = 60f + (flowIndex % 3) * 320f
            val hubY = 60f + (flowIndex / 3) * 280f
            nodes += BlueprintNode(
                id = hubId,
                type = BlueprintNodeType.FLOW_AUTOMATION.id,
                position = BlueprintPosition(hubX, hubY),
                data = flowHubData(flow),
            )

            flow.steps.forEachIndexed { stepIndex, step ->
                val stepId = sanitizeId("$hubId-step-$stepIndex")
                nodes += BlueprintNode(
                    id = stepId,
                    type = BlueprintNodeType.FLOW_AUTOMATION.id,
                    position = BlueprintPosition(
                        x = hubX + 200f,
                        y = hubY + stepIndex * 90f,
                    ),
                    data = buildJsonObject {
                        put("label", step.target ?: step.type)
                        put("langflowType", step.type)
                        put("role", "step")
                        put("flowId", flow.id)
                    },
                )
                val prevId = if (stepIndex == 0) hubId else sanitizeId("$hubId-step-${stepIndex - 1}")
                edges += BlueprintEdge(
                    id = "e${edgeIndex++}",
                    source = prevId,
                    target = stepId,
                    port = if (stepIndex == 0) "trigger" else "next",
                )
            }
        }

        return BlueprintFile(
            name = "$projectName flows",
            description = "Flows visualized as blueprint nodes (from flows.json).",
            nodes = nodes,
            edges = edges,
        )
    }

    private fun flowHubData(flow: FlowDefinition) = buildJsonObject {
        put("label", flow.name.ifBlank { flow.id })
        put("langflowType", "Flow")
        put("role", "trigger")
        put("flowId", flow.id)
        put("mode", flow.mode)
        put("trigger", flow.trigger.type)
        put("enabled", flow.enabled.toString())
    }

    private fun sanitizeId(raw: String): String =
        raw.replace(Regex("[^A-Za-z0-9._-]"), "-").ifBlank { "node" }
}

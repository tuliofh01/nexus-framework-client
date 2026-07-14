package nexus.opensource.framework.core.service

import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowMode
import nexus.opensource.framework.core.model.FlowStep
import nexus.opensource.framework.core.model.FlowStepType
import nexus.opensource.framework.core.model.FlowTrigger
import nexus.opensource.framework.core.model.FlowTriggerType
import nexus.opensource.framework.core.model.FlowsFile
import nexus.opensource.framework.core.model.LangflowEdge
import nexus.opensource.framework.core.model.LangflowExport
import nexus.opensource.framework.core.model.LangflowJson
import nexus.opensource.framework.core.model.LangflowMapping
import nexus.opensource.framework.core.model.LangflowNode

/**
 * Transforms a [LangflowExport] (from Langflow's "Export flow" JSON) into
 * a [FlowsFile] that the Nexus runtime can execute.
 *
 * == SAFETY GUARANTEE ==
 * Every imported flow has [FlowDefinition.enabled] set to **false**.
 * The user must explicitly enable each flow after reviewing it.
 * This prevents accidentally shipping Langflow test flows in production.
 *
 * == INFERENCE RULES ==
 * - Input-type nodes (ChatInput, TextInput, Webhook, Schedule) determine
 *   the [FlowTriggerType] and [FlowMode] for the generated flow.
 * - All downstream nodes become [FlowStep]s in execution order (topological
 *   order based on [LangflowEdge] definitions).
 * - Unknown/unrecognized node types become `invoke` steps with
 *   `target = "nxs.<type>"` as a convention-based fallback.
 */
class LangflowTransformationEngine(
    private val validator: FlowsValidator = FlowsValidator(),
) {
    companion object {
        const val FLOW_ID_PREFIX = "imported-"
        const val DEFAULT_INTERVAL_MS: Long = 5000

        /**
         * Langflow node types that are treated as trigger sources
         * (they begin a flow rather than process data).
         */
        private val TRIGGER_TYPES = setOf(
            "ChatInput", "TextInput", "Webhook", "Schedule",
            "Timer", "ChatOutput", "TextOutput",
        )

        /**
         * Generic node types that map to a known [FlowStepType].
         */
        private fun inferStepType(langflowType: String): FlowStepType = when {
            langflowType.contains("Condition", ignoreCase = true) ||
            langflowType.contains("Filter", ignoreCase = true) ||
            langflowType.contains("Router", ignoreCase = true) -> FlowStepType.CONDITION

            langflowType.contains("Delay", ignoreCase = true) ||
            langflowType.contains("Wait", ignoreCase = true) -> FlowStepType.DELAY

            else -> FlowStepType.INVOKE
        }

        /**
         * Map a Langflow node type to a Nexus invocation target string.
         */
        private fun inferTarget(langflowType: String): String = when {
            langflowType.contains("LLM", ignoreCase = true) ||
            langflowType.contains("Agent", ignoreCase = true) -> "python:llm"

            langflowType.contains("Python", ignoreCase = true) ||
            langflowType.contains("Function", ignoreCase = true) ||
            langflowType.contains("Code", ignoreCase = true) -> "python:module"

            langflowType.contains("Lua", ignoreCase = true) ||
            langflowType.contains("Script", ignoreCase = true) -> "lua:script"

            langflowType.contains("Log", ignoreCase = true) ||
            langflowType.contains("Print", ignoreCase = true) -> "nxs.log"

            langflowType.contains("API", ignoreCase = true) ||
            langflowType.contains("HTTP", ignoreCase = true) ||
            langflowType.contains("Request", ignoreCase = true) -> "nxs.http"

            langflowType == "ChatOutput" || langflowType == "TextOutput" -> "nxs.output"

            else -> "nxs.${langflowType.lowercase().replace(" ", "_")}"
        }

        /** Condition step types that need a [whenExpr] */
        private val CONDITION_TYPES = setOf("Condition", "Filter", "Router")
        /** Delay step types that need [ms] */
        private val DELAY_TYPES = setOf("Delay", "Wait")
    }

    /**
     * Transform a raw Langflow JSON string into a [FlowsFile].
     *
     * @param rawJson The Langflow export JSON string.
     * @return [LangflowTransformationResult] with the mapped flows and any warnings.
     */
    fun transform(rawJson: String): LangflowTransformationResult {
        return try {
            val export = LangflowJson.read(rawJson)
            transformExport(export)
        } catch (e: Exception) {
            LangflowTransformationResult(
                warnings = listOf("Failed to parse Langflow JSON: ${e.message}"),
            )
        }
    }

    /**
     * Transform a parsed [LangflowExport] into a [FlowsFile].
     */
    fun transformExport(export: LangflowExport): LangflowTransformationResult {
        val graph = export.data ?: return LangflowTransformationResult(
            warnings = listOf("Langflow export has no graph data"),
        )

        if (graph.nodes.isEmpty()) {
            return LangflowTransformationResult(
                warnings = listOf("Langflow export has no nodes"),
            )
        }

        val warnings = mutableListOf<String>()
        val flows = mutableListOf<FlowDefinition>()

        // ── Step 1: Identify trigger nodes (inputs / entry points) ──
        val triggerNodes = findTriggerNodes(graph.nodes, graph.edges)

        if (triggerNodes.isEmpty()) {
            // No explicit trigger found — treat the whole graph as one flow
            val flow = buildFlowFromNodes(
                flowId = "${FLOW_ID_PREFIX}graph",
                name = export.name.ifBlank { "Imported Graph" },
                triggerType = FlowTriggerType.STARTUP,
                mode = FlowMode.TRIGGERED,
                nodes = graph.nodes,
                edges = graph.edges,
            )
            flows.add(flow.flow)
            return LangflowTransformationResult(
                flows = flows,
                warnings = warnings,
            )
        }

        // ── Step 2: For each trigger, generate one flow ──
        for ((index, triggerNode) in triggerNodes.withIndex()) {
            val langflowType = LangflowJson.extractNodeType(triggerNode)
            val triggerType = LangflowJson.inferTriggerType(langflowType)
            val mode = LangflowJson.inferMode(langflowType)

            val downstreamNodes = findDownstreamNodes(triggerNode.id, graph.nodes, graph.edges)

            val flowId = "${FLOW_ID_PREFIX}${export.name.lowercase().replace("\\s+".toRegex(), "_")}-flow-${index + 1}"

            val mapping = buildFlowFromNodes(
                flowId = flowId,
                name = "${export.name.ifBlank { "Flow ${index + 1}" }} (imported)",
                triggerType = triggerType,
                mode = mode,
                triggerValue = when (triggerType) {
                    FlowTriggerType.INTERVAL -> DEFAULT_INTERVAL_MS
                    FlowTriggerType.EVENT -> LangflowJson.extractNodeLabel(triggerNode)
                    else -> null
                },
                nodes = downstreamNodes,
                edges = graph.edges,
            )

            flows.add(mapping.flow)
        }

        // ── Step 3: Merge duplicate triggered flows ──
        val deduplicated = mergeDuplicateFlows(flows)
        val trimmedFlows = deduplicateFlowIds(deduplicated)

        // ── Step 4: Validate ──
        val flowsFile = FlowsFile(version = 1, flows = trimmedFlows)
        val validation = validator.validate(flowsFile)
        if (!validation.isValid) {
            warnings.addAll(validation.errors.map { "Validation: $it" })
        }
        warnings.addAll(validation.warnings)

        // ── Step 5: Build result ──
        return LangflowTransformationResult(
            flows = trimmedFlows,
            warnings = warnings,
            validation = validation,
        )
    }

    /**
     * Find nodes that act as flow triggers (input nodes with no incoming edges).
     */
    private fun findTriggerNodes(
        nodes: List<LangflowNode>,
        edges: List<LangflowEdge>,
    ): List<LangflowNode> {
        val targets = edges.map { it.target }.toSet()
        val candidates = nodes.filter { it.id !in targets }

        return candidates.filter { candidate ->
            val langflowType = LangflowJson.extractNodeType(candidate)
            langflowType in TRIGGER_TYPES || targets.isEmpty()
        }
    }

    /**
     * Find all nodes downstream of [rootId] using BFS over edges.
     */
    private fun findDownstreamNodes(
        rootId: String,
        nodes: List<LangflowNode>,
        edges: List<LangflowEdge>,
    ): List<LangflowNode> {
        val adjacency = mutableMapOf<String, MutableList<String>>()
        for (edge in edges) {
            adjacency.getOrPut(edge.source) { mutableListOf() } += edge.target
        }

        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        val downstreamIds = mutableListOf<String>()

        // Include the root trigger itself
        visited.add(rootId)
        queue.addLast(rootId)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (neighbor in adjacency[current].orEmpty()) {
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    downstreamIds.add(neighbor)
                    queue.addLast(neighbor)
                }
            }
        }

        return nodes.filter { it.id in downstreamIds || it.id == rootId }
    }

    /**
     * Build a single [FlowDefinition] from a set of Langflow nodes.
     *
     * @param flowId Unique flow identifier.
     * @param name Human-readable name.
     * @param triggerType Nexus trigger type inferred from Langflow type.
     * @param mode Background or triggered.
     * @param triggerValue Value for interval (ms) or event (name).
     * @param nodes Langflow nodes to map to steps.
     * @param edges Langflow edges for ordering.
     */
    private fun buildFlowFromNodes(
        flowId: String,
        name: String,
        triggerType: FlowTriggerType,
        mode: FlowMode,
        triggerValue: Any? = null,
        nodes: List<LangflowNode>,
        edges: List<LangflowEdge>,
    ): LangflowMapping {
        // Topological sort: build ordering from edges
        val orderedNodes = topologicalSort(nodes, edges)

        val trigger = when (triggerType) {
            FlowTriggerType.INTERVAL -> FlowTrigger(
                type = triggerType.id,
                ms = (triggerValue as? Long) ?: DEFAULT_INTERVAL_MS,
            )
            FlowTriggerType.EVENT -> FlowTrigger(
                type = triggerType.id,
                name = triggerValue as? String,
            )
            else -> FlowTrigger(type = triggerType.id)
        }

        val steps = orderedNodes
            .filter { node ->
                val langflowType = LangflowJson.extractNodeType(node)
                langflowType !in TRIGGER_TYPES
            }
            .sortedBy { topologicalSort(nodes, edges).indexOf(it) }
            .map { node ->
                val langflowType = LangflowJson.extractNodeType(node)
                val stepType = inferStepType(langflowType)

                FlowStep(
                    type = stepType.id,
                    target = if (stepType == FlowStepType.INVOKE) {
                        inferTarget(langflowType)
                    } else null,
                    args = listOf(LangflowJson.extractNodeLabel(node)),
                    whenExpr = if (stepType == FlowStepType.CONDITION) {
                        "true" // stub — user edits after import
                    } else null,
                    ms = if (stepType == FlowStepType.DELAY) {
                        1000L // default 1 second delay
                    } else null,
                )
            }

        val flow = FlowDefinition(
            id = flowId,
            name = name,
            enabled = false, // SAFETY: always disabled on import
            mode = mode.id,
            trigger = trigger,
            steps = steps.ifEmpty {
                listOf(
                    FlowStep(
                        type = FlowStepType.INVOKE.id,
                        target = "nxs.log",
                        args = listOf("Flow imported from Langflow — no executable steps mapped"),
                    ),
                )
            },
        )

        val mappedIds = orderedNodes.map { it.id }
        return LangflowMapping(flow = flow, mappedNodeIds = mappedIds)
    }

    /**
     * Simple topological sort based on edges.
     * Nodes without dependencies come first.
     */
    internal fun topologicalSort(
        nodes: List<LangflowNode>,
        edges: List<LangflowEdge>,
    ): List<LangflowNode> {
        val nodeMap = nodes.associateBy { it.id }

        // Build in-degree count
        val inDegree = mutableMapOf<String, Int>()
        val adjacency = mutableMapOf<String, MutableList<String>>()

        for (node in nodes) {
            inDegree[node.id] = 0
        }
        for (edge in edges) {
            val sourceId = edge.source
            val targetId = edge.target
            if (sourceId in nodeMap && targetId in nodeMap) {
                adjacency.getOrPut(sourceId) { mutableListOf() } += targetId
                inDegree[targetId] = (inDegree[targetId] ?: 0) + 1
            }
        }

        // Kahn's algorithm
        val queue = ArrayDeque<String>()
        for ((id, degree) in inDegree) {
            if (degree == 0) queue.addLast(id)
        }

        val sorted = mutableListOf<LangflowNode>()
        val visited = mutableSetOf<String>()

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue
            visited.add(current)
            nodeMap[current]?.let { sorted.add(it) }

            for (neighbor in adjacency[current].orEmpty()) {
                val newDegree = (inDegree[neighbor] ?: 1) - 1
                inDegree[neighbor] = newDegree
                if (newDegree == 0) {
                    queue.addLast(neighbor)
                }
            }
        }

        // Add any remaining nodes not reachable via edges
        for (node in nodes) {
            if (node.id !in visited) {
                sorted.add(node)
            }
        }

        return sorted
    }

    /**
     * Merge flows that have identical trigger types and trigger values.
     * Keeps the first occurrence, removes duplicates.
     */
    internal fun mergeDuplicateFlows(flows: List<FlowDefinition>): List<FlowDefinition> {
        val seen = mutableSetOf<String>()
        return flows.filter { flow ->
            val key = "${flow.trigger.type}:${flow.trigger.name ?: flow.trigger.ms ?: "startup"}"
            seen.add(key)
        }
    }

    /**
     * Ensure all flow IDs are unique by appending suffixes.
     */
    internal fun deduplicateFlowIds(flows: List<FlowDefinition>): List<FlowDefinition> {
        val idCount = mutableMapOf<String, Int>()
        return flows.map { flow ->
            val count = idCount.getOrDefault(flow.id, 0)
            idCount[flow.id] = count + 1
            if (count == 0) flow
            else flow.copy(id = "${flow.id}-${count}")
        }
    }
}

/**
 * Result of a Langflow→Nexus transformation.
 *
 * @property flows The generated [FlowDefinition]s (all disabled).
 * @property warnings Non-fatal issues encountered during transformation.
 * @property validation Optional validation result if validation was run.
 */
data class LangflowTransformationResult(
    val flows: List<FlowDefinition> = emptyList(),
    val warnings: List<String> = emptyList(),
    val validation: FlowsValidationResult? = null,
) {
    /** True if at least one flow was generated. */
    val hasFlows: Boolean get() = flows.isNotEmpty()
}

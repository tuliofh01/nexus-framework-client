package nexus.opensource.framework.core.service

import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowTrigger
import nexus.opensource.framework.core.model.LangflowEdge
import nexus.opensource.framework.core.model.LangflowNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LangflowTransformationEngineTest {
    private val engine = LangflowTransformationEngine()

    @Test
    fun minimalChatflowProducesDisabledFlow() {
        val json = loadFixture("langflow/minimal-chatflow.json")
        val result = engine.transform(json)
        assertTrue(result.hasFlows, "Should produce at least one flow")
        result.flows.forEach { flow ->
            assertFalse(flow.enabled, "All imported flows must be disabled")
        }
    }

    @Test
    fun minimalChatflowHasManualTrigger() {
        val json = loadFixture("langflow/minimal-chatflow.json")
        val result = engine.transform(json)
        val flow = result.flows.firstOrNull()
        assertNotNull(flow, "Should have a flow")
        assertEquals("manual", flow?.trigger?.type)
    }

    @Test
    fun agentWithToolsIsDisabledOnImport() {
        val json = loadFixture("langflow/agent-with-tools.json")
        val result = engine.transform(json)
        assertTrue(result.hasFlows, "Should produce flows")
        result.flows.forEach { flow ->
            assertFalse(flow.enabled, "Flow '${flow.name}' must be disabled on import")
        }
    }

    @Test
    fun agentWithToolsUsesEventTrigger() {
        val json = loadFixture("langflow/agent-with-tools.json")
        val result = engine.transform(json)
        val flow = result.flows.firstOrNull()
        assertNotNull(flow, "Should have a flow")
        assertEquals("event", flow?.trigger?.type, "Webhook should map to event trigger")
    }

    @Test
    fun scheduleTriggerProducesBackgroundMode() {
        val json = loadFixture("langflow/schedule-trigger.json")
        val result = engine.transform(json)
        val flow = result.flows.firstOrNull()
        assertNotNull(flow, "Should have a flow")
        assertEquals("background", flow?.mode, "Schedule should map to background mode")
        assertEquals("interval", flow?.trigger?.type, "Schedule should map to interval trigger")
    }

    @Test
    fun scheduleTriggerHasDefaultInterval() {
        val json = loadFixture("langflow/schedule-trigger.json")
        val result = engine.transform(json)
        val flow = result.flows.firstOrNull()
        assertNotNull(flow, "Should have a flow")
        val ms = flow?.trigger?.ms
        assertNotNull(ms, "Schedule trigger should have default ms")
        assertEquals(LangflowTransformationEngine.DEFAULT_INTERVAL_MS, ms)
    }

    @Test
    fun emptyJsonProducesWarningNoFlows() {
        val result = engine.transform("{}")
        assertFalse(result.hasFlows, "Empty export should not produce flows")
        assertTrue(result.warnings.isNotEmpty(), "Should have a warning")
    }

    @Test
    fun invalidJsonProducesWarning() {
        val result = engine.transform("not valid json {{{")
        assertFalse(result.hasFlows, "Invalid JSON should not produce flows")
        assertTrue(result.warnings.isNotEmpty(), "Should have a parse error warning")
    }

    @Test
    fun emptyNodesProducesWarning() {
        val json = """{"data": {"nodes": [], "edges": []}}"""
        val result = engine.transform(json)
        assertFalse(result.hasFlows)
        assertTrue(result.warnings.any { it.contains("no nodes") })
    }

    @Test
    fun topologicalSortOrdersCorrectly() {
        val nodes = listOf(
            LangflowNode(id = "a", type = "ChatInput"),
            LangflowNode(id = "b", type = "LLM"),
            LangflowNode(id = "c", type = "ChatOutput"),
            LangflowNode(id = "d", type = "Tool"),
        )
        val edges = listOf(
            LangflowEdge(source = "a", target = "b"),
            LangflowEdge(source = "b", target = "d"),
            LangflowEdge(source = "d", target = "c"),
        )
        val sorted = engine.topologicalSort(nodes, edges)
        val order = sorted.map { it.id }
        assertTrue(order.indexOf("a") < order.indexOf("b"), "a should come before b")
        assertTrue(order.indexOf("b") < order.indexOf("d"), "b should come before d")
        assertTrue(order.indexOf("d") < order.indexOf("c"), "d should come before c")
    }

    @Test
    fun mergeDuplicateFlowsDeduplicates() {
        val trigger = FlowTrigger(type = "manual")
        val flows = listOf(
            FlowDefinition(id = "a", trigger = trigger),
            FlowDefinition(id = "b", trigger = trigger),
            FlowDefinition(id = "c", trigger = FlowTrigger(type = "event", name = "test")),
        )
        val merged = engine.mergeDuplicateFlows(flows)
        assertEquals(2, merged.size, "Should merge same-trigger flows")
    }

    @Test
    fun deduplicateFlowIdsAppendsSuffix() {
        val flows = listOf(
            FlowDefinition(id = "imported-test-flow-1"),
            FlowDefinition(id = "imported-test-flow-1"),
        )
        val deduped = engine.deduplicateFlowIds(flows)
        assertEquals(2, deduped.size)
        assertEquals("imported-test-flow-1", deduped[0].id)
        assertEquals("imported-test-flow-1-1", deduped[1].id)
    }

    @Test
    fun transformResultValidatesCorrectly() {
        val json = loadFixture("langflow/minimal-chatflow.json")
        val result = engine.transform(json)
        val validation = result.validation
        assertNotNull(validation, "Validation should run on result")
        assertTrue(validation.isValid, "Imported flows should pass validation: ${validation.errors.joinToString()}")
    }

    private fun loadFixture(path: String): String {
        val resource = javaClass.classLoader.getResource(path)
            ?: throw IllegalStateException("Test fixture not found: $path")
        return resource.readText()
    }
}

package nexus.opensource.core.service

import nexus.opensource.core.model.BlueprintEdge
import nexus.opensource.core.model.BlueprintFile
import nexus.opensource.core.model.BlueprintJson
import nexus.opensource.core.model.BlueprintNode
import nexus.opensource.core.model.BlueprintNodeType
import nexus.opensource.core.model.BlueprintPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlueprintValidatorTest {
    private val validator = BlueprintValidator()

    @Test
    fun samplePlotterIsValid() {
        val blueprint = BlueprintJson.samplePlotter("TestApp", nexus.opensource.core.model.AppType.DESKTOP)
        val result = validator.validate(blueprint)
        assertTrue(result.isValid, result.errors.joinToString())
    }

    @Test
    fun rejectsDuplicateNodeIds() {
        val node = BlueprintNode("dup", BlueprintNodeType.CPP_MODEL.id, BlueprintPosition())
        val blueprint = BlueprintFile(
            name = "bad",
            nodes = listOf(node, node),
            edges = emptyList(),
        )
        val result = validator.validate(blueprint)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate node ids") })
    }

    @Test
    fun rejectsEdgeWithMissingTarget() {
        val blueprint = BlueprintFile(
            name = "bad",
            nodes = listOf(
                BlueprintNode("a", BlueprintNodeType.CPP_MODEL.id, BlueprintPosition()),
            ),
            edges = listOf(BlueprintEdge("e1", "a", "missing", "port")),
        )
        val result = validator.validate(blueprint)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("missing target") })
    }

    @Test
    fun rejectsUnknownNodeType() {
        val blueprint = BlueprintFile(
            name = "bad",
            nodes = listOf(BlueprintNode("x", "unknown.type", BlueprintPosition())),
            edges = emptyList(),
        )
        val result = validator.validate(blueprint)
        assertFalse(result.isValid)
        assertEquals(1, result.errors.count { it.contains("Unknown node type") })
    }
}

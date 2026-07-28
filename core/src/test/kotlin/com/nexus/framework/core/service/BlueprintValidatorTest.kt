package com.nexus.framework.core.service

import com.nexus.framework.core.RepoRoot
import com.nexus.framework.core.model.AppType
import com.nexus.framework.core.model.BlueprintEdge
import com.nexus.framework.core.model.BlueprintFile
import com.nexus.framework.core.model.BlueprintJson
import com.nexus.framework.core.model.BlueprintNode
import com.nexus.framework.core.model.BlueprintNodeType
import com.nexus.framework.core.model.BlueprintPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BlueprintValidatorTest {
    private val validator = BlueprintValidator()

    @Test
    fun sampleAppIsValid() {
        val blueprint = BlueprintJson.sampleApp("TestApp", com.nexus.framework.core.model.AppType.DESKTOP)
        val result = validator.validate(blueprint)
        assertTrue(result.isValid, result.errors.joinToString())
    }

    @Test
    fun samplePlotterIsValid() {
        val blueprint = BlueprintJson.samplePlotter("TestApp", com.nexus.framework.core.model.AppType.DESKTOP)
        val result = validator.validate(blueprint)
        assertTrue(result.isValid, result.errors.joinToString())
    }

    @Test
    fun bundledTemplateBlueprintsAreValid() {
        val repoRoot = RepoRoot.resolve()
        val generator = ProjectGenerator(repoRoot)
        for (appType in AppType.entries) {
            val blueprint = generator.loadTemplateBlueprint(appType)
            val result = validator.validate(blueprint)
            assertTrue(result.isValid, "${appType.label}: ${result.errors.joinToString()}")
            val types = blueprint.nodes.map { it.type }.toSet()
            assertEquals(BlueprintNodeType.ALL.map { it.id }.toSet(), types)
        }
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

package nexus.opensource.core.service

import nexus.opensource.core.RepoRoot
import nexus.opensource.core.model.AppType
import nexus.opensource.core.model.FlowDefinition
import nexus.opensource.core.model.FlowMode
import nexus.opensource.core.model.FlowStep
import nexus.opensource.core.model.FlowStepType
import nexus.opensource.core.model.FlowTrigger
import nexus.opensource.core.model.FlowTriggerType
import nexus.opensource.core.model.FlowsFile
import nexus.opensource.core.model.FlowsJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlowsValidatorTest {
    private val validator = FlowsValidator()

    @Test
    fun samplePlotterIsValid() {
        val flows = FlowsJson.samplePlotter()
        val result = validator.validate(flows)
        assertTrue(result.isValid, result.errors.joinToString())
    }

    @Test
    fun bundledTemplateFlowsAreValid() {
        val repoRoot = RepoRoot.resolve()
        val generator = ProjectGenerator(repoRoot)
        for (appType in AppType.entries) {
            val flows = generator.loadTemplateFlows(appType)
            val result = validator.validate(flows)
            assertTrue(result.isValid, "${appType.label}: ${result.errors.joinToString()}")
            assertEquals(2, flows.flows.size)
        }
    }

    @Test
    fun rejectsDuplicateFlowIds() {
        val flow = FlowDefinition(
            id = "dup",
            mode = FlowMode.BACKGROUND.id,
            trigger = FlowTrigger(type = FlowTriggerType.INTERVAL.id, ms = 1000),
            steps = listOf(FlowStep(type = FlowStepType.INVOKE.id, target = "nxs.log", args = listOf("hi"))),
        )
        val flows = FlowsFile(flows = listOf(flow, flow))
        val result = validator.validate(flows)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Duplicate flow ids") })
    }

    @Test
    fun rejectsIntervalWithoutMs() {
        val flows = FlowsFile(
            flows = listOf(
                FlowDefinition(
                    id = "bad",
                    mode = FlowMode.BACKGROUND.id,
                    trigger = FlowTrigger(type = FlowTriggerType.INTERVAL.id),
                    steps = listOf(FlowStep(type = FlowStepType.INVOKE.id, target = "nxs.log")),
                ),
            ),
        )
        val result = validator.validate(flows)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("interval trigger requires positive 'ms'") })
    }

    @Test
    fun emptyFlowsIsValidWithWarning() {
        val result = validator.validate(FlowsFile(flows = emptyList()))
        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("no flows") })
    }
}

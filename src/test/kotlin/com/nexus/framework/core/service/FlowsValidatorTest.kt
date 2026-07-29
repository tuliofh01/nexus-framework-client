package com.nexus.framework.core.service

import com.nexus.framework.core.RepoRoot
import com.nexus.framework.core.model.AppType
import com.nexus.framework.core.model.FlowDefinition
import com.nexus.framework.core.model.FlowMode
import com.nexus.framework.core.model.FlowStep
import com.nexus.framework.core.model.FlowStepType
import com.nexus.framework.core.model.FlowTrigger
import com.nexus.framework.core.model.FlowTriggerType
import com.nexus.framework.core.model.FlowsFile
import com.nexus.framework.core.model.FlowsJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlowsValidatorTest {
    private val validator = FlowsValidator()

    @Test
    fun sampleAppIsValid() {
        val flows = FlowsJson.sampleApp()
        val result = validator.validate(flows)
        assertTrue(result.isValid, result.errors.joinToString())
    }

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

    @Test
    fun rejectsUnknownTriggerAndStepTypes() {
        val badTrigger = FlowsFile(
            flows = listOf(
                FlowDefinition(
                    id = "t1",
                    mode = FlowMode.BACKGROUND.id,
                    trigger = FlowTrigger(type = "not.a.trigger"),
                    steps = listOf(FlowStep(type = FlowStepType.INVOKE.id, target = "nxs.log")),
                ),
            ),
        )
        val triggerResult = validator.validate(badTrigger)
        assertFalse(triggerResult.isValid)
        assertTrue(triggerResult.errors.any { it.contains("Unknown trigger type") })

        val badStep = FlowsFile(
            flows = listOf(
                FlowDefinition(
                    id = "s1",
                    mode = FlowMode.BACKGROUND.id,
                    trigger = FlowTrigger(type = FlowTriggerType.INTERVAL.id, ms = 500),
                    steps = listOf(FlowStep(type = "not.a.step", target = "x")),
                ),
            ),
        )
        val stepResult = validator.validate(badStep)
        assertFalse(stepResult.isValid)
        assertTrue(stepResult.errors.any { it.contains("unknown step type") })
    }
}

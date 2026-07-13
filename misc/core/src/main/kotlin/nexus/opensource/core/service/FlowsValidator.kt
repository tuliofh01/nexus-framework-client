package nexus.opensource.core.service

import nexus.opensource.core.model.FlowDefinition
import nexus.opensource.core.model.FlowMode
import nexus.opensource.core.model.FlowStepType
import nexus.opensource.core.model.FlowTriggerType
import nexus.opensource.core.model.FlowsFile

data class FlowsValidationResult(
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val isValid: Boolean get() = errors.isEmpty()
}

class FlowsValidator {
    /** Mirrors what FlowRunner v1 can execute — unknown step types are errors;
     *  condition/hotkey gaps are warnings until the native runner catches up.
     */
    fun validate(flows: FlowsFile): FlowsValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (flows.version < 1) {
            errors += "Unsupported flows version: ${flows.version}"
        }

        val flowIds = flows.flows.map { it.id }
        val duplicateIds = flowIds.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
        if (duplicateIds.isNotEmpty()) {
            errors += "Duplicate flow ids: ${duplicateIds.joinToString()}"
        }

        for (flow in flows.flows) {
            validateFlow(flow, errors, warnings)
        }

        if (flows.flows.isEmpty()) {
            warnings += "flows.json has no flows (runtime runner is a no-op)"
        }

        return FlowsValidationResult(errors = errors, warnings = warnings)
    }

    private fun validateFlow(
        flow: FlowDefinition,
        errors: MutableList<String>,
        warnings: MutableList<String>,
    ) {
        if (flow.id.isBlank()) {
            errors += "Flow id must not be blank"
        }
        if (FlowMode.fromId(flow.mode) == null) {
            errors += "Unknown mode '${flow.mode}' on flow '${flow.id}'"
        }
        validateTrigger(flow, errors, warnings)
        if (flow.steps.isEmpty()) {
            warnings += "Flow '${flow.id}' has no steps"
        }
        for ((index, step) in flow.steps.withIndex()) {
            validateStep(flow.id, index, step.type, step.target, step.ms, errors, warnings)
        }
    }

    private fun validateTrigger(
        flow: FlowDefinition,
        errors: MutableList<String>,
        warnings: MutableList<String>,
    ) {
        val triggerType = FlowTriggerType.fromId(flow.trigger.type)
        if (triggerType == null) {
            errors += "Unknown trigger type '${flow.trigger.type}' on flow '${flow.id}'"
            return
        }
        when (triggerType) {
            FlowTriggerType.INTERVAL -> {
                val ms = flow.trigger.ms
                if (ms == null || ms <= 0) {
                    errors += "Flow '${flow.id}': interval trigger requires positive 'ms'"
                }
                if (flow.mode != FlowMode.BACKGROUND.id) {
                    warnings += "Flow '${flow.id}': interval triggers are usually background mode"
                }
            }
            FlowTriggerType.EVENT -> {
                if (flow.trigger.name.isNullOrBlank()) {
                    errors += "Flow '${flow.id}': event trigger requires 'name'"
                }
            }
            FlowTriggerType.HOTKEY -> {
                if (flow.trigger.key.isNullOrBlank()) {
                    errors += "Flow '${flow.id}': hotkey trigger requires 'key'"
                }
            }
            FlowTriggerType.STARTUP, FlowTriggerType.MANUAL -> Unit
        }
    }

    private fun validateStep(
        flowId: String,
        index: Int,
        type: String,
        target: String?,
        delayMs: Long?,
        errors: MutableList<String>,
        warnings: MutableList<String>,
    ) {
        val stepType = FlowStepType.fromId(type)
        if (stepType == null) {
            errors += "Flow '$flowId' step $index: unknown step type '$type'"
            return
        }
        when (stepType) {
            FlowStepType.INVOKE -> {
                if (target.isNullOrBlank()) {
                    errors += "Flow '$flowId' step $index: invoke step requires 'target'"
                }
            }
            FlowStepType.CONDITION -> {
                warnings += "Flow '$flowId' step $index: condition steps are stubbed in v1"
            }
            FlowStepType.DELAY -> {
                if (delayMs == null || delayMs < 0) {
                    errors += "Flow '$flowId' step $index: delay step requires non-negative 'ms'"
                }
            }
        }
    }

    fun requireValid(flows: FlowsFile) {
        val result = validate(flows)
        if (!result.isValid) {
            error("Invalid flows: ${result.errors.joinToString("; ")}")
        }
    }
}

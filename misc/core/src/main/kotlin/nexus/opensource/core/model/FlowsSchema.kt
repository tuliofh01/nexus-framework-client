package nexus.opensource.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

private const val FLOWS_SCHEMA_URL = "https://nexus.dev/schemas/flows-1.json"

/**
 * Optional runtime automation graph — background loops and event-triggered services.
 * Distinct from [BlueprintFile] (design-time app structure).
 * Schema: [docs/templates/flows-schema.md].
 */
@Serializable
data class FlowsFile(
    val version: Int = 1,
    val flows: List<FlowDefinition> = emptyList(),
)

@Serializable
data class FlowDefinition(
    val id: String,
    val name: String = "",
    val enabled: Boolean = true,
    /** `background` = scheduler while app alive; `triggered` = fires on trigger only. */
    val mode: String = FlowMode.TRIGGERED.id,
    val trigger: FlowTrigger = FlowTrigger(),
    val steps: List<FlowStep> = emptyList(),
)

@Serializable
data class FlowTrigger(
    val type: String = FlowTriggerType.MANUAL.id,
    /** Interval in milliseconds when [type] is `interval`. */
    val ms: Long? = null,
    /** Event name when [type] is `event`. */
    val name: String? = null,
    /** Hotkey token when [type] is `hotkey`. */
    val key: String? = null,
)

@Serializable
data class FlowStep(
    val type: String = FlowStepType.INVOKE.id,
    /** Invoke target, e.g. `nxs.set_samples`, `python.module`, `lua.script`. */
    val target: String? = null,
    val args: List<String> = emptyList(),
    /** Condition expression stub for v1 (truthy string). */
    val whenExpr: String? = null,
    /** Delay milliseconds when [type] is `delay`. */
    val ms: Long? = null,
)

enum class FlowMode(val id: String, val label: String) {
    BACKGROUND("background", "Background"),
    TRIGGERED("triggered", "Triggered"),
    ;

    companion object {
        fun fromId(id: String): FlowMode? = entries.find { it.id == id }
    }
}

enum class FlowTriggerType(val id: String, val label: String) {
    INTERVAL("interval", "Interval timer"),
    EVENT("event", "App event"),
    HOTKEY("hotkey", "Hotkey"),
    STARTUP("startup", "App startup"),
    MANUAL("manual", "Manual (UI button)"),
    ;

    companion object {
        val ALL: List<FlowTriggerType> = entries.toList()

        fun fromId(id: String): FlowTriggerType? = entries.find { it.id == id }
    }
}

enum class FlowStepType(val id: String, val label: String) {
    INVOKE("invoke", "Invoke command"),
    CONDITION("condition", "Conditional branch"),
    DELAY("delay", "Delay"),
    ;

    companion object {
        val ALL: List<FlowStepType> = entries.toList()

        fun fromId(id: String): FlowStepType? = entries.find { it.id == id }
    }
}

@Serializable
data class FlowsConfigSpec(
    val path: String = "flows/flows.json",
    val enabled: Boolean = true,
)

object FlowsJson {
    val parser: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = false
    }

    fun read(text: String): FlowsFile = parser.decodeFromString(text)

    fun write(flows: FlowsFile): String = parser.encodeToString(flows)

    fun readRendered(text: String, vars: Map<String, String>): FlowsFile =
        read(TemplateSubst.substitute(text, vars))

    fun sampleApp(): FlowsFile = FlowsFile(
        version = 1,
        flows = listOf(
            FlowDefinition(
                id = "startup-log",
                name = "Startup log",
                enabled = true,
                mode = FlowMode.TRIGGERED.id,
                trigger = FlowTrigger(type = FlowTriggerType.STARTUP.id),
                steps = listOf(
                    FlowStep(
                        type = FlowStepType.INVOKE.id,
                        target = "nxs.log",
                        args = listOf("App started"),
                    ),
                ),
            ),
            FlowDefinition(
                id = "manual-trigger",
                name = "Manual ready event",
                enabled = true,
                mode = FlowMode.TRIGGERED.id,
                trigger = FlowTrigger(type = FlowTriggerType.EVENT.id, name = "app.ready"),
                steps = listOf(
                    FlowStep(
                        type = FlowStepType.INVOKE.id,
                        target = "nxs.log",
                        args = listOf("App ready"),
                    ),
                ),
            ),
        ),
    )

    /** Plotter-specific flows — see template/examples/plotter/. */
    fun samplePlotter(): FlowsFile = FlowsFile(
        version = 1,
        flows = listOf(
            FlowDefinition(
                id = "sync-curves",
                name = "Resample on timer",
                enabled = true,
                mode = FlowMode.BACKGROUND.id,
                trigger = FlowTrigger(type = FlowTriggerType.INTERVAL.id, ms = 5000),
                steps = listOf(
                    FlowStep(
                        type = FlowStepType.INVOKE.id,
                        target = "nxs.set_samples",
                        args = listOf("\${state.sampleCount}"),
                    ),
                ),
            ),
            FlowDefinition(
                id = "on-add",
                name = "Welcome toast",
                enabled = true,
                mode = FlowMode.TRIGGERED.id,
                trigger = FlowTrigger(type = FlowTriggerType.EVENT.id, name = "curve.added"),
                steps = listOf(
                    FlowStep(
                        type = FlowStepType.INVOKE.id,
                        target = "nxs.log",
                        args = listOf("Added curve"),
                    ),
                ),
            ),
        ),
    )

    const val SCHEMA_URL = FLOWS_SCHEMA_URL
    const val FILE_NAME = "flows.json"
    const val DEFAULT_PATH = "flows/flows.json"
}

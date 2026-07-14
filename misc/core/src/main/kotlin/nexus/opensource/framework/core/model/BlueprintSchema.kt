package nexus.opensource.framework.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private const val BLUEPRINT_SCHEMA_URL = "https://nexus.dev/schemas/blueprint-1.json"

/**
 * Langflow-style app graph consumed by the generator and runtime tooling.
 * Schema: [docs/templates/blueprint-schema.md].
 */
@Serializable
data class BlueprintFile(
    @SerialName("\$schema") val schema: String = BLUEPRINT_SCHEMA_URL,
    val name: String = "App flow",
    val description: String = "",
    val editor: BlueprintEditorMeta = BlueprintEditorMeta(),
    val nodes: List<BlueprintNode> = emptyList(),
    val edges: List<BlueprintEdge> = emptyList(),
)

@Serializable
data class BlueprintEditorMeta(
    val tool: String = "imnodes",
    val version: String = "0.5",
    @SerialName("grid_snap") val gridSnap: Int = 16,
    /** `langflow` = typed DAG (v1 default); `n8n` reserved for future automation hooks. */
    val paradigm: String = BlueprintParadigm.LANGFLOW.id,
)

@Serializable
data class BlueprintNode(
    val id: String,
    val type: String,
    val position: BlueprintPosition = BlueprintPosition(),
    val data: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class BlueprintPosition(
    val x: Float = 0f,
    val y: Float = 0f,
)

@Serializable
data class BlueprintEdge(
    val id: String,
    val source: String,
    val target: String,
    val port: String = "",
)

/** Conceptual authoring paradigm — all v1 node types are Langflow-style DAG nodes. */
enum class BlueprintParadigm(val id: String, val label: String) {
    LANGFLOW("langflow", "Langflow-style DAG"),
    /** Reserved for future runtime automation hooks (webhooks, schedules) — not v1 node types. */
    N8N("n8n", "n8n-style workflow"),
}

enum class BlueprintNodeType(
    val id: String,
    val label: String,
    val paradigm: BlueprintParadigm = BlueprintParadigm.LANGFLOW,
) {
    PYTHON_MODULE("python.module", "Python module"),
    CPP_MODEL("cpp.model", "C++ model"),
    CPP_CONTROLLER("cpp.controller", "C++ controller"),
    UI_PAGE("ui.page", "UI page"),
    LUA_SCRIPT("lua.script", "Lua script"),
    ;

    companion object {
        val ALL: List<BlueprintNodeType> = entries.toList()

        fun fromId(id: String): BlueprintNodeType? = entries.find { it.id == id }

        fun byParadigm(paradigm: BlueprintParadigm): List<BlueprintNodeType> =
            entries.filter { it.paradigm == paradigm }
    }
}

object BlueprintJson {
    val parser: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = false
    }

    fun read(text: String): BlueprintFile = parser.decodeFromString(text)

    fun write(blueprint: BlueprintFile): String = parser.encodeToString(blueprint)

    /** Substitute template placeholders before parsing (e.g. `{{projectName}}`). */
    fun readRendered(text: String, vars: Map<String, String>): BlueprintFile =
        read(TemplateSubst.substitute(text, vars))

    fun defaultDataFor(type: BlueprintNodeType, appType: AppType): JsonObject = when (type) {
        BlueprintNodeType.PYTHON_MODULE -> buildJsonObject {
            put(
                "source",
                if (appType == AppType.ANDROID) "app/src/main/python/helpers.py" else "python/helpers.py",
            )
            putJsonArray("exports") { add("greeting") }
            putJsonArray("packages") { }
        }
        BlueprintNodeType.CPP_MODEL -> buildJsonObject {
            put("class", "nxs::model::AppModel")
            putJsonArray("fields") { add("counter"); add("greeting") }
        }
        BlueprintNodeType.CPP_CONTROLLER -> buildJsonObject {
            put("class", "nxs::controller::AppController")
            putJsonArray("commands") { add("increment"); add("decrement"); add("reset") }
        }
        BlueprintNodeType.UI_PAGE -> buildJsonObject {
            put("source", "ui/ui.xhtml")
            put("controllerScript", "ui/ui.ts#AppPage")
            putJsonArray("widgets") {
                listOf("panel", "label", "button", "row").forEach { add(it) }
            }
        }
        BlueprintNodeType.LUA_SCRIPT -> buildJsonObject {
            put("source", "scripts/panels.lua")
            putJsonArray("hotkeys") { add("F1") }
        }
    }

    fun createNode(
        type: BlueprintNodeType,
        id: String,
        position: BlueprintPosition,
        appType: AppType,
    ): BlueprintNode = BlueprintNode(
        id = id,
        type = type.id,
        position = position,
        data = defaultDataFor(type, appType),
    )

    fun sampleApp(projectName: String, appType: AppType): BlueprintFile {
        val pySource = if (appType == AppType.ANDROID) {
            "app/src/main/python/helpers.py"
        } else {
            "python/helpers.py"
        }
        return BlueprintFile(
            name = "$projectName flow",
            description = "General-purpose Nexus app graph — edit in the Compose blueprint editor.",
            nodes = listOf(
                BlueprintNode(
                    id = "py-helpers",
                    type = BlueprintNodeType.PYTHON_MODULE.id,
                    position = BlueprintPosition(40f, 120f),
                    data = buildJsonObject {
                        put("source", pySource)
                        putJsonArray("exports") { add("greeting") }
                        putJsonArray("packages") { }
                    },
                ),
                BlueprintNode(
                    id = "model-app",
                    type = BlueprintNodeType.CPP_MODEL.id,
                    position = BlueprintPosition(320f, 40f),
                    data = defaultDataFor(BlueprintNodeType.CPP_MODEL, appType),
                ),
                BlueprintNode(
                    id = "controller-app",
                    type = BlueprintNodeType.CPP_CONTROLLER.id,
                    position = BlueprintPosition(320f, 220f),
                    data = defaultDataFor(BlueprintNodeType.CPP_CONTROLLER, appType),
                ),
                BlueprintNode(
                    id = "view-main",
                    type = BlueprintNodeType.UI_PAGE.id,
                    position = BlueprintPosition(620f, 120f),
                    data = defaultDataFor(BlueprintNodeType.UI_PAGE, appType),
                ),
                BlueprintNode(
                    id = "lua-panels",
                    type = BlueprintNodeType.LUA_SCRIPT.id,
                    position = BlueprintPosition(620f, 320f),
                    data = defaultDataFor(BlueprintNodeType.LUA_SCRIPT, appType),
                ),
            ),
            edges = listOf(
                BlueprintEdge("e1", "py-helpers", "controller-app", "greeting"),
                BlueprintEdge("e2", "controller-app", "model-app", "state"),
                BlueprintEdge("e3", "model-app", "view-main", "bindings"),
                BlueprintEdge("e4", "view-main", "controller-app", "commands"),
                BlueprintEdge("e5", "lua-panels", "controller-app", "commands"),
            ),
        )
    }

    /** Desmos-style plotter sample — see template/examples/plotter/. */
    fun samplePlotter(projectName: String, appType: AppType): BlueprintFile {
        val pySource = if (appType == AppType.ANDROID) {
            "app/src/main/python/functions.py"
        } else {
            "python/functions.py"
        }
        return BlueprintFile(
            name = "$projectName flow",
            description = "Langflow-style app graph — edit in the Compose blueprint editor (v1) or future imnodes native panel.",
            nodes = listOf(
                BlueprintNode(
                    id = "py-functions",
                    type = BlueprintNodeType.PYTHON_MODULE.id,
                    position = BlueprintPosition(40f, 120f),
                    data = buildJsonObject {
                        put("source", pySource)
                        putJsonArray("exports") { add("evaluate") }
                        putJsonArray("packages") { add("numpy") }
                    },
                ),
                BlueprintNode(
                    id = "model-registry",
                    type = BlueprintNodeType.CPP_MODEL.id,
                    position = BlueprintPosition(320f, 40f),
                    data = defaultDataFor(BlueprintNodeType.CPP_MODEL, appType),
                ),
                BlueprintNode(
                    id = "controller-plot",
                    type = BlueprintNodeType.CPP_CONTROLLER.id,
                    position = BlueprintPosition(320f, 220f),
                    data = defaultDataFor(BlueprintNodeType.CPP_CONTROLLER, appType),
                ),
                BlueprintNode(
                    id = "view-plotter",
                    type = BlueprintNodeType.UI_PAGE.id,
                    position = BlueprintPosition(620f, 120f),
                    data = defaultDataFor(BlueprintNodeType.UI_PAGE, appType),
                ),
                BlueprintNode(
                    id = "lua-panels",
                    type = BlueprintNodeType.LUA_SCRIPT.id,
                    position = BlueprintPosition(620f, 320f),
                    data = defaultDataFor(BlueprintNodeType.LUA_SCRIPT, appType),
                ),
            ),
            edges = listOf(
                BlueprintEdge("e1", "py-functions", "controller-plot", "evaluate"),
                BlueprintEdge("e2", "controller-plot", "model-registry", "sampleCache"),
                BlueprintEdge("e3", "model-registry", "view-plotter", "activeCurves"),
                BlueprintEdge("e4", "view-plotter", "controller-plot", "commands"),
                BlueprintEdge("e5", "lua-panels", "controller-plot", "commands"),
            ),
        )
    }

    const val SCHEMA_URL = BLUEPRINT_SCHEMA_URL
    const val FILE_NAME = "blueprint.json"
}

/** Minimal `{{key}}` substitution shared with [TemplateEngine]. */
internal object TemplateSubst {
    fun substitute(text: String, vars: Map<String, String>): String {
        var out = text
        for ((key, value) in vars) {
            out = out.replace("{{$key}}", value)
        }
        return out
    }
}

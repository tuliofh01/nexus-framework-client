package nexus.opensource.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Langflow-style app graph consumed by the generator and runtime tooling.
 * Schema: [docs/templates/blueprint-schema.md].
 */
@Serializable
data class BlueprintFile(
    @SerialName("\$schema") val schema: String = SCHEMA_URL,
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

enum class BlueprintNodeType(val id: String, val label: String) {
    PYTHON_MODULE("python.module", "Python module"),
    CPP_MODEL("cpp.model", "C++ model"),
    CPP_CONTROLLER("cpp.controller", "C++ controller"),
    UI_PAGE("ui.page", "UI page"),
    LUA_SCRIPT("lua.script", "Lua script"),
    ;

    companion object {
        val ALL: List<BlueprintNodeType> = entries.toList()

        fun fromId(id: String): BlueprintNodeType? = entries.find { it.id == id }
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
                if (appType == AppType.ANDROID) "app/src/main/python/functions.py" else "python/functions.py",
            )
            putJsonArray("exports") { add("evaluate") }
            putJsonArray("packages") { add("numpy") }
        }
        BlueprintNodeType.CPP_MODEL -> buildJsonObject {
            put("class", "nxs::model::FunctionRegistry")
            putJsonArray("catalog") {
                listOf("sine", "cosine", "gaussian", "polynomial", "damped", "sinc").forEach { add(it) }
            }
        }
        BlueprintNodeType.CPP_CONTROLLER -> buildJsonObject {
            put("class", "nxs::controller::PlotController")
            put(
                "settings",
                buildJsonObject {
                    put("xMin", -10)
                    put("xMax", 10)
                    put("sampleCount", 512)
                },
            )
        }
        BlueprintNodeType.UI_PAGE -> buildJsonObject {
            put("source", "ui/ui.xhtml")
            put("controllerScript", "ui/ui.ts#PlotterPage")
            putJsonArray("widgets") {
                listOf("combo", "list", "slider", "toggle", "plot").forEach { add(it) }
            }
        }
        BlueprintNodeType.LUA_SCRIPT -> buildJsonObject {
            put("source", "scripts/panels.lua")
            putJsonArray("hotkeys") { add("F1"); add("L") }
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

    const val SCHEMA_URL = "https://nexus.dev/schemas/blueprint-1.json"
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

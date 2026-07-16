package nexus.opensource.framework.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Schema v2 manifest written to every generated project as `nxs_config.json`.
 * Matches the bundled template files under `template/{desktop-app,android-app}/nxs_config.json`.
 */
@Serializable
data class NexusConfigFile(
    val nexus: NexusMeta = NexusMeta(),
    val project: NexusProject = NexusProject(),
    val scriptProtection: ScriptProtectionSpec? = null,
    val targets: Map<String, JsonObject> = emptyMap(),
    val features: Map<String, JsonElement> = emptyMap(),
    val allocator: AllocatorSpec? = null,
    val blueprint: Map<String, String> = emptyMap(),
    val flows: FlowsConfigSpec? = null,
    val build: NexusBuild = NexusBuild(),
)

@Serializable
data class ScriptProtectionSpec(
    val enabled: Boolean = true,
    val salt: String = "",
    val algorithm: String = "nxs-v1",
)

@Serializable
data class NexusMeta(
    val configVersion: Int = 2,
    val generatedBy: String = "nexus-framework",
    val template: String = "",
)

@Serializable
data class NexusProject(
    val name: String = "",
    val windowTitle: String = "",
    val version: String = "0.1.0",
    val cppStandard: String = "20",
    val applicationId: String? = null,
    val createdAt: String = "",
)

@Serializable
data class NexusBuild(
    val outputDir: String = "builds",
    val presets: String = "",
    val nativeBackend: String = "zig",
    val gradleModule: String? = null,
)

@Serializable
data class AllocatorSpec(
    val zigArena: Boolean = false,
)

object NexusConfigJson {
    val parser: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun read(text: String): NexusConfigFile = parser.decodeFromString(text)

    fun write(config: NexusConfigFile): String = parser.encodeToString(config)
}

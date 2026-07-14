package nexus.opensource.framework.core.service

import nexus.opensource.framework.core.model.BlueprintJson
import nexus.opensource.framework.core.model.FlowsJson
import nexus.opensource.framework.core.model.NexusConfigJson
import nexus.opensource.framework.core.model.ProjectSpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.UUID

data class GenerateOptions(
    val dryRun: Boolean = false,
    val verbose: Boolean = false,
    val force: Boolean = false,
)

/** Orchestrates template copy, placeholder render, and post-generation validation.
 *
 * Order matters: shared/ lands inside projectRoot so the generated project is
 * self-contained with all runtime helpers available at shared/runtime/.
 */
class ProjectGenerator(
    private val repoRoot: Path,
    private val templateEngine: TemplateEngine = TemplateEngine(),
    private val blueprintValidator: BlueprintValidator = BlueprintValidator(),
    private val flowsValidator: FlowsValidator = FlowsValidator(),
) {
    fun generate(
        spec: ProjectSpec,
        onProgress: (String) -> Unit = {},
        options: GenerateOptions = GenerateOptions(),
    ): Path {
        validate(spec)
        val engine = if (options.verbose) TemplateEngine(verbose = true) else templateEngine
        val vars = templateVars(spec)
        val projectRoot = resolveProjectRoot(spec)
        val templateDir = repoRoot.resolve(TEMPLATE_DIR).resolve(spec.appType.templateFolder)
        val sharedDir = repoRoot.resolve(TEMPLATE_DIR).resolve(SHARED_DIR)

        if (options.dryRun) {
            onProgress("Dry-run render: ${spec.appType.templateFolder} → $projectRoot")
            engine.dryRunRender(templateDir, vars).forEach { file ->
                val unresolved = if (file.unresolvedPlaceholders.isNotEmpty()) {
                    " [UNRESOLVED: ${file.unresolvedPlaceholders.joinToString()}]"
                } else {
                    ""
                }
                onProgress("  ${file.relativePath}$unresolved")
            }
            engine.dryRunRender(sharedDir, vars).forEach { file ->
                val unresolved = if (file.unresolvedPlaceholders.isNotEmpty()) {
                    " [UNRESOLVED: ${file.unresolvedPlaceholders.joinToString()}]"
                } else {
                    ""
                }
                onProgress("  shared/${file.relativePath}$unresolved")
            }
            return projectRoot
        }

        ensureProjectDirectory(projectRoot, options.force)
        onProgress("Rendering ${spec.appType.templateFolder}/ → $projectRoot")
        engine.copyTree(templateDir, projectRoot, vars, onProgress)

        val sharedDest = projectRoot.resolve(SHARED_DIR)
        if (Files.isDirectory(sharedDir)) {
            onProgress("Copying shared/ → $sharedDest")
            engine.copyTree(sharedDir, sharedDest, vars, onProgress)
            // Salt + project metadata become compile-time constants for ScriptArchive crypto.
            writeScriptProtectionHeader(sharedDest, vars, onProgress)
        }

        writeBlueprint(projectRoot, spec, vars, onProgress)
        writeFlows(projectRoot, spec, vars, onProgress)
        validateRenderedConfig(projectRoot, onProgress)
        validateRenderedBlueprint(projectRoot, onProgress)
        validateRenderedFlows(projectRoot, onProgress)
        onProgress("Done: $projectRoot")
        return projectRoot
    }

    fun loadTemplateBlueprint(appType: nexus.opensource.framework.core.model.AppType): nexus.opensource.framework.core.model.BlueprintFile {
        val path = repoRoot.resolve(TEMPLATE_DIR)
            .resolve(appType.templateFolder)
            .resolve(BlueprintJson.FILE_NAME)
        require(Files.isRegularFile(path)) { "Missing template ${BlueprintJson.FILE_NAME}: $path" }
        return BlueprintJson.read(Files.readString(path))
    }

    fun loadTemplateFlows(appType: nexus.opensource.framework.core.model.AppType): nexus.opensource.framework.core.model.FlowsFile {
        val path = repoRoot.resolve(TEMPLATE_DIR)
            .resolve(appType.templateFolder)
            .resolve(FlowsJson.DEFAULT_PATH)
        require(Files.isRegularFile(path)) { "Missing template ${FlowsJson.DEFAULT_PATH}: $path" }
        return FlowsJson.read(Files.readString(path))
    }

    fun templateVars(spec: ProjectSpec): Map<String, String> {
        val createdAt = Instant.now().toString()
        val salt = if (spec.scriptProtectionEnabled) UUID.randomUUID().toString() else ""
        val packageName = spec.projectName.lowercase().replace(Regex("[^a-z0-9]"), "")
        return mapOf(
            "projectName" to spec.projectName,
            "project_name" to spec.projectName.lowercase(),
            "packageName" to packageName.ifBlank { "app" },
            "windowTitle" to spec.windowTitle,
            "license" to spec.license,
            "cppStandard" to spec.cppStandard,
            "appType" to spec.appType.id,
            "createdAt" to createdAt,
            "scriptProtectionEnabled" to spec.scriptProtectionEnabled.toString(),
            "scriptProtectionSalt" to salt,
        )
    }

    private fun writeScriptProtectionHeader(
        sharedDest: Path,
        vars: Map<String, String>,
        onProgress: (String) -> Unit,
    ) {
        val templatePath = sharedDest.resolve(SCRIPT_PROTECTION_TEMPLATE)
        if (!Files.isRegularFile(templatePath)) {
            onProgress("  [warn] missing $SCRIPT_PROTECTION_TEMPLATE")
            return
        }
        val rendered = templateEngine.render(Files.readString(templatePath), vars)
        val outPath = sharedDest.resolve(SCRIPT_PROTECTION_HEADER)
        Files.writeString(outPath, rendered)
        onProgress("  ${SHARED_DIR}/runtime/ScriptProtectionConfig.hpp")
    }

    private fun validate(spec: ProjectSpec) {
        require(spec.projectName.matches(PROJECT_NAME_PATTERN)) {
            "Project name must start with a letter and contain only letters, digits, _ or -"
        }
    }

    private fun resolveProjectRoot(spec: ProjectSpec): Path {
        val output = Paths.get(spec.outputPath).toAbsolutePath().normalize()
        val endsWithProjectName = output.fileName.toString() == spec.projectName
        return if (endsWithProjectName) output else output.resolve(spec.projectName)
    }

    private fun ensureProjectDirectory(projectRoot: Path, force: Boolean) {
        if (!Files.exists(projectRoot)) {
            Files.createDirectories(projectRoot)
            return
        }
        val hasContent = Files.list(projectRoot).use { it.findAny().isPresent }
        if (hasContent && !force) {
            error("Project directory already exists and is not empty: $projectRoot (use --force to overwrite)")
        }
        if (hasContent && force) {
            Files.walk(projectRoot)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
        Files.createDirectories(projectRoot)
    }

    private fun writeBlueprint(
        projectRoot: Path,
        spec: ProjectSpec,
        vars: Map<String, String>,
        onProgress: (String) -> Unit,
    ) {
        val custom = spec.blueprint ?: return
        blueprintValidator.requireValid(custom)
        val path = projectRoot.resolve(BlueprintJson.FILE_NAME)
        val rendered = templateEngine.render(BlueprintJson.write(custom), vars)
        Files.writeString(path, rendered)
        onProgress("Wrote custom ${BlueprintJson.FILE_NAME} (${custom.nodes.size} nodes, ${custom.edges.size} edges)")
    }

    private fun writeFlows(
        projectRoot: Path,
        spec: ProjectSpec,
        vars: Map<String, String>,
        onProgress: (String) -> Unit,
    ) {
        val custom = spec.flows ?: return
        flowsValidator.requireValid(custom)
        val path = projectRoot.resolve(FlowsJson.DEFAULT_PATH)
        Files.createDirectories(path.parent)
        val rendered = templateEngine.render(FlowsJson.write(custom), vars)
        Files.writeString(path, rendered)
        onProgress("Wrote custom ${FlowsJson.DEFAULT_PATH} (${custom.flows.size} flows)")
    }

    private fun validateRenderedFlows(projectRoot: Path, onProgress: (String) -> Unit) {
        val path = projectRoot.resolve(FlowsJson.DEFAULT_PATH)
        if (!Files.isRegularFile(path)) {
            onProgress("  [info] no ${FlowsJson.DEFAULT_PATH} — flow runner disabled")
            return
        }
        try {
            val flows = FlowsJson.read(Files.readString(path))
            val result = flowsValidator.validate(flows)
            if (!result.isValid) {
                onProgress("  [warn] ${FlowsJson.DEFAULT_PATH}: ${result.errors.joinToString()}")
                return
            }
            result.warnings.forEach { onProgress("  [flows] $it") }
            onProgress("Validated ${FlowsJson.DEFAULT_PATH} (${flows.flows.size} flows)")
        } catch (e: Exception) {
            onProgress("  [warn] could not parse ${FlowsJson.DEFAULT_PATH}: ${e.message}")
        }
    }

    private fun validateRenderedBlueprint(projectRoot: Path, onProgress: (String) -> Unit) {
        val path = projectRoot.resolve(BlueprintJson.FILE_NAME)
        if (!Files.isRegularFile(path)) {
            onProgress("  [warn] missing ${BlueprintJson.FILE_NAME}")
            return
        }
        try {
            val blueprint = BlueprintJson.read(Files.readString(path))
            val result = blueprintValidator.validate(blueprint)
            if (!result.isValid) {
                onProgress("  [warn] ${BlueprintJson.FILE_NAME}: ${result.errors.joinToString()}")
                return
            }
            result.warnings.forEach { onProgress("  [blueprint] $it") }
            onProgress(
                "Validated ${BlueprintJson.FILE_NAME} " +
                    "(${blueprint.nodes.size} nodes, ${blueprint.edges.size} edges)",
            )
        } catch (e: Exception) {
            onProgress("  [warn] could not parse ${BlueprintJson.FILE_NAME}: ${e.message}")
        }
    }

    private fun validateRenderedConfig(projectRoot: Path, onProgress: (String) -> Unit) {
        val configPath = projectRoot.resolve(CONFIG_FILE)
        if (!Files.isRegularFile(configPath)) {
            onProgress("  [warn] missing $CONFIG_FILE")
            return
        }
        try {
            val config = NexusConfigJson.read(Files.readString(configPath))
            require(config.nexus.configVersion >= 1) {
                "Invalid nxs_config.json: unsupported configVersion"
            }
            require(config.project.name.isNotBlank()) {
                "Invalid nxs_config.json: project.name is blank after render"
            }
            val protection = config.scriptProtection?.enabled == true
            onProgress(
                "Validated $CONFIG_FILE (schema v${config.nexus.configVersion}, " +
                    "project=${config.project.name}, scriptProtection=$protection)",
            )
        } catch (e: Exception) {
            onProgress("  [warn] could not parse $CONFIG_FILE: ${e.message}")
        }
    }

    companion object {
        const val TEMPLATE_DIR = "template"
        const val SHARED_DIR = "shared"
        const val CONFIG_FILE = "nxs_config.json"
        const val DEFAULT_OUTPUT_PARENT = "builds/framework"
        const val SCRIPT_PROTECTION_TEMPLATE = "runtime/ScriptProtectionConfig.hpp.in"
        const val SCRIPT_PROTECTION_HEADER = "runtime/ScriptProtectionConfig.hpp"
        val PROJECT_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_-]*")

        fun defaultOutputPath(projectName: String): String =
            Paths.get(DEFAULT_OUTPUT_PARENT, projectName).toString()
    }
}

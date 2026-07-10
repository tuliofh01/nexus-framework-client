package nexus.opensource.core.service

import nexus.opensource.core.model.NexusConfigJson
import nexus.opensource.core.model.ProjectSpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class GenerateOptions(
    val dryRun: Boolean = false,
    val verbose: Boolean = false,
    val force: Boolean = false,
)

class ProjectGenerator(
    private val repoRoot: Path,
    private val templateEngine: TemplateEngine = TemplateEngine(),
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
            return projectRoot
        }

        ensureProjectDirectory(projectRoot, options.force)
        onProgress("Rendering ${spec.appType.templateFolder}/ → $projectRoot")
        engine.copyTree(templateDir, projectRoot, vars, onProgress)

        val sharedDest = projectRoot.parent.resolve(SHARED_DIR)
        if (Files.isDirectory(sharedDir)) {
            onProgress("Copying shared/ → $sharedDest")
            engine.copyTree(sharedDir, sharedDest, vars, onProgress)
        }

        validateRenderedConfig(projectRoot, onProgress)
        onProgress("Done: $projectRoot")
        return projectRoot
    }

    fun templateVars(spec: ProjectSpec): Map<String, String> = mapOf(
        "projectName" to spec.projectName,
        "project_name" to spec.projectName.lowercase(),
        "windowTitle" to spec.windowTitle,
        "license" to spec.license,
        "cppStandard" to spec.cppStandard,
        "appType" to spec.appType.id,
    )

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
            onProgress("Validated $CONFIG_FILE (schema v${config.nexus.configVersion}, project=${config.project.name})")
        } catch (e: Exception) {
            onProgress("  [warn] could not parse $CONFIG_FILE: ${e.message}")
        }
    }

    companion object {
        const val TEMPLATE_DIR = "template"
        const val SHARED_DIR = "shared"
        const val CONFIG_FILE = "nxs_config.json"
        const val DEFAULT_OUTPUT_PARENT = "builds/framework"
        val PROJECT_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_-]*")

        fun defaultOutputPath(projectName: String): String =
            Paths.get(DEFAULT_OUTPUT_PARENT, projectName).toString()
    }
}

package nexus.opensource.framework.cli

import nexus.opensource.framework.core.RepoRoot
import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.FlowsFile
import nexus.opensource.framework.core.model.FlowsJson
import nexus.opensource.framework.core.model.NexusBranding
import nexus.opensource.framework.core.model.ProjectSpec
import nexus.opensource.framework.core.service.GenerateOptions
import nexus.opensource.framework.core.service.LangflowTransformationEngine
import nexus.opensource.framework.core.service.ProjectGenerator
import nexus.opensource.framework.core.service.TemplateEngine
import java.nio.file.Files
import java.nio.file.Path

private const val CLIENT_VERSION = "1.0.2"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }
    try {
        when (args[0]) {
            "generate" -> runGenerate(args.drop(1))
            "import-langflow" -> runImportLangflow(args.drop(1))
            "version" -> println("${NexusBranding.FRAMEWORK_NAME} CLI $CLIENT_VERSION")
            "help", "--help", "-h" -> printUsage()
            else -> {
                System.err.println("Unknown command: ${args[0]}")
                printUsage()
                kotlin.system.exitProcess(1)
            }
        }
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
        kotlin.system.exitProcess(1)
    }
}

private fun runGenerate(args: List<String>) {
    val flags = parseFlags(args)
    val typeRaw = flags.require("type")
    val name = flags.require("name")
    val output = flags["output"] ?: ProjectGenerator.defaultOutputPath(name)
    val appType = AppType.fromCliArg(typeRaw)
    val dryRun = flags["dry-run"] == "true" || args.contains("--dry-run")
    val verbose = flags["verbose"] == "true" || TemplateEngine.isDebugEnabled()
    val force = flags["force"] == "true"
    val scriptProtection = flags["script-protection"]?.let { it != "false" } ?: true

    val spec = ProjectSpec(
        projectName = name,
        outputPath = output,
        appType = appType,
        windowTitle = flags["window-title"] ?: NexusBranding.windowTitle(name),
        cppStandard = flags["cpp-standard"] ?: "20",
        license = flags["license"] ?: "Nexus-1.0",
        scriptProtectionEnabled = scriptProtection,
    )

    val repoRoot = RepoRoot.resolve()
    val generator = ProjectGenerator(repoRoot)
    generator.generate(
        spec,
        onProgress = { line -> println(line) },
        options = GenerateOptions(dryRun = dryRun, verbose = verbose, force = force),
    )
}

/**
 * Import a Langflow export JSON into Nexus `flows.json` stubs (always disabled).
 * Credit: compatible with Langflow export format; Nexus does not run Langflow.
 */
private fun runImportLangflow(args: List<String>) {
    val flags = parseFlags(args)
    val file = flags["file"] ?: flags.require("input")
    val inputPath = Path.of(file)
    require(Files.isRegularFile(inputPath)) { "Langflow export not found: $inputPath" }

    val mergeInto = flags["merge-into"]?.let { Path.of(it) }
    val output = flags["output"]?.let { Path.of(it) }
        ?: mergeInto
        ?: inputPath.parent.resolve("flows.json")

    val raw = Files.readString(inputPath)
    val engine = LangflowTransformationEngine()
    val result = engine.transform(raw)
    result.warnings.forEach { System.err.println("warn: $it") }
    if (!result.hasFlows) {
        error("No flows produced from Langflow export")
    }

    val imported = FlowsFile(version = 1, flows = result.flows)
    val finalFlows = if (mergeInto != null && Files.isRegularFile(mergeInto)) {
        val existing = FlowsJson.read(Files.readString(mergeInto))
        val existingIds = existing.flows.map { it.id }.toSet()
        val merged = existing.flows + imported.flows.filter { it.id !in existingIds }
        FlowsFile(version = existing.version.coerceAtLeast(1), flows = merged)
    } else {
        imported
    }

    if (flags["dry-run"] == "true") {
        println(FlowsJson.write(finalFlows))
        println("Dry-run: would write ${finalFlows.flows.size} flow(s) → $output")
        return
    }

    Files.createDirectories(output.parent ?: output)
    Files.writeString(output, FlowsJson.write(finalFlows))
    println("Imported ${result.flows.size} Langflow flow(s) → $output (all enabled=false)")
    println("Compatible with Langflow export JSON; review stubs before enabling.")
}

private data class FlagMap(val values: Map<String, String>) {
    fun require(key: String): String =
        values[key]?.takeIf { it.isNotBlank() }
            ?: error("Missing required flag: --$key")

    operator fun get(key: String): String? = values[key]
}

private fun parseFlags(args: List<String>): FlagMap {
    val map = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        when {
            arg.startsWith("--") -> {
                val key = arg.removePrefix("--")
                val eq = key.indexOf('=')
                if (eq >= 0) {
                    map[key.substring(0, eq)] = key.substring(eq + 1)
                } else if (i + 1 < args.size && !args[i + 1].startsWith("--")) {
                    map[key] = args[i + 1]
                    i++
                } else {
                    map[key] = "true"
                }
            }
            arg.startsWith("-") && arg.length == 2 -> {
                val key = when (arg[1]) {
                    't' -> "type"
                    'n' -> "name"
                    'o' -> "output"
                    'f' -> "file"
                    else -> error("Unknown short flag: $arg")
                }
                if (i + 1 >= args.size) error("Missing value for $arg")
                map[key] = args[i + 1]
                i++
            }
        }
        i++
    }
    return FlagMap(map)
}

private fun printUsage() {
    println(
        """
        Nexus Framework CLI $CLIENT_VERSION

        Usage:
          ./gradlew :cli:run --args="COMMAND [OPTIONS]"

        Commands:
          generate         Scaffold a project from bundled templates
          import-langflow  Import Langflow export JSON → flows.json stubs
          version          Print CLI version
          help             Show this help

        generate options:
          --type desktop|android     App template (required)
          --name NAME                Project name (required)
          --output PATH              Output path (default: builds/framework/{name})
          --window-title TITLE       Window title override
          --cpp-standard 20          C++ standard (default: 20)
          --license Nexus-1.0        License id (default: Nexus License)
          --script-protection true   Encrypt lua.dat / python.dat (default: true)
          --dry-run                  Preview rendered paths without writing
          --force                    Overwrite non-empty output directory
          --verbose                  Verbose template engine logging

        import-langflow options:
          --file PATH                Langflow export JSON (required; -f)
          --output PATH              Write flows.json here (default: beside input)
          --merge-into PATH          Merge into an existing flows.json
          --dry-run                  Print JSON without writing

        Langflow credit: Nexus reads Langflow-compatible export JSON and emits
        disabled Nexus flow stubs for human review. Unaffiliated with Langflow.

        Examples:
          ./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
          ./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MyApp/flows/flows.json"
        """.trimIndent(),
    )
}

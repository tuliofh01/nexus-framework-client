package nexus.opensource.cli

import nexus.opensource.core.RepoRoot
import nexus.opensource.core.model.AppType
import nexus.opensource.core.model.NexusBranding
import nexus.opensource.core.model.ProjectSpec
import nexus.opensource.core.service.GenerateOptions
import nexus.opensource.core.service.ProjectGenerator
import nexus.opensource.core.service.TemplateEngine

private const val CLIENT_VERSION = "0.1.0"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }
    try {
        when (args[0]) {
            "generate" -> runGenerate(args.drop(1))
            "version" -> println(CLIENT_VERSION)
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

    val spec = ProjectSpec(
        projectName = name,
        outputPath = output,
        appType = appType,
        windowTitle = flags["window-title"] ?: NexusBranding.windowTitle(name),
        cppStandard = flags["cpp-standard"] ?: "20",
        license = flags["license"] ?: "Apache-2.0",
    )

    val repoRoot = RepoRoot.resolve()
    val generator = ProjectGenerator(repoRoot)
    generator.generate(
        spec,
        onProgress = { line -> println(line) },
        options = GenerateOptions(dryRun = dryRun, verbose = verbose, force = force),
    )
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
          generate   Scaffold a project from bundled templates
          version    Print CLI version
          help       Show this help

        generate options:
          --type desktop|android     App template (required)
          --name NAME                Project name (required)
          --output PATH              Output path (default: builds/framework/{name})
          --window-title TITLE       Window title override
          --cpp-standard 20          C++ standard (default: 20)
          --license Apache-2.0       License identifier
          --dry-run                  Preview rendered paths without writing
          --force                    Overwrite non-empty output directory
          --verbose                  Verbose template engine logging

        Examples:
          ./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
          ./gradlew :cli:run --args="generate --type android --name MyApp --output builds/framework/MyApp"
        """.trimIndent(),
    )
}

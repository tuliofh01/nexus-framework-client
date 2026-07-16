package nexus.opensource.framework.core.service

import java.nio.file.Files
import java.nio.file.Path

data class RenderedFilePreview(
    val relativePath: String,
    val contentPreview: List<String>,
    val unresolvedPlaceholders: Set<String>,
)

/**
 * Copies a template directory tree and substitutes `{{variable}}` placeholders in file
 * contents and relative path segments.
 */
class TemplateEngine(
    private val verbose: Boolean = isDebugEnabled(),
) {
    private val placeholder = Regex("\\{\\{([a-zA-Z0-9_]+)\\}\\}")

    fun render(text: String, vars: Map<String, String>): String =
        placeholder.replace(text) { match ->
            vars[match.groupValues[1]] ?: match.value
        }

    fun findUnresolvedPlaceholders(content: String): Set<String> =
        placeholder.findAll(content).map { it.value }.toSet()

    fun dryRunRender(
        sourceRoot: Path,
        vars: Map<String, String>,
        previewLines: Int = 3,
    ): List<RenderedFilePreview> {
        if (!Files.exists(sourceRoot)) {
            error("Template folder not found: $sourceRoot")
        }
        val results = mutableListOf<RenderedFilePreview>()
        Files.walk(sourceRoot).use { paths ->
            paths.filter { Files.isRegularFile(it) }.forEach { src ->
                val relativePath = sourceRoot.relativize(src).toString()
                val destRelative = render(relativePath, vars)
                val textLike = isTextLike(relativePath)
                val rendered = if (textLike) render(Files.readString(src), vars) else null
                val preview = if (previewLines > 0 && rendered != null) {
                    rendered.lineSequence().take(previewLines).toList()
                } else {
                    emptyList()
                }
                val unresolved =
                    rendered?.let(::findUnresolvedPlaceholders)
                        ?: findUnresolvedPlaceholders(destRelative)
                debugLog("dry-run: $destRelative (${unresolved.size} unresolved)")
                results += RenderedFilePreview(destRelative, preview, unresolved)
            }
        }
        return results.sortedBy { it.relativePath }
    }

    fun copyTree(
        sourceRoot: Path,
        destRoot: Path,
        vars: Map<String, String>,
        onProgress: (String) -> Unit = {},
    ) {
        if (!Files.exists(sourceRoot)) {
            error("Template folder not found: $sourceRoot")
        }
        debugLog("copyTree: $sourceRoot → $destRoot (${vars.size} vars)")
        Files.walk(sourceRoot).use { paths ->
            paths.filter { Files.isRegularFile(it) }.forEach { src ->
                val relativePath = sourceRoot.relativize(src).toString()
                val destRelative = render(relativePath, vars)
                val dest = destRoot.resolve(destRelative)
                Files.createDirectories(dest.parent)

                if (isTextLike(relativePath)) {
                    val rawContent = Files.readString(src)
                    val content = render(rawContent, vars)
                    Files.writeString(dest, content)
                    debugLog("  wrote $destRelative (${content.length} bytes)")
                } else {
                    Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                    debugLog("  copied $destRelative (binary)")
                }
                onProgress("  $destRelative")
            }
        }
    }

    fun isTextLike(path: String): Boolean {
        val lower = path.lowercase()
        return lower.endsWith(".cpp") || lower.endsWith(".cppm") || lower.endsWith(".hpp") ||
            lower.endsWith(".h") || lower.endsWith(".lua") || lower.endsWith(".py") ||
            lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".json") ||
            lower.endsWith(".toml") || lower.endsWith(".yaml") || lower.endsWith(".yml") ||
            lower.endsWith(".cmake") || lower.endsWith(".in") ||
            lower.endsWith("cmakelists.txt") ||
            lower.endsWith(".gitignore") || lower.endsWith(".clang-tidy") ||
            lower.endsWith(".clang-format") || lower.endsWith(".editorconfig") ||
            lower.endsWith(".luarc.json") || lower.endsWith(".properties") ||
            lower.endsWith(".gradle") || lower.endsWith(".kts") || lower.endsWith(".xml") ||
            lower.endsWith(".workflow") || lower.endsWith(".sh") || lower.endsWith(".java") ||
            lower.endsWith(".kt") || lower.endsWith(".dockerignore") || lower.endsWith(".clangd") ||
            lower.endsWith(".xhtml") || lower.endsWith(".ts") || lower.endsWith(".zig") ||
            lower.endsWith(".zon") || lower.endsWith(".bat") || lower.endsWith(".template")
    }

    private fun debugLog(message: String) {
        if (verbose) {
            System.err.println("[nexus-generate] $message")
        }
    }

    companion object {
        fun isDebugEnabled(): Boolean {
            val value = System.getenv("NEXUS_DEBUG")?.lowercase()
            return value == "1" || value == "true" || value == "yes"
        }
    }
}

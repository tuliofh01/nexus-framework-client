package nexus.opensource.framework.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Regex-based debugger that scans log entries against a configurable pattern list
 * to detect potential bugs, misconfigurations, and runtime anomalies.
 */
class DebuggerService {

    data class DebugPattern(
        val id: String,
        val label: String,
        val regex: Regex,
        val severity: Severity,
        val category: Category,
        val description: String,
    )

    enum class Severity { Error, Warning, Info }
    enum class Category { NullSafety, Resource, Concurrency, Config, Performance, Security, TypeSafety }

    data class DebugMatch(
        val pattern: DebugPattern,
        val line: String,
        val matchedGroup: String,
        val timestamp: Long = System.currentTimeMillis(),
    )

    data class DebuggerState(
        val log: List<DebugMatch> = emptyList(),
        val enabled: Boolean = true,
        val customPatterns: List<DebugPattern> = emptyList(),
    )

    // --------------- built-in patterns ---------------

    private val builtinPatterns: List<DebugPattern> = listOf(
        DebugPattern(
            id = "null-deref",
            label = "Null dereference",
            regex = Regex("""\.(?!\?)[a-zA-Z]+\s*!|!!\s*|nullable.*without\s*null\s*check""", setOf(RegexOption.IGNORE_CASE)),
            severity = DebuggerService.Severity.Error,
            category = DebuggerService.Category.NullSafety,
            description = "Possible null-pointer dereference without safe-call operator",
        ),
        DebugPattern(
            id = "uncaught-exception",
            label = "Uncaught exception",
            regex = Regex("""catch\s*\(\s*[a-zA-Z]+\s*\)\s*\{\s*\}"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.Resource,
            description = "Empty catch block — exception swallowed silently",
        ),
        DebugPattern(
            id = "hardcoded-cred",
            label = "Hardcoded credential",
            regex = Regex("""(password|secret|api[_-]?key|token)\s*[=:]\s*['\"][^'\"]+['\"]""", setOf(RegexOption.IGNORE_CASE)),
            severity = DebuggerService.Severity.Error,
            category = DebuggerService.Category.Security,
            description = "Potential hardcoded credential in source",
        ),
        DebugPattern(
            id = "thread-escape",
            label = "Thread escape",
            regex = Regex("""Thread\s*\(\s*\)\s*\.\s*start|GlobalScope\.launch"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.Concurrency,
            description = "Unmanaged thread or global coroutine scope — risk of leaks",
        ),
        DebugPattern(
            id = "magic-number",
            label = "Magic number",
            regex = Regex("""[^a-zA-Z]\d{4,}[^.\w]"""),
            severity = DebuggerService.Severity.Info,
            category = DebuggerService.Category.Config,
            description = "Numeric literal ≥4 digits without named constant",
        ),
        DebugPattern(
            id = "infinite-loop",
            label = "Unbounded loop",
            regex = Regex("""while\s*\(\s*true\s*\)|for\s*\(\s*;\s*;\s*\)"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.Performance,
            description = "Unbounded loop — could hang the process",
        ),
        DebugPattern(
            id = "type-cast",
            label = "Unsafe type cast",
            regex = Regex("""as\s+[A-Z]\w+(\s*\)|[^?])"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.TypeSafety,
            description = "Unsafe cast — use `as?` safe-cast instead",
        ),
        DebugPattern(
            id = "print-stmt",
            label = "Leftover debug print",
            regex = Regex("""(println|console\.log|print)\s*\(""", setOf(RegexOption.IGNORE_CASE)),
            severity = DebuggerService.Severity.Info,
            category = DebuggerService.Category.Config,
            description = "Debug print statement in production path",
        ),
        DebugPattern(
            id = "resource-leak",
            label = "Unclosed resource",
            regex = Regex("""(File|InputStream|OutputStream|Socket|Connection)\s*\([^)]*\)\s*[^.]"""),
            severity = DebuggerService.Severity.Error,
            category = DebuggerService.Category.Resource,
            description = "Resource opened without `.use {}` or explicit close",
        ),
        DebugPattern(
            id = "todo-left",
            label = "Unresolved TODO",
            regex = Regex("""TODO\(|FIXME\(|HACK\(|XXX\s*:""", setOf(RegexOption.IGNORE_CASE)),
            severity = DebuggerService.Severity.Info,
            category = DebuggerService.Category.Config,
            description = "Unresolved TODO/FIXME/HACK in code",
        ),
        DebugPattern(
            id = "improper-null-init",
            label = "Improper null initialization",
            regex = Regex("""var\s+\w+\s*[=:]\s*null"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.NullSafety,
            description = "Variable initialized to null — likely missing lateinit or delegate",
        ),
        DebugPattern(
            id = "race-condition",
            label = "Possible race condition",
            regex = Regex("""(synchronized|@Synchronized)\s*(?!.*volatile|.*Atomic)"""),
            severity = DebuggerService.Severity.Warning,
            category = DebuggerService.Category.Concurrency,
            description = "Synchronization without volatile/atomic — possible visibility issue",
        ),
        DebugPattern(
            id = "string-concat",
            label = "String concatenation in loop",
            regex = Regex("""for\s*\(.*\)\s*\{[^}]*\+=\s*\"\"\"\"[^\}]*\}"""),
            severity = DebuggerService.Severity.Info,
            category = DebuggerService.Category.Performance,
            description = "String concatenation in loop — use StringBuilder",
        ),
        DebugPattern(
            id = "assert-side-effect",
            label = "Assert with side effect",
            regex = Regex("""assert\s*\([^)]*=(={0,1})[^)]*\)"""),
            severity = DebuggerService.Severity.Error,
            category = DebuggerService.Category.TypeSafety,
            description = "Assert statement with assignment — side-effect in assertion",
        ),
    )

    // --------------- state ---------------

    private val _state = MutableStateFlow(DebuggerState())
    val state: StateFlow<DebuggerState> = _state.asStateFlow()

    val allPatterns: List<DebugPattern>
        get() = builtinPatterns + _state.value.customPatterns

    /**
     * Scan a single log line against all active patterns.
     * Returns matches found and updates internal state.
     */
    fun scan(line: String): List<DebugMatch> {
        if (!_state.value.enabled) return emptyList()

        val matches = allPatterns.mapNotNull { pattern ->
            pattern.regex.find(line)?.let { match ->
                DebugMatch(
                    pattern = pattern,
                    line = line,
                    matchedGroup = match.value,
                )
            }
        }

        if (matches.isNotEmpty()) {
            _state.value = _state.value.copy(
                log = _state.value.log + matches,
            )
        }
        return matches
    }

    /** Scan multiple lines at once. */
    fun scanLines(lines: List<String>): List<DebugMatch> = lines.flatMap { scan(it) }

    /** Toggle debugger on/off. */
    fun toggleEnabled() {
        _state.value = _state.value.copy(enabled = !_state.value.enabled)
    }

    /** Add a custom user-defined pattern at runtime. */
    fun addCustomPattern(pattern: DebugPattern) {
        _state.value = _state.value.copy(
            customPatterns = _state.value.customPatterns + pattern,
        )
    }

    /** Clear all log matches. */
    fun clearLog() {
        _state.value = _state.value.copy(log = emptyList())
    }

    /** Remove a custom pattern by ID. */
    fun removeCustomPattern(id: String) {
        _state.value = _state.value.copy(
            customPatterns = _state.value.customPatterns.filter { it.id != id },
        )
    }

    /** Get all built-in patterns (for UI display). */
    fun getBuiltinPatterns(): List<DebugPattern> = builtinPatterns

    /** Export current matches as a summary string. */
    fun exportSummary(): String {
        val grouped = _state.value.log.groupBy { it.pattern.category }
        return buildString {
            appendLine("=== Nexus Debugger Report ===")
            appendLine("Total matches: ${_state.value.log.size}")
            appendLine()
            grouped.forEach { (category, matches) ->
                appendLine("[$category] ${matches.size} match(es)")
                matches.take(5).forEach { match ->
                    appendLine("  • [${match.pattern.severity}] ${match.pattern.label}")
                    appendLine("    ${match.line.take(120)}")
                }
                if (matches.size > 5) {
                    appendLine("  ... and ${matches.size - 5} more")
                }
                appendLine()
            }
        }
    }
}

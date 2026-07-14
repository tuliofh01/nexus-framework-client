package nexus.opensource.framework.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory unitary test runner.
 *
 * Tests are defined as lambdas and executed immediately — no files written.
 * Results are collected in memory and deallocated after each run via [clearResults].
 *
 * Supports basic assertions and structured output for the debugger panel.
 */
class TestRunner {

    data class TestCase(
        val name: String,
        val category: String = "general",
        val body: suspend () -> TestResult,
    )

    data class TestResult(
        val passed: Boolean,
        val message: String = "",
        val durationMs: Long = 0,
        val expected: String = "",
        val actual: String = "",
    )

    data class TestRun(
        val testName: String,
        val category: String,
        val result: TestResult,
        val timestamp: Long = System.currentTimeMillis(),
    )

    data class RunnerState(
        val isRunning: Boolean = false,
        val runs: List<TestRun> = emptyList(),
        val summary: TestSummary = TestSummary(),
    )

    data class TestSummary(
        val total: Int = 0,
        val passed: Int = 0,
        val failed: Int = 0,
        val totalDurationMs: Long = 0,
    )

    private val _state = MutableStateFlow(RunnerState())
    val state: StateFlow<RunnerState> = _state.asStateFlow()

    private var _tests: MutableList<TestCase> = mutableListOf()

    // --------------- built-in assertions ---------------

    companion object {
        suspend fun assertEqual(expected: Any?, actual: Any?, message: String = ""): TestResult {
            val start = System.currentTimeMillis()
            val passed = expected == actual
            val duration = System.currentTimeMillis() - start
            return TestResult(
                passed = passed,
                message = if (passed) "OK" else message.ifEmpty { "Expected $expected but got $actual" },
                durationMs = duration,
                expected = expected?.toString() ?: "null",
                actual = actual?.toString() ?: "null",
            )
        }

        suspend fun assertTrue(actual: Boolean, message: String = ""): TestResult {
            val start = System.currentTimeMillis()
            val duration = System.currentTimeMillis() - start
            return TestResult(
                passed = actual,
                message = if (actual) "OK" else message.ifEmpty { "Expected true but got false" },
                durationMs = duration,
                expected = "true",
                actual = actual.toString(),
            )
        }

        suspend fun assertFalse(actual: Boolean, message: String = ""): TestResult {
            val start = System.currentTimeMillis()
            val duration = System.currentTimeMillis() - start
            return TestResult(
                passed = !actual,
                message = if (!actual) "OK" else message.ifEmpty { "Expected false but got true" },
                durationMs = duration,
                expected = "false",
                actual = actual.toString(),
            )
        }

        suspend fun assertThrows(expectedMessage: String? = null, block: suspend () -> Unit): TestResult {
            val start = System.currentTimeMillis()
            return try {
                block()
                TestResult(
                    passed = false,
                    message = "Expected exception but none was thrown",
                    durationMs = System.currentTimeMillis() - start,
                    expected = "exception${expectedMessage?.let { ": $it" } ?: ""}",
                    actual = "no exception",
                )
            } catch (e: Exception) {
                val msg = e.message ?: ""
                val matched = expectedMessage == null || msg.contains(expectedMessage)
                TestResult(
                    passed = matched,
                    message = if (matched) "OK: ${e::class.simpleName}" else "Expected message '$expectedMessage' but got '$msg'",
                    durationMs = System.currentTimeMillis() - start,
                    expected = expectedMessage ?: "any exception",
                    actual = "${e::class.simpleName}: $msg",
                )
            }
        }

        suspend fun assertNull(actual: Any?, message: String = ""): TestResult {
            val start = System.currentTimeMillis()
            val passed = actual == null
            val duration = System.currentTimeMillis() - start
            return TestResult(
                passed = passed,
                message = if (passed) "OK" else message.ifEmpty { "Expected null but got $actual" },
                durationMs = duration,
                expected = "null",
                actual = actual?.toString() ?: "null",
            )
        }

        suspend fun assertNotNull(actual: Any?, message: String = ""): TestResult {
            val start = System.currentTimeMillis()
            val passed = actual != null
            val duration = System.currentTimeMillis() - start
            return TestResult(
                passed = passed,
                message = if (passed) "OK" else message.ifEmpty { "Expected non-null but got null" },
                durationMs = duration,
                expected = "non-null",
                actual = actual?.toString() ?: "null",
            )
        }
    }

    // --------------- test lifecycle ---------------

    /** Register a test case. Tests live in memory until [clearTests] is called. */
    fun register(test: TestCase) {
        _tests.add(test)
    }

    /** Register multiple tests at once. */
    fun registerAll(tests: List<TestCase>) {
        _tests.addAll(tests)
    }

    /** Remove all registered tests from memory. */
    fun clearTests() {
        _tests.clear()
    }

    /** Remove all test results from memory. */
    fun clearResults() {
        _state.value = _state.value.copy(runs = emptyList(), summary = TestSummary())
    }

    /** Remove both tests and results from memory — full deallocation. */
    fun clearAll() {
        _tests.clear()
        _state.value = RunnerState()
    }

    /**
     * Run all registered tests and collect results.
     * Tests are executed sequentially. Results accumulate in memory until cleared.
     */
    suspend fun runAll() {
        if (_state.value.isRunning) return
        _state.value = _state.value.copy(isRunning = true)

        var passed = 0
        var failed = 0
        var totalDuration = 0L
        val newRuns = mutableListOf<TestRun>()

        for (test in _tests) {
            val start = System.currentTimeMillis()
            val result = try {
                test.body()
            } catch (e: Exception) {
                TestResult(
                    passed = false,
                    message = "Uncaught exception: ${e::class.simpleName}: ${e.message}",
                    durationMs = System.currentTimeMillis() - start,
                    actual = "${e::class.simpleName}: ${e.message}",
                )
            }

            val run = TestRun(
                testName = test.name,
                category = test.category,
                result = result,
            )
            newRuns.add(run)

            if (result.passed) passed++ else failed++
            totalDuration += result.durationMs
        }

        _state.value = _state.value.copy(
            isRunning = false,
            runs = _state.value.runs + newRuns,
            summary = TestSummary(
                total = _state.value.runs.size + newRuns.size,
                passed = _state.value.summary.passed + passed,
                failed = _state.value.summary.failed + failed,
                totalDurationMs = _state.value.summary.totalDurationMs + totalDuration,
            ),
        )
    }

    /** Run a single named test. */
    suspend fun runSingle(name: String) {
        val test = _tests.find { it.name == name } ?: return
        if (_state.value.isRunning) return

        _state.value = _state.value.copy(isRunning = true)

        val start = System.currentTimeMillis()
        val result = try {
            test.body()
        } catch (e: Exception) {
            TestResult(
                passed = false,
                message = "Uncaught exception: ${e::class.simpleName}: ${e.message}",
                durationMs = System.currentTimeMillis() - start,
                actual = "${e::class.simpleName}: ${e.message}",
            )
        }

        val run = TestRun(
            testName = test.name,
            category = test.category,
            result = result,
        )

        val prev = _state.value.summary
        _state.value = _state.value.copy(
            isRunning = false,
            runs = _state.value.runs + run,
            summary = TestSummary(
                total = _state.value.runs.size + 1,
                passed = prev.passed + (if (result.passed) 1 else 0),
                failed = prev.failed + (if (result.passed) 0 else 1),
                totalDurationMs = prev.totalDurationMs + result.durationMs,
            ),
        )
    }

    /** Register the built-in smoke tests. */
    fun registerBuiltinTests() {
        registerAll(
            listOf(
                TestCase("math.addition", "core") {
                    assertEqual(2 + 2, 4, "Basic addition failed")
                },
                TestCase("math.null_check", "core") {
                    assertNull(null, "Null check failed")
                },
                TestCase("math.truth", "core") {
                    assertTrue(true, "Truth check failed")
                },
                TestCase("math.exception_catch", "core") {
                    assertThrows(null) {
                        throw RuntimeException("test error")
                    }
                },
                TestCase("string.concat", "core") {
                    assertEqual("hello " + "world", "hello world", "String concat failed")
                },
                TestCase("debugger.patterns_nonempty", "debugger") {
                    assertTrue(DebuggerService().allPatterns.isNotEmpty(), "Debugger should have patterns")
                },
                TestCase("model.counter_default", "model") {
                    val model = DebuggerService.DebuggerState()
                    assertEqual(model.enabled, true, "Debugger should be enabled by default")
                },
            )
        )
    }

    /** Generate a human-readable report from current results. */
    fun report(): String = buildString {
        val s = _state.value.summary
        appendLine("=== Test Runner Report ===")
        appendLine("Total: ${s.total} | Passed: ${s.passed} | Failed: ${s.failed} | ${s.totalDurationMs}ms")
        appendLine()
        if (_state.value.runs.isEmpty()) {
            appendLine("No tests run yet.")
            return@buildString
        }
        val byCategory = _state.value.runs.groupBy { it.category }
        byCategory.forEach { (category, runs) ->
            appendLine("[$category]")
            runs.forEach { run ->
                val icon = if (run.result.passed) "[PASS]" else "[FAIL]"
                appendLine("  $icon ${run.testName} (${run.result.durationMs}ms)")
                if (!run.result.passed) {
                    appendLine("       ${run.result.message}")
                }
            }
            appendLine()
        }
    }
}

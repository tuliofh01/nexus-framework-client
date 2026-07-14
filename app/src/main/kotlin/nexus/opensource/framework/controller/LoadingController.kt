package nexus.opensource.framework.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for the loading/splash screen.
 * Simulates environment checks and initialization before transitioning
 * to the main application screens.
 */
class LoadingController {

    data class LoadingState(
        val currentStep: Int = 0,
        val totalSteps: Int = 5,
        val stepLabel: String = "",
        val isComplete: Boolean = false,
        val message: String = "Initializing...",
    )

    data class Step(val label: String, val action: suspend () -> Unit = {})

    val steps: List<Step> = listOf(
        Step("Checking environment..."),
        Step("Loading modules..."),
        Step("Initializing services..."),
        Step("Preparing generator pipeline..."),
        Step("Ready!"),
    )

    private val _state = MutableStateFlow(LoadingState())
    val state: StateFlow<LoadingState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /** Runs the loading sequence. Returns when complete. */
    suspend fun runSequence() {
        for (i in steps.indices) {
            _state.value = _state.value.copy(
                currentStep = i + 1,
                stepLabel = steps[i].label,
                message = "Step ${i + 1}/${steps.size}: ${steps[i].label}",
            )
            // Simulate work — realistic delay per step
            delay(400L + (i * 150L))
        }
        _state.value = _state.value.copy(isComplete = true, message = "Ready.")
    }

    fun cancel() {
        scope.cancel()
    }
}

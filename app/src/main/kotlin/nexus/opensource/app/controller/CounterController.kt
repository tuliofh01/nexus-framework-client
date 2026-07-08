package nexus.opensource.app.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import nexus.opensource.app.model.CounterModel

/**
 * Controller layer: owns the mutable state and exposes the actions the view can invoke.
 * Compose observes [model] through the snapshot state system, so views recompose on change.
 */
class CounterController {

    var model: CounterModel by mutableStateOf(CounterModel())
        private set

    fun increment() {
        model = model.incremented()
    }

    fun decrement() {
        model = model.decremented()
    }

    fun reset() {
        model = CounterModel()
    }
}

package nexus.opensource.app.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nexus.opensource.app.controller.CounterController
import nexus.opensource.app.model.CounterModel
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * View layer: stateless composables. All state lives in the controller,
 * all data comes from the model.
 */
@Composable
fun CounterView(model: CounterModel, onIncrement: () -> Unit, onDecrement: () -> Unit, onReset: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Count: ${model.count}", style = MaterialTheme.typography.h4)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDecrement) { Text("-") }
            Button(onClick = onReset) { Text("Reset") }
            Button(onClick = onIncrement) { Text("+") }
        }
    }
}

/** Convenience overload wiring a controller straight into the stateless view. */
@Composable
fun CounterScreen(controller: CounterController) {
    CounterView(
        model = controller.model,
        onIncrement = controller::increment,
        onDecrement = controller::decrement,
        onReset = controller::reset,
    )
}

/** Renders in the IDE's Compose preview panel. */
@Preview
@Composable
fun CounterViewPreview() {
    MaterialTheme {
        CounterScreen(controller = remember { CounterController() })
    }
}

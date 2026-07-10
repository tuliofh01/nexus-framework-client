package nexus.opensource.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import nexus.opensource.controller.FlowsEditorController
import nexus.opensource.core.model.FlowDefinition

@Composable
fun FlowsEditorScreen(
    controller: FlowsEditorController,
    onBack: () -> Unit,
) {
    var showJson by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Flows Editor", style = MaterialTheme.typography.h5)
                Text(
                    "Optional runtime services — background loops & event triggers (distinct from blueprint.json)",
                    style = MaterialTheme.typography.caption,
                )
            }
            TextButton(onClick = onBack) { Text("Back") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable flows in generated project")
            Switch(
                checked = controller.flowsEnabled,
                onCheckedChange = { controller.setAllFlowsEnabled(it) },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { controller.reloadFromTemplate() }) { Text("Reload template") }
            Button(onClick = { showJson = !showJson }) {
                Text(if (showJson) "Hide JSON" else "Preview JSON")
            }
        }

        if (controller.validationErrors.isNotEmpty()) {
            Text(
                controller.validationErrors.first(),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
            )
        }

        Text("Flows (${controller.flows.flows.size})", style = MaterialTheme.typography.subtitle1)

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (flow in controller.flows.flows) {
                FlowListCard(
                    flow = flow,
                    onToggle = { enabled -> controller.toggleFlowEnabled(flow.id, enabled) },
                )
            }
        }

        if (showJson) {
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Text(
                    controller.toJsonPreview(),
                    modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.caption,
                )
            }
        }

        if (controller.statusMessage.isNotBlank()) {
            Text(controller.statusMessage, style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
private fun FlowListCard(
    flow: FlowDefinition,
    onToggle: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(flow.name.ifBlank { flow.id }, style = MaterialTheme.typography.subtitle2)
                Text(
                    "${flow.mode} · ${flow.trigger.type}" +
                        (flow.trigger.name?.let { " · $it" } ?: "") +
                        (flow.trigger.ms?.let { " · ${it}ms" } ?: ""),
                    style = MaterialTheme.typography.caption,
                )
                Text("${flow.steps.size} step(s)", style = MaterialTheme.typography.caption)
            }
            Switch(checked = flow.enabled, onCheckedChange = onToggle)
        }
    }
}

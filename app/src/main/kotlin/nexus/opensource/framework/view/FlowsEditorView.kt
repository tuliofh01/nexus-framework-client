package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.controller.FlowsEditorController
import nexus.opensource.framework.core.model.FlowDefinition
import nexus.opensource.framework.core.model.FlowStep
import nexus.opensource.framework.core.model.FlowStepType
import nexus.opensource.framework.core.model.FlowsJson
import nexus.opensource.framework.util.NativeFileDialogs

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
                    "Runtime automations (distinct from blueprint.json) — import Langflow exports as disabled stubs",
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
            OutlinedButton(
                onClick = {
                    val path = NativeFileDialogs.pickOpenFile("Import Langflow export JSON")
                    if (path != null) {
                        runCatching { controller.importLangflow(path, replace = false) }
                            .onFailure { controller.statusMessage = "Import failed: ${it.message}" }
                    }
                },
            ) {
                Text("Import Langflow…")
            }
            OutlinedButton(
                onClick = {
                    val path = NativeFileDialogs.pickOpenFile("Open flows.json")
                    if (path != null) {
                        runCatching { controller.loadFromFile(path) }
                            .onFailure { controller.statusMessage = "Load failed: ${it.message}" }
                    }
                },
            ) {
                Text("Load…")
            }
            OutlinedButton(
                onClick = {
                    if (!controller.save()) {
                        val path = NativeFileDialogs.pickSaveFile(
                            "Save flows.json",
                            FlowsJson.DEFAULT_PATH.substringAfterLast('/'),
                        )
                        if (path != null) controller.save(path)
                    }
                },
            ) {
                Text("Save…")
            }
            Button(onClick = { showJson = !showJson }) {
                Text(if (showJson) "Hide JSON" else "Preview JSON")
            }
        }

        Text(
            "Langflow-compatible import only maps to flows.json stubs (enabled=false). " +
                "Nexus does not bundle Langflow — credit: Langflow export format.",
            style = MaterialTheme.typography.caption.copy(color = Color.Gray, fontSize = 11.sp),
        )

        if (controller.validationErrors.isNotEmpty()) {
            Text(
                controller.validationErrors.first(),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.caption,
            )
        }
        if (controller.importWarnings.isNotEmpty()) {
            Text(
                controller.importWarnings.take(3).joinToString(" · "),
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.caption,
                maxLines = 2,
            )
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Flow list
            Column(
                modifier = Modifier.weight(0.4f).fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Flows (${controller.flows.flows.size})", style = MaterialTheme.typography.subtitle1)
                for (flow in controller.flows.flows) {
                    FlowListCard(
                        flow = flow,
                        selected = controller.selectedFlowId == flow.id,
                        onSelect = { controller.selectFlow(flow.id) },
                        onToggle = { enabled -> controller.toggleFlowEnabled(flow.id, enabled) },
                    )
                }
            }

            // Step editor + visual canvas skeleton
            Column(
                modifier = Modifier.weight(0.6f).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowStepEditorPanel(controller = controller)
                FlowVisualCanvasPlaceholder(
                    flow = controller.selectedFlow,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
        }

        if (showJson) {
            Card(modifier = Modifier.fillMaxWidth().height(160.dp)) {
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
    selected: Boolean,
    onSelect: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colors.primary else Color.Transparent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onSelect),
    ) {
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

/** Skeleton step list — add/remove/reorder; CUSTOMIZE: full step property editor. */
@Composable
private fun FlowStepEditorPanel(controller: FlowsEditorController) {
    val flow = controller.selectedFlow
    var addMenuExpanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = flow?.let { "Steps: ${it.name.ifBlank { it.id }}" } ?: "Select a flow",
                    style = MaterialTheme.typography.subtitle2,
                )
                if (flow != null) {
                    Box {
                        OutlinedButton(onClick = { addMenuExpanded = true }) {
                            Text("+ Add step")
                        }
                        DropdownMenu(expanded = addMenuExpanded, onDismissRequest = { addMenuExpanded = false }) {
                            FlowStepType.ALL.forEach { type ->
                                DropdownMenuItem(onClick = {
                                    controller.addStep(flow.id, type)
                                    addMenuExpanded = false
                                }) {
                                    Text(type.label)
                                }
                            }
                        }
                    }
                }
            }

            if (flow == null) {
                Text("Click a flow to edit its steps", style = MaterialTheme.typography.caption)
            } else if (flow.steps.isEmpty()) {
                Text("No steps — use Add step", style = MaterialTheme.typography.caption)
            } else {
                flow.steps.forEachIndexed { index, step ->
                    FlowStepRow(
                        index = index,
                        step = step,
                        flowId = flow.id,
                        canMoveUp = index > 0,
                        canMoveDown = index < flow.steps.lastIndex,
                        controller = controller,
                    )
                }
            }
        }
    }
}

@Composable
private fun FlowStepRow(
    index: Int,
    step: FlowStep,
    flowId: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    controller: FlowsEditorController,
) {
    var targetField by remember(step, index) { mutableStateOf(step.target ?: "") }
    var whenField by remember(step, index) { mutableStateOf(step.whenExpr ?: "") }
    var delayField by remember(step, index) { mutableStateOf(step.ms?.toString() ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E2E), RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${index + 1}.", fontSize = 11.sp, modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(step.type, style = MaterialTheme.typography.caption, fontSize = 10.sp)
            when (step.type) {
                FlowStepType.INVOKE.id -> OutlinedTextField(
                    value = targetField,
                    onValueChange = {
                        targetField = it
                        controller.updateStepTarget(flowId, index, it)
                    },
                    label = { Text("target", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.caption,
                )
                FlowStepType.CONDITION.id -> OutlinedTextField(
                    value = whenField,
                    onValueChange = {
                        whenField = it
                        controller.updateStepWhenExpr(flowId, index, it)
                    },
                    label = { Text("whenExpr", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.caption,
                )
                FlowStepType.DELAY.id -> OutlinedTextField(
                    value = delayField,
                    onValueChange = {
                        delayField = it
                        controller.updateStepDelayMs(flowId, index, it.toLongOrNull())
                    },
                    label = { Text("ms", fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.caption,
                )
                else -> Text("—", fontSize = 10.sp, color = Color.Gray)
            }
            // CUSTOMIZE: args JSON editor, step labels, branch targets
        }
        Column {
            TextButton(onClick = { controller.moveStep(flowId, index, index - 1) }, enabled = canMoveUp) {
                Text("↑", fontSize = 12.sp)
            }
            TextButton(onClick = { controller.moveStep(flowId, index, index + 1) }, enabled = canMoveDown) {
                Text("↓", fontSize = 12.sp)
            }
        }
        TextButton(onClick = { controller.removeStep(flowId, index) }) {
            Text("✕", color = MaterialTheme.colors.error, fontSize = 12.sp)
        }
    }
}

/**
 * Placeholder for visual flow authoring (v1.1+).
 * CUSTOMIZE: replace body with node graph, imnodes, or custom canvas.
 */
@Composable
fun FlowVisualCanvasPlaceholder(
    flow: FlowDefinition?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF3A3A5C), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Visual flow canvas (skeleton)",
                style = MaterialTheme.typography.subtitle2,
                color = Color(0xFF9090A0),
            )
            Spacer(Modifier.height(8.dp))
            if (flow == null) {
                Text("Select a flow to preview step sequence", fontSize = 11.sp, color = Color.Gray)
            } else {
                // CUSTOMIZE: render nodes/edges from flow.steps
                flow.steps.forEachIndexed { i, step ->
                    Text(
                        "${i + 1}. [${step.type}] ${step.target ?: step.ms?.let { "${it}ms" } ?: step.whenExpr ?: ""}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF6BCBFF),
                    )
                    if (i < flow.steps.lastIndex) {
                        Text("  ↓", fontSize = 10.sp, color = Color.Gray)
                    }
                }
                if (flow.steps.isEmpty()) {
                    Text("(empty — add steps above)", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "// CUSTOMIZE: swap FlowVisualCanvasPlaceholder for drag-drop graph editor",
                fontSize = 9.sp,
                color = Color(0xFF606060),
            )
        }
    }
}

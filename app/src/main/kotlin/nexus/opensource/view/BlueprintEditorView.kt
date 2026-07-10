package nexus.opensource.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.controller.BlueprintEditorController
import nexus.opensource.core.model.BlueprintNode
import nexus.opensource.core.model.BlueprintNodeType
import kotlin.math.roundToInt

private val nodeWidth = 160f
private val nodeHeight = 56f

private fun nodeColor(type: String): Color = when (type) {
    BlueprintNodeType.PYTHON_MODULE.id -> Color(0xFF2E7D32)
    BlueprintNodeType.CPP_MODEL.id -> Color(0xFF1565C0)
    BlueprintNodeType.CPP_CONTROLLER.id -> Color(0xFF6A1B9A)
    BlueprintNodeType.UI_PAGE.id -> Color(0xFFEF6C00)
    BlueprintNodeType.LUA_SCRIPT.id -> Color(0xFF00838F)
    else -> Color(0xFF546E7A)
}

@Composable
fun BlueprintEditorScreen(
    controller: BlueprintEditorController,
    onBack: () -> Unit,
) {
    var addMenuExpanded by remember { mutableStateOf(false) }
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
                Text("Blueprint Editor", style = MaterialTheme.typography.h5)
                Text(
                    "Langflow-style typed DAG (design-time) — not n8n runtime automation; same schema in future imnodes panel",
                    style = MaterialTheme.typography.caption,
                )
            }
            TextButton(onClick = onBack) { Text("Back") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { addMenuExpanded = true }) { Text("Add node") }
            DropdownMenu(expanded = addMenuExpanded, onDismissRequest = { addMenuExpanded = false }) {
                for (type in BlueprintNodeType.ALL) {
                    DropdownMenuItem(onClick = {
                        controller.addNode(type)
                        addMenuExpanded = false
                    }) {
                        Text("${type.label} (${type.paradigm.id})")
                    }
                }
            }
            Button(onClick = { controller.removeSelectedNode() }, enabled = controller.selectedNodeId != null) {
                Text("Delete node")
            }
            Button(
                onClick = {
                    val id = controller.selectedNodeId
                    if (id != null) controller.beginEdgeFrom(id)
                },
                enabled = controller.selectedNodeId != null,
            ) {
                Text("Connect from")
            }
            Button(onClick = { controller.reloadFromTemplate() }) { Text("Reload template") }
            Button(onClick = { showJson = !showJson }) {
                Text(if (showJson) "Hide JSON" else "Show JSON")
            }
        }

        if (controller.validationErrors.isNotEmpty()) {
            Text(
                controller.validationErrors.joinToString("\n"),
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.body2,
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BlueprintGraphCanvas(
                controller = controller,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            BlueprintInspectorPanel(
                controller = controller,
                modifier = Modifier.width(280.dp).fillMaxHeight(),
            )
        }

        if (showJson) {
            Card(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                Text(
                    controller.toJsonPreview(),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
            }
        }

        if (controller.statusMessage.isNotBlank()) {
            Text(controller.statusMessage, style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
private fun BlueprintGraphCanvas(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    val blueprint = controller.blueprint
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(8.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val positions = blueprint.nodes.associate { it.id to it.position }
                for (edge in blueprint.edges) {
                    val src = positions[edge.source] ?: continue
                    val tgt = positions[edge.target] ?: continue
                    val start = Offset(src.x + nodeWidth / 2f, src.y + nodeHeight / 2f)
                    val end = Offset(tgt.x + nodeWidth / 2f, tgt.y + nodeHeight / 2f)
                    drawLine(Color(0xFF90CAF9), start, end, strokeWidth = 2f)
                }
                for (node in blueprint.nodes) {
                    val selected = node.id == controller.selectedNodeId
                    val pending = node.id == controller.pendingEdgeSourceId
                    drawRect(
                        color = nodeColor(node.type),
                        topLeft = Offset(node.position.x, node.position.y),
                        size = Size(nodeWidth, nodeHeight),
                        style = Stroke(width = if (selected || pending) 4f else 1f),
                    )
                    drawRect(
                        color = nodeColor(node.type).copy(alpha = 0.35f),
                        topLeft = Offset(node.position.x, node.position.y),
                        size = Size(nodeWidth, nodeHeight),
                    )
                }
            }

            for (node in blueprint.nodes) {
                DraggableNodeChip(
                    node = node,
                    selected = node.id == controller.selectedNodeId,
                    pendingSource = node.id == controller.pendingEdgeSourceId,
                    onSelect = {
                        if (controller.pendingEdgeSourceId != null && controller.pendingEdgeSourceId != node.id) {
                            controller.completeEdgeTo(node.id)
                        } else {
                            controller.selectNode(node.id)
                        }
                    },
                    onDrag = { dx, dy ->
                        controller.moveNode(
                            node.id,
                            node.position.x + dx,
                            node.position.y + dy,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DraggableNodeChip(
    node: BlueprintNode,
    selected: Boolean,
    pendingSource: Boolean,
    onSelect: () -> Unit,
    onDrag: (Float, Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(node.position.x.roundToInt(), node.position.y.roundToInt())
            }
            .size(nodeWidth.dp, nodeHeight.dp)
            .pointerInput(node.id) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                node.id,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
            )
            Text(
                node.type.substringAfter('.'),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
            )
            if (selected || pendingSource) {
                Text("●", color = Color.Yellow, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun BlueprintInspectorPanel(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    val selected = controller.blueprint.nodes.find { it.id == controller.selectedNodeId }

    Card(modifier = modifier.verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Inspector", style = MaterialTheme.typography.subtitle1)
            Text("Nodes: ${controller.blueprint.nodes.size}", style = MaterialTheme.typography.caption)
            Text("Edges: ${controller.blueprint.edges.size}", style = MaterialTheme.typography.caption)

            if (selected != null) {
                var nodeIdField by remember(selected.id) { mutableStateOf(selected.id) }
                OutlinedTextField(
                    value = nodeIdField,
                    onValueChange = {
                        nodeIdField = it
                        controller.updateNodeId(selected.id, it)
                    },
                    label = { Text("Node id") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Type: ${selected.type}", style = MaterialTheme.typography.body2)
                Text(
                    selected.data.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                )
            } else {
                Text("Select a node on the canvas", style = MaterialTheme.typography.body2)
            }

            Text("Edges", style = MaterialTheme.typography.subtitle2)
            for (edge in controller.blueprint.edges) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${edge.source} → ${edge.target} (${edge.port})",
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { controller.removeSelectedEdge(edge.id) }) {
                        Text("×")
                    }
                }
            }
        }
    }
}

package nexus.opensource.framework.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.controller.BlueprintEditorController
import nexus.opensource.framework.core.model.BlueprintNode
import nexus.opensource.framework.core.model.BlueprintNodeType
import kotlin.math.roundToInt

private val nodeWidth = 160f
private val nodeHeight = 56f
private val DarkBg = Color(0xFF1A1A2E)
private val CardBg = Color(0xFF1F2B47)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9090A0)

private fun nodeBg(type: String): Color = when (type) {
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Blueprint Editor",
                        style = MaterialTheme.typography.h5.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                    )
                    Text(
                        text = "Design-time typed DAG — drag nodes, connect edges",
                        style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                    )
                }
                TextButton(onClick = onBack) {
                    Text("\u2190 Back", color = TextSecondary)
                }
            }

            // Toolbar
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { addMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                ) {
                    Text("+ Add node", color = Color.White)
                }
                DropdownMenu(expanded = addMenuExpanded, onDismissRequest = { addMenuExpanded = false }) {
                    for (type in BlueprintNodeType.ALL) {
                        DropdownMenuItem(onClick = {
                            controller.addNode(type)
                            addMenuExpanded = false
                        }) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(nodeBg(type.id)),
                                )
                                Text("${type.label} (${type.paradigm.id})")
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { controller.removeSelectedNode() },
                    enabled = controller.selectedNodeId != null,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                ) {
                    Text("Delete")
                }
                OutlinedButton(
                    onClick = {
                        val id = controller.selectedNodeId
                        if (id != null) controller.beginEdgeFrom(id)
                    },
                    enabled = controller.selectedNodeId != null,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00D4FF)),
                ) {
                    Text("Connect")
                }
                OutlinedButton(
                    onClick = { controller.reloadFromTemplate() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                ) {
                    Text("Reload")
                }
            }

            // Validation
            if (controller.validationErrors.isNotEmpty()) {
                Text(
                    controller.validationErrors.joinToString("\n"),
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2,
                )
            }

            // Canvas + Inspector
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

            // Status
            if (controller.statusMessage.isNotBlank()) {
                Text(
                    controller.statusMessage,
                    style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                )
            }
        }
    }
}

@Composable
private fun BlueprintGraphCanvas(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    val blueprint = controller.blueprint
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = CardBg,
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                        color = nodeBg(node.type),
                        topLeft = Offset(node.position.x, node.position.y),
                        size = Size(nodeWidth, nodeHeight),
                        style = Stroke(width = if (selected || pending) 4f else 1f),
                    )
                    drawRect(
                        color = nodeBg(node.type).copy(alpha = 0.35f),
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
    var hovered by remember { mutableStateOf(false) }
    val bgColor = nodeBg(node.type)
    val borderWidth = if (selected || pendingSource) 3.dp else if (hovered) 2.dp else 0.dp

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor.copy(alpha = 0.85f))
                .then(
                    if (borderWidth > 0.dp)
                        Modifier.border(borderWidth, bgColor.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    node.id,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    node.type.substringAfter('.'),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                )
            }
        }

        // Selection indicator
        if (selected || pendingSource) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (pendingSource) Color(0xFFFFD93D) else Color(0xFF00D4FF)),
            )
        }
    }
}

@Composable
private fun BlueprintInspectorPanel(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    val selected = controller.blueprint.nodes.find { it.id == controller.selectedNodeId }

    Card(
        modifier = modifier.verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = CardBg,
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Inspector",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )

            // Node count badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1565C0).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("${controller.blueprint.nodes.size} nodes", color = Color(0xFF64B5F6), fontSize = 10.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEF6C00).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text("${controller.blueprint.edges.size} edges", color = Color(0xFFFFB74D), fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(4.dp))

            // Selected node detail
            if (selected != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(nodeBg(selected.type).copy(alpha = 0.15f))
                        .padding(8.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = selected.id,
                            style = MaterialTheme.typography.subtitle2.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(nodeBg(selected.type)),
                            )
                            Text(
                                text = selected.type,
                                color = TextSecondary,
                                fontSize = 11.sp,
                            )
                        }
                    }
                }

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
                    textStyle = MaterialTheme.typography.body2.copy(color = TextPrimary),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF00D4FF),
                        unfocusedBorderColor = Color(0xFF3A3A5C),
                        cursorColor = Color(0xFF00D4FF),
                        focusedLabelColor = Color(0xFF00D4FF),
                        unfocusedLabelColor = TextSecondary,
                    ),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2D2D4A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Tap a node on the canvas to inspect",
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Edge list
            Text(
                text = "Edges",
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                ),
            )
            if (controller.blueprint.edges.isEmpty()) {
                Text(
                    text = "No edges — connect nodes from the toolbar",
                    color = TextSecondary,
                    fontSize = 10.sp,
                )
            } else {
                controller.blueprint.edges.forEach { edge ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        backgroundColor = Color(0xFF2D2D4A),
                        elevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${edge.source} \u2192 ${edge.target}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = { controller.removeSelectedEdge(edge.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text("\u2717", color = Color(0xFFFF5252), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

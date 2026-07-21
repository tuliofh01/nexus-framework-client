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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.controller.BlueprintEditorController
import nexus.opensource.framework.core.model.BlueprintJson
import nexus.opensource.framework.core.model.BlueprintNode
import nexus.opensource.framework.core.model.BlueprintNodeType
import nexus.opensource.framework.util.NativeFileDialogs
import kotlin.math.roundToInt

private val nodeWidth = 168f
private val nodeHeight = 64f

private fun nodeAccent(type: String): Color = when (type) {
    BlueprintNodeType.PYTHON_MODULE.id -> NexusTheme.AccentGreen
    BlueprintNodeType.CPP_MODEL.id -> NexusTheme.AccentCyan
    BlueprintNodeType.CPP_CONTROLLER.id -> NexusTheme.BrandPurple
    BlueprintNodeType.UI_PAGE.id -> NexusTheme.AccentOrange
    BlueprintNodeType.LUA_SCRIPT.id -> NexusTheme.FlamingoPink
    BlueprintNodeType.FLOW_AUTOMATION.id -> NexusTheme.BeakOrange
    else -> NexusTheme.TextMuted
}

/**
 * Visual mockup of the blueprint node-graph editor.
 *
 * Layout mockup: `docs/assets/examples/mockup-blueprint-editor.svg`
 * Regenerate: `python3 misc/scripts/generate-diagrams.py --mockups`
 *
 * Compose canvas draws sample nodes/edges; native imnodes plugs in later via
 * [BlueprintEditorController.canvasExtension].
 */
@Composable
fun BlueprintEditorScreen(
    controller: BlueprintEditorController,
    onBack: () -> Unit,
) {
    var addMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = NexusTheme.DarkBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BlueprintEditorHeader(
                blueprintName = controller.blueprint.name,
                projectName = controller.projectName,
                onBack = onBack,
            )

            BlueprintToolbar(
                controller = controller,
                addMenuExpanded = addMenuExpanded,
                onAddMenuExpand = { addMenuExpanded = true },
                onAddMenuDismiss = { addMenuExpanded = false },
            )

            if (controller.validationErrors.isNotEmpty()) {
                ValidationBanner(errors = controller.validationErrors)
            }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BlueprintNodePaletteSidebar(
                    controller = controller,
                    modifier = Modifier.width(168.dp).fillMaxHeight(),
                )

                if (controller.canvasExtension.useNativeCanvas) {
                    // CUSTOMIZE: swap in imnodes native surface when canvasExtension.useNativeCanvas = true
                    BlueprintImNodesCanvasStub(
                        controller = controller,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                } else {
                    BlueprintGraphCanvas(
                        controller = controller,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }

                Column(
                    modifier = Modifier.width(300.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BlueprintInspectorPanel(
                        controller = controller,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                    if (controller.showJsonPreview) {
                        JsonPreviewPane(
                            json = controller.toJsonPreview(),
                            modifier = Modifier.height(160.dp).fillMaxWidth(),
                        )
                    }
                }
            }

            StatusStrip(controller = controller)
        }
    }
}

@Composable
private fun BlueprintEditorHeader(
    blueprintName: String,
    projectName: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlamingoGlyph(modifier = Modifier.size(36.dp), tint = NexusTheme.AccentOrange)
            Column {
                Text(
                    text = "Blueprint Editor",
                    style = MaterialTheme.typography.h5.copy(
                        fontWeight = FontWeight.Bold,
                        color = NexusTheme.TextPrimary,
                    ),
                )
                Text(
                    text = "$blueprintName · $projectName — drag nodes, connect edges",
                    style = MaterialTheme.typography.caption.copy(color = NexusTheme.TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        TextButton(onClick = onBack) {
            Text("← Back", color = NexusTheme.TextSecondary)
        }
    }
}

@Composable
private fun BlueprintToolbar(
    controller: BlueprintEditorController,
    addMenuExpanded: Boolean,
    onAddMenuExpand: () -> Unit,
    onAddMenuDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = NexusTheme.CardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Button(
                    onClick = onAddMenuExpand,
                    colors = ButtonDefaults.buttonColors(backgroundColor = NexusTheme.AccentGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text("+ Add node", color = Color.White, fontSize = 12.sp)
                }
                DropdownMenu(expanded = addMenuExpanded, onDismissRequest = onAddMenuDismiss) {
                    for (type in BlueprintNodeType.ALL) {
                        DropdownMenuItem(onClick = {
                            controller.addNode(type)
                            onAddMenuDismiss()
                        }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(nodeAccent(type.id)),
                                )
                                Text("${type.label} (${type.paradigm.id})")
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { controller.removeSelectedNode() },
                enabled = controller.selectedNodeId != null,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTheme.AccentRed),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Delete", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = {
                    val id = controller.selectedNodeId
                    if (id != null) controller.beginEdgeFrom(id)
                },
                enabled = controller.selectedNodeId != null,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTheme.AccentCyan),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Connect", fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(NexusTheme.Divider),
            )

            OutlinedButton(
                onClick = {
                    if (!controller.save()) {
                        val path = NativeFileDialogs.pickSaveFile(
                            "Save blueprint.json",
                            BlueprintJson.FILE_NAME,
                        )
                        if (path != null) controller.save(path)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTheme.TextPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Save", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = {
                    val path = NativeFileDialogs.pickOpenFile("Open blueprint.json")
                    if (path != null) controller.load(path)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTheme.TextPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Load", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { controller.runValidate() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NexusTheme.AccentOrange),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Validate", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { controller.toggleJsonPreview() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (controller.showJsonPreview) NexusTheme.FlamingoPink else NexusTheme.TextSecondary,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(if (controller.showJsonPreview) "Hide JSON" else "JSON", fontSize = 12.sp)
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "${controller.blueprint.nodes.size} nodes · ${controller.blueprint.edges.size} edges",
                style = MaterialTheme.typography.caption.copy(
                    color = NexusTheme.TextMuted,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun ValidationBanner(errors: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = NexusTheme.AccentRed.copy(alpha = 0.12f),
    ) {
        Text(
            text = errors.joinToString(" · "),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = NexusTheme.AccentRed,
            style = MaterialTheme.typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusStrip(controller: BlueprintEditorController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NexusTheme.SurfaceBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = controller.statusMessage.ifBlank { "Ready — select a node or add from the palette" },
            style = MaterialTheme.typography.caption.copy(color = NexusTheme.TextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (controller.pendingEdgeSourceId != null) {
            Text(
                text = "Connecting from ${controller.pendingEdgeSourceId}…",
                style = MaterialTheme.typography.caption.copy(
                    color = NexusTheme.AccentCyan,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/**
 * Skeleton node palette — drag/add shortcuts for blueprint node types.
 * CUSTOMIZE: Wire drag-and-drop onto canvas or imnodes palette in v1.1.
 */
@Composable
private fun BlueprintNodePaletteSidebar(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.CardBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Palette",
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.TextPrimary,
                ),
            )
            Text(
                text = "Click to add to canvas",
                color = NexusTheme.TextMuted,
                fontSize = 10.sp,
            )
            // CUSTOMIZE: paletteNodeTypes() — favorites, search filter, custom templates
            for (type in controller.paletteNodeTypes()) {
                PaletteNodeRow(
                    type = type,
                    onClick = { controller.addNode(type) },
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "imnodes · v1.1",
                color = NexusTheme.TextMuted.copy(alpha = 0.7f),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun PaletteNodeRow(
    type: BlueprintNodeType,
    onClick: () -> Unit,
) {
    val accent = nodeAccent(type.id)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = NexusTheme.ProgressTrack,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(type.label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(type.id, fontSize = 9.sp, color = NexusTheme.TextMuted, maxLines = 1)
            }
        }
    }
}

/**
 * Placeholder when [BlueprintEditorController.canvasExtension] enables imnodes.
 * CUSTOMIZE: Replace with native imnodes embed (v1.1).
 */
@Composable
fun BlueprintImNodesCanvasStub(
    controller: BlueprintEditorController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.CardBg,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "imnodes canvas (v1.1)",
                    style = MaterialTheme.typography.subtitle1.copy(color = NexusTheme.TextPrimary),
                )
                Text(
                    "${controller.blueprint.nodes.size} nodes · ${controller.blueprint.edges.size} edges",
                    color = NexusTheme.TextSecondary,
                    fontSize = 11.sp,
                )
                Text(
                    "CUSTOMIZE: mount native imnodes editor here",
                    color = NexusTheme.TextMuted,
                    fontSize = 10.sp,
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.SurfaceBg,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, NexusTheme.Divider.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Dot grid backdrop
                val step = 28f
                var gx = step
                while (gx < size.width) {
                    var gy = step
                    while (gy < size.height) {
                        drawCircle(
                            color = NexusTheme.Divider.copy(alpha = 0.35f),
                            radius = 1.2f,
                            center = Offset(gx, gy),
                        )
                        gy += step
                    }
                    gx += step
                }

                val positions = blueprint.nodes.associate { it.id to it.position }
                for (edge in blueprint.edges) {
                    val src = positions[edge.source] ?: continue
                    val tgt = positions[edge.target] ?: continue
                    val start = Offset(src.x + nodeWidth, src.y + nodeHeight / 2f)
                    val end = Offset(tgt.x, tgt.y + nodeHeight / 2f)
                    val midX = (start.x + end.x) / 2f
                    val path = Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(midX, start.y, midX, end.y, end.x, end.y)
                    }
                    drawPath(
                        path = path,
                        color = NexusTheme.AccentCyan.copy(alpha = 0.65f),
                        style = Stroke(width = 2.2f, cap = StrokeCap.Round),
                    )
                    // Port dots
                    drawCircle(NexusTheme.AccentCyan, 4f, start)
                    drawCircle(NexusTheme.FlamingoPink, 4f, end)
                }

                for (node in blueprint.nodes) {
                    val selected = node.id == controller.selectedNodeId
                    val pending = node.id == controller.pendingEdgeSourceId
                    val accent = nodeAccent(node.type)
                    drawRoundRect(
                        color = accent.copy(alpha = 0.18f),
                        topLeft = Offset(node.position.x, node.position.y),
                        size = Size(nodeWidth, nodeHeight),
                        cornerRadius = CornerRadius(10f, 10f),
                    )
                    drawRoundRect(
                        color = if (selected || pending) accent else accent.copy(alpha = 0.55f),
                        topLeft = Offset(node.position.x, node.position.y),
                        size = Size(nodeWidth, nodeHeight),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = if (selected || pending) 2.5f else 1.2f),
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

            Text(
                text = "Compose canvas · drag nodes · Connect then click target",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                color = NexusTheme.TextMuted.copy(alpha = 0.6f),
                fontSize = 9.sp,
            )
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
    // CUSTOMIZE: richer hover affordances (port highlights, tooltips)
    var hovered by remember { mutableStateOf(false) }
    val accent = nodeAccent(node.type)
    val borderWidth = when {
        selected || pendingSource -> 2.5.dp
        hovered -> 1.5.dp
        else -> 0.dp
    }

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
                    onDragStart = {
                        hovered = true
                        onSelect()
                    },
                    onDragEnd = { hovered = false },
                    onDragCancel = { hovered = false },
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
                .clip(RoundedCornerShape(10.dp))
                .background(NexusTheme.CardBg)
                .then(
                    if (borderWidth > 0.dp) {
                        Modifier.border(borderWidth, accent, RoundedCornerShape(10.dp))
                    } else {
                        Modifier.border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    },
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 10.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    node.id,
                    color = NexusTheme.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    node.type,
                    color = NexusTheme.TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (selected || pendingSource) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(11.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (pendingSource) NexusTheme.BeakOrange else NexusTheme.AccentCyan),
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

    Surface(
        modifier = modifier.verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(12.dp),
        color = NexusTheme.CardBg,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Inspector",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.TextPrimary,
                ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip("${controller.blueprint.nodes.size} nodes", NexusTheme.AccentCyan)
                StatChip("${controller.blueprint.edges.size} edges", NexusTheme.AccentOrange)
            }

            if (selected != null) {
                val accent = nodeAccent(selected.type)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = selected.id,
                            style = MaterialTheme.typography.subtitle2.copy(
                                color = NexusTheme.TextPrimary,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(accent),
                            )
                            Text(
                                text = selected.type,
                                color = NexusTheme.TextSecondary,
                                fontSize = 11.sp,
                            )
                        }
                        // CUSTOMIZE: typed property editors from node.data JSON
                        Text(
                            text = "data keys: ${selected.data.keys.joinToString()}",
                            color = NexusTheme.TextMuted,
                            fontSize = 10.sp,
                        )
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
                    textStyle = MaterialTheme.typography.body2.copy(color = NexusTheme.TextPrimary),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = NexusTheme.AccentCyan,
                        unfocusedBorderColor = NexusTheme.Divider,
                        cursorColor = NexusTheme.AccentCyan,
                        focusedLabelColor = NexusTheme.AccentCyan,
                        unfocusedLabelColor = NexusTheme.TextSecondary,
                    ),
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = NexusTheme.ProgressTrack,
                ) {
                    Text(
                        text = "Tap a node on the canvas to inspect",
                        modifier = Modifier.padding(14.dp),
                        color = NexusTheme.TextSecondary,
                        fontSize = 11.sp,
                    )
                }
            }

            Text(
                text = "Edges",
                style = MaterialTheme.typography.subtitle2.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.TextPrimary,
                ),
            )
            if (controller.blueprint.edges.isEmpty()) {
                Text(
                    text = "No edges — use Connect or click a target after selecting a source",
                    color = NexusTheme.TextMuted,
                    fontSize = 10.sp,
                )
            } else {
                controller.blueprint.edges.forEach { edge ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = NexusTheme.ProgressTrack,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${edge.source} → ${edge.target}",
                                color = NexusTheme.TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            TextButton(
                                onClick = { controller.removeSelectedEdge(edge.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) {
                                Text("✕", color = NexusTheme.AccentRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(accent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, color = accent, fontSize = 10.sp)
    }
}

@Composable
private fun JsonPreviewPane(
    json: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = NexusTheme.CardBg,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "JSON preview",
                style = MaterialTheme.typography.caption.copy(
                    fontWeight = FontWeight.Bold,
                    color = NexusTheme.FlamingoPink,
                ),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = json,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.caption.copy(
                    color = NexusTheme.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                ),
            )
        }
    }
}

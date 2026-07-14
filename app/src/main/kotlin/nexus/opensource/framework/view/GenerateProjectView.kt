package nexus.opensource.framework.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.core.model.AppType

private val DarkBg = Color(0xFF1A1A2E)
private val CardBg = Color(0xFF1F2B47)
private val TextPrimary = Color(0xFFE8E8E8)
private val TextSecondary = Color(0xFF9090A0)
private val AccentCyan = Color(0xFF00D4FF)
private val AccentGreen = Color(0xFF00E676)
private val AccentOrange = Color(0xFFFF9100)

@Composable
fun GenerateProjectScreen(
    controller: GenerateController,
    onBack: () -> Unit,
    onEditBlueprint: () -> Unit,
    onEditFlows: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Generate Project",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        ),
                    )
                    Text(
                        text = "Scaffold a new native app from templates",
                        style = MaterialTheme.typography.body2.copy(color = TextSecondary),
                    )
                }
                TextButton(onClick = onBack) {
                    Text("\u2190 Dashboard", color = TextSecondary)
                }
            }

            // Project name — styled as prominent input
            OutlinedTextField(
                value = controller.projectName,
                onValueChange = {
                    controller.projectName = it
                    controller.syncAllContext()
                },
                label = { Text("Project name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.h5.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color(0xFF3A3A5C),
                    cursorColor = AccentCyan,
                    focusedLabelColor = AccentCyan,
                    unfocusedLabelColor = TextSecondary,
                ),
            )

            // Project type — visual cards instead of dropdown
            Text(
                text = "Template type",
                style = MaterialTheme.typography.subtitle2.copy(color = TextSecondary),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AppTypeCard(
                    type = AppType.DESKTOP,
                    selected = controller.appType == AppType.DESKTOP,
                    onClick = {
                        controller.appType = AppType.DESKTOP
                        controller.syncAllContext()
                    },
                )
                AppTypeCard(
                    type = AppType.ANDROID,
                    selected = controller.appType == AppType.ANDROID,
                    onClick = {
                        controller.appType = AppType.ANDROID
                        controller.syncAllContext()
                    },
                )
            }

            // Output preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = CardBg,
                elevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Output path",
                        style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                    )
                    Text(
                        text = "builds/framework/${controller.projectName}/",
                        style = MaterialTheme.typography.body1.copy(
                            color = AccentCyan,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                    )
                    Text(
                        text = "Stack: SDL3 + ImGui + Lua + Python + TypeScript/XHTML",
                        style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                    )
                }
            }

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Generate — primary action, large
                Button(
                    onClick = { controller.generate() },
                    enabled = !controller.isGenerating && controller.projectName.isNotBlank(),
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF2E7D32),
                        disabledBackgroundColor = Color(0xFF2D2D4A),
                    ),
                ) {
                    Text(
                        text = if (controller.isGenerating) "\u23F3 Generating..." else "\uD83D\uDE80 Generate",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }

                // Blueprint editor — secondary
                OutlinedButton(
                    onClick = {
                        controller.syncAllContext()
                        onEditBlueprint()
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                ) {
                    Text("\uD83D\uDD35 Blueprint", fontWeight = FontWeight.Medium)
                }

                // Flows editor — secondary
                OutlinedButton(
                    onClick = {
                        controller.syncAllContext()
                        onEditFlows()
                    },
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                ) {
                    Text("\u26A1 Flows", fontWeight = FontWeight.Medium)
                }
            }

            // Status messages
            if (controller.statusMessage.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = if (controller.statusMessage.startsWith("Generated"))
                        Color(0xFF1B3A2A) else Color(0xFF3D1A1A),
                    elevation = 0.dp,
                ) {
                    Text(
                        text = controller.statusMessage,
                        modifier = Modifier.padding(12.dp),
                        color = if (controller.statusMessage.startsWith("Generated"))
                            AccentGreen else Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                    )
                }
            }

            // Validation errors
            val errors = mutableListOf<String>().apply {
                if (controller.blueprintEditor.validationErrors.isNotEmpty()) {
                    add("Blueprint: ${controller.blueprintEditor.validationErrors.first()}")
                }
                if (controller.flowsEditor.validationErrors.isNotEmpty()) {
                    add("Flows: ${controller.flowsEditor.validationErrors.first()}")
                }
            }
            if (errors.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFF3D1A1A),
                    elevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        errors.forEach { err ->
                            Text(
                                text = err,
                                color = Color(0xFFFF6B6B),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTypeCard(
    type: AppType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val (icon, desc) = when (type) {
        AppType.DESKTOP -> "\uD83D\uDDBB" to "Windows \u00B7 macOS \u00B7 Linux"
        AppType.ANDROID -> "\uD83D\uDCF1" to "Android APK (Chaquopy)"
    }

    val borderColor = if (selected) AccentCyan else Color(0xFF3A3A5C)
    val bgColor = if (selected) Color(0xFF0D2B3D) else CardBg

    Box(
        modifier = Modifier
            .width(180.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = icon, fontSize = 28.sp)
            Text(
                text = type.label,
                style = MaterialTheme.typography.subtitle1.copy(
                    color = if (selected) AccentCyan else TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.caption.copy(color = TextSecondary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

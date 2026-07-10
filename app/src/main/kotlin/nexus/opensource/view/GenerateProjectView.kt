package nexus.opensource.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
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
import androidx.compose.ui.unit.dp
import nexus.opensource.controller.GenerateController
import nexus.opensource.core.model.AppType
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GenerateProjectScreen(
    controller: GenerateController,
    onBack: () -> Unit,
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Generate Project", style = MaterialTheme.typography.h5)
            TextButton(onClick = onBack) { Text("Back") }
        }

        OutlinedTextField(
            value = controller.projectName,
            onValueChange = { controller.projectName = it },
            label = { Text("Project name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Template:")
            Button(onClick = { typeMenuExpanded = true }) {
                Text(controller.appType.label)
            }
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                for (type in AppType.entries) {
                    DropdownMenuItem(onClick = {
                        controller.appType = type
                        typeMenuExpanded = false
                    }) {
                        Text(type.label)
                    }
                }
            }
        }

        Text(
            "Output: builds/framework/${controller.projectName}/",
            style = MaterialTheme.typography.caption,
        )

        Button(
            onClick = { controller.generate() },
            enabled = !controller.isGenerating && controller.projectName.isNotBlank(),
        ) {
            Text(if (controller.isGenerating) "Generating…" else "Generate")
        }

        if (controller.statusMessage.isNotBlank()) {
            Text(controller.statusMessage, style = MaterialTheme.typography.body2)
        }
    }
}

@Preview
@Composable
fun GenerateProjectScreenPreview() {
    MaterialTheme {
        GenerateProjectScreen(controller = remember { GenerateController() }, onBack = {})
    }
}

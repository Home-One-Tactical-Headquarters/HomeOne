package dk.holonet.ui.editor.borderpane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dk.holonet.config.ModuleConfig

@Composable
internal fun ModulePicker(
    imageVector: ImageVector,
    contentDescription: String,
    availableModules: List<ModuleConfig>,
    selectedModules: List<ModuleConfig>,
    onModuleSelected: (ModuleConfig, Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            modifier = Modifier.heightIn(
                min = 0.dp,
                max = if (availableModules.size <= 10) {
                    // Height per item (48.dp padding included) * number of items
                    (80.dp * availableModules.size)
                } else {
                    // Max height for 10 items
                    800.dp
                }
            ),
            expanded = expanded,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onDismissRequest = { expanded = false },
        ) {
            availableModules.forEach { module ->
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(module.name)
                    Checkbox(
                        checked = selectedModules.contains(module),
                        onCheckedChange = { onModuleSelected(module, it) },
                    )
                }
            }
        }
    }
}
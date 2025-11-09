package dk.holonet.ui.editor.modulelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.PlusSolid
import compose.icons.lineawesomeicons.TrashSolid
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ModulesList(
    modifier: Modifier,
    viewModel: ModuleListViewModel = koinViewModel(),
) {
    val state by viewModel.modules.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val overwriteConfirmation by viewModel.overwriteConfirmation.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteModulesDialog(
            modules = state,
            onDismiss = { showDeleteDialog = false },
            onDelete = { pluginIds ->
                viewModel.removeModule(pluginIds)
                showDeleteDialog = false
            }
        )
    }

    overwriteConfirmation?.let { files ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelUpload() },
            title = { Text("Overwrite Modules?") },
            text = {
                val fileNames = files.joinToString(separator = "\n") { "• ${it.name}" }
                Text("The following modules already exist. Do you want to overwrite them?\n\n$fileNames")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmUpload() }) {
                    Text("Overwrite")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelUpload() }) {
                    Text("Cancel")
                }
            }
        )
    }

    val fileLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("jar"))
    ) { files ->
        viewModel.uploadModules(files)
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = "Available Modules",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state) { module ->
                ModuleConfigBox(
                    module = module,
                    modifier = if (currentPosition != null) Modifier.clickable {
                        viewModel.addModule(module)
                    } else Modifier
                )
            }
        }

        HorizontalDivider()

        Row {
            TextButton(
                onClick = { fileLauncher.launch() },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RectangleShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.PlusSolid,
                        contentDescription = "Add Module",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            TextButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RectangleShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.TrashSolid,
                        contentDescription = "Delete modules",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteModulesDialog(
    modules: List<dk.holonet.core.HolonetSchema>,
    onDismiss: () -> Unit,
    onDelete: (List<String>) -> Unit
) {
    var selectedModules by remember { mutableStateOf(emptySet<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Modules") },
        text = {
            LazyColumn {
                items(modules) { module ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedModules = if (module.pluginId in selectedModules) {
                                    selectedModules - module.pluginId
                                } else {
                                    selectedModules + module.pluginId
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = module.pluginId in selectedModules,
                            onCheckedChange = {
                                selectedModules = if (module.pluginId in selectedModules) {
                                    selectedModules - module.pluginId
                                } else {
                                    selectedModules + module.pluginId
                                }
                            }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(module.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDelete(selectedModules.toList())
              },
                enabled = selectedModules.isNotEmpty()
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
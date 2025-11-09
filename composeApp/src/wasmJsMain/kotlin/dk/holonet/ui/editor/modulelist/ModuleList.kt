package dk.holonet.ui.editor.modulelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.PlusSolid
import compose.icons.lineawesomeicons.TrashSolid
import dk.holonet.core.HolonetSchema
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ModulesList(
    modifier: Modifier = Modifier,
    viewModel: ModuleListViewModel = koinViewModel(),
) {
    val modules by viewModel.modules.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val overwriteConfirmation by viewModel.overwriteConfirmation.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialogs
    if (showDeleteDialog) {
        DeleteModulesDialog(
            modules = modules,
            onDismiss = { showDeleteDialog = false },
            onDelete = { pluginIds ->
                viewModel.removeModule(pluginIds)
                showDeleteDialog = false
            }
        )
    }

    overwriteConfirmation?.let { files ->
        OverwriteConfirmationDialog(
            files = files,
            onConfirm = { viewModel.confirmUpload() },
            onDismiss = { viewModel.cancelUpload() }
        )
    }

    val fileLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Multiple(),
        type = FileKitType.File(extensions = listOf("jar")),
        onResult = { files -> viewModel.uploadModules(files) }
    )

    Column(modifier = modifier) {
        ModuleListHeader()
        ModuleListContent(
            modules = modules,
            currentPosition = currentPosition,
            onModuleClick = { viewModel.addModule(it) },
            modifier = Modifier.weight(1f)
        )
        ModuleListActions(
            onAddClick = { fileLauncher.launch() },
            onDeleteClick = { showDeleteDialog = true }
        )
    }
}

@Composable
private fun ModuleListHeader() {
    Column {
        Text(
            text = "Available Modules",
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        HorizontalDivider()
    }
}

@Composable
private fun ModuleListContent(
    modules: List<HolonetSchema>,
    currentPosition: dk.holonet.core.Position?,
    onModuleClick: (HolonetSchema) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(modules) { module ->
            ModuleConfigBox(
                module = module,
                modifier = if (currentPosition != null) {
                    Modifier.clickable { onModuleClick(module) }
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
private fun ModuleListActions(
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column {
        HorizontalDivider()
        Row {
            ActionButton(
                text = "Add",
                icon = LineAwesomeIcons.PlusSolid,
                contentDescription = "Add Module",
                onClick = onAddClick,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Delete",
                icon = LineAwesomeIcons.TrashSolid,
                contentDescription = "Delete modules",
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RectangleShape
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OverwriteConfirmationDialog(
    files: List<PlatformFile>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Overwrite Modules?") },
        text = {
            val fileNames = files.joinToString(separator = "\n") { "• ${it.name}" }
            Text("The following modules already exist. Do you want to overwrite them?\n\n$fileNames")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Overwrite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteModulesDialog(
    modules: List<HolonetSchema>,
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
                    ModuleCheckboxItem(
                        module = module,
                        isSelected = module.pluginId in selectedModules,
                        onToggle = { selectedModules = toggleSelection(selectedModules, module.pluginId) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDelete(selectedModules.toList()) },
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

@Composable
private fun ModuleCheckboxItem(
    module: HolonetSchema,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Spacer(Modifier.width(16.dp))
        Text(module.name)
    }
}

private fun toggleSelection(currentSelection: Set<String>, id: String): Set<String> =
    if (id in currentSelection) currentSelection - id else currentSelection + id
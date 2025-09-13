package dk.holonet.ui.editor.borderpane.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.Save
import compose.icons.lineawesomeicons.TrashSolid
import dk.holonet.config.ModuleConfig
import dk.holonet.ui.editor.EditorViewModel

@Composable
fun FloatingActionButtonWrapper(
    viewModel: EditorViewModel,
    currentModuleConfig: ModuleConfig?,
    setCurrentModuleConfig: (ModuleConfig?) -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        content()

        when (currentModuleConfig) {
            null -> { /* No buttons */ }
            else -> {
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.removeModule(currentModuleConfig)
                            setCurrentModuleConfig(null)
                        }
                    ) {
                        Icon(LineAwesomeIcons.TrashSolid, contentDescription = "Delete Module")
                    }

                    FloatingActionButton(
                        onClick = {
                            viewModel.saveConfiguration()
                        }
                    ) {
                        Icon(LineAwesomeIcons.Save, contentDescription = "Save Configuration")
                    }
                }
            }
        }
    }
}
package dk.holonet.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dk.holonet.ui.editor.borderpane.BorderPane
import dk.holonet.ui.editor.modulelist.ModulesList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditorView(
    viewModel: EditorViewModel = koinViewModel(),
) {
    // Top Row dividing the screen into two sections:
    // Left: List of available modules
    // Right: Position of applied modules
    Row {
        ModulesList(
            modifier = Modifier.fillMaxHeight().weight(1f),
            viewModel = viewModel
        )

        BorderPane(
            modifier = Modifier.fillMaxHeight().weight(4f).background(MaterialTheme.colorScheme.surfaceContainer),
            viewModel = viewModel
        )
    }
}

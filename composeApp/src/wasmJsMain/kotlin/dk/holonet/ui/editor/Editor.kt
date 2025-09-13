package dk.holonet.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import dk.holonet.ui.editor.borderpane.BorderPaneScreen
import dk.holonet.ui.editor.modulelist.ModulesList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditorView(
    viewModel: EditorViewModel = koinViewModel(),
    paddingValues: PaddingValues,
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row dividing the screen into two sections:
        // Left: List of available modules
        // Right: Position of applied modules
        Row {
            ModulesList(
                modifier = Modifier.fillMaxHeight().weight(1f),
                viewModel = viewModel
            )

            BorderPaneScreen(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(4f)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                viewModel = viewModel,
                onNavHostReady = onNavHostReady
            )
        }
    }
}

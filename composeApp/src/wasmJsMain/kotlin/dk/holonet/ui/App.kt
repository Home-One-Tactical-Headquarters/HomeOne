package dk.holonet.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.SyncSolid
import dk.holonet.theme.HoloNetTheme
import dk.holonet.ui.editor.EditorView
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    viewModel: AppViewModel = koinViewModel(),
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    HoloNetTheme(true) {
        val uiState by viewModel.state.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "HomeOne",
                            maxLines = 1
                        )
                    },
                    actions = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            state = rememberTooltipState(),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        text = "Sync configuration",
                                    )
                                }
                            }
                        ) {
                            IconButton(onClick = { viewModel.fetch() }) {
                                Icon(
                                    imageVector = LineAwesomeIcons.SyncSolid,
                                    contentDescription = "Sync configuration",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            EditorView(
                paddingValues = paddingValues,
                onNavHostReady = onNavHostReady
            )

            /*Column(Modifier.padding(paddingValues).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row {
                    Button(onClick = { viewModel.fetch() }) {
                        Text("Fetch configuration!")
                    }

                    Button(onClick = { viewModel.update() }) {
                        Text("Update configuration!")
                    }
                }

                when (uiState) {
                    is AppViewModel.UiState.Loading -> {
                        Text("Loading...")
                    }
                    is AppViewModel.UiState.Error -> {
                        Text("Error: ${(uiState as AppViewModel.UiState.Error).message}")
                    }
                    is AppViewModel.UiState.Success -> {
                        Text("Success: ${(uiState as AppViewModel.UiState.Success).data}")
                    }
                }
            }*/
        }
    }
}
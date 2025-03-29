package dk.holonet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dk.holonet.theme.HoloNetTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    viewModel: AppViewModel = koinViewModel(),
) {
    HoloNetTheme {
        val uiState by viewModel.state.collectAsState()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
        }
    }
}
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.PlusSolid
import compose.icons.lineawesomeicons.Save
import dk.holonet.ui.editor.EditorViewModel

@Composable
internal fun ModulesList(
    modifier: Modifier,
    viewModel: EditorViewModel
) {
    val state by viewModel.modules.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()

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
                onClick = {},
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
                    viewModel.saveConfiguration()
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RectangleShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.Save,
                        contentDescription = "Save configuration",
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }


    }
}
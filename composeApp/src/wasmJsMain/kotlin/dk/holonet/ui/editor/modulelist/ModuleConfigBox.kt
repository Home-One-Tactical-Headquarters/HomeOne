package dk.holonet.ui.editor.modulelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.holonet.config.ModuleConfig

@Composable
fun ModuleConfigBox(
    modifier: Modifier = Modifier,
    module: ModuleConfig,
) {
    Card(
        modifier = modifier.fillMaxWidth().wrapContentHeight()
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "v${module.version}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            module.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            module.author?.let {
                Text(
                    text = "By $it",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
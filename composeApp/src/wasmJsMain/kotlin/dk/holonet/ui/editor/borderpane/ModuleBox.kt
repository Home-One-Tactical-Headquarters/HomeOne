package dk.holonet.ui.editor.borderpane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.GripLinesSolid
import dk.holonet.config.ModuleConfig
import sh.calvin.reorderable.ReorderableCollectionItemScope

@Composable
internal fun ModuleBox(
    modifier: Modifier,
    scope: ReorderableCollectionItemScope,
    moduleConfig: ModuleConfig,
    fillMaxWidth: Boolean = true,
) {
    val cardModifier = if (fillMaxWidth) {
        modifier.fillMaxWidth().height(48.dp)
    } else {
        modifier.fillMaxHeight().fillMaxWidth(0.25f)
    }

    val rowModifier = if (fillMaxWidth) {
        Modifier.fillMaxSize()
    } else {
        Modifier.fillMaxHeight()
    }

    val horizontalArrangement = if (fillMaxWidth) {
        Arrangement.SpaceBetween
    } else {
        Arrangement.spacedBy(8.dp)
    }

    Card(
        modifier = with(scope) { cardModifier.draggableHandle() }
    ) {
        Row(
            modifier = rowModifier.padding(8.dp),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(moduleConfig.name)
            Icon(
                imageVector = LineAwesomeIcons.GripLinesSolid,
                contentDescription = "Drag handle",
                modifier = Modifier.size(24.dp).align(Alignment.CenterVertically)
            )
        }
    }


    /*Box(with(scope) {
        newModifier.draggableHandle()
    }) {
        Text(moduleConfig.name)
    }*/
}
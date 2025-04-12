package dk.holonet.ui.editor.borderpane

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.PlusSolid
import dk.holonet.config.ModuleConfig
import dk.holonet.core.Position
import dk.holonet.ui.editor.EditorViewModel
import dk.holonet.ui.prettyPrint
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun ColumnComponent(
    modifier: Modifier,
    viewModel: EditorViewModel,
    position: Position,
    state: Map<Position, List<ModuleConfig>>
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier
            .wrapContentHeight()
            .border(BorderStroke(1.dp, Color.Black)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            Text(position.prettyPrint())
            ModulePicker(
                LineAwesomeIcons.PlusSolid,
                "Add module",
                availableModules = viewModel.modules.value,
                selectedModules = state[position] ?: emptyList(),
                onModuleSelected = { module, isAdded ->
                    viewModel.updateModule(position, module, isAdded)
                },
            )
            Spacer(Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.Black)),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyListState
        ) {
            state[position]?.let { modules ->
                items(modules, key = { it }) { module ->
                    ReorderableItem(reorderableLazyListState, key = module) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                        ModuleBox(Modifier.shadow(elevation), this, module)
                    }
                }
            }
        }
    }


}
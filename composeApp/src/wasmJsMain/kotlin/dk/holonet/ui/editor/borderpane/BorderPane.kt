package dk.holonet.ui.editor.borderpane

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
internal fun BorderPane(
    modifier: Modifier,
    viewModel: EditorViewModel
) {
    val state by viewModel.positions.collectAsState()

    Box(modifier = modifier) {
        // Top bar (light gray), pinned to the top
        RowComponent(
            viewModel,
            modifier = Modifier.align(Alignment.TopCenter),
            Position.TOP_BAR,
            state
        )

        // Bottom bar (light gray), pinned to the bottom
        RowComponent(
            viewModel,
            modifier = Modifier.align(Alignment.BottomCenter),
            Position.BOTTOM_BAR,
            state
        )

        // Main content column, taking the remaining space between top/bottom bars
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 80.dp), // Same height used for top/bottom bars
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ----------------------
            // Top row with 3 blocks
            // ----------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                // top_left
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.TOP_LEFT,
                    state = state
                )

                // top_center
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.TOP_CENTER,
                    state = state
                )

                // top_right
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.TOP_RIGHT,
                    state = state
                )
            }

            // ---------------------------------------
            // Middle stack: upper_third, middle_center,
            // lower_third
            // ---------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // upper_third
                RowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel,
                    position = Position.UPPER_THIRD,
                    state = state
                )

                // middle_center
                RowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel,
                    position = Position.MIDDLE_THIRD,
                    state = state
                )

                // lower_third
                RowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel,
                    position = Position.LOWER_THIRD,
                    state = state
                )
            }

            // -------------------------
            // Bottom row with 3 blocks
            // -------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                verticalAlignment = Alignment.Bottom
            ) {
                // bottom_left
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.BOTTOM_LEFT,
                    state = state
                )

                // bottom_center
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.BOTTOM_CENTER,
                    state = state
                )

                // bottom_right
                ColumnComponent(
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    position = Position.BOTTOM_RIGHT,
                    state = state
                )
            }
        }
    }
}

@Composable
private fun RowComponent(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
    position: Position,
    state: Map<Position, List<ModuleConfig>>,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(BorderStroke(1.dp, Color.Black)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(32.dp)
        ) {
            Spacer(Modifier.weight(1f))
            Text(position.prettyPrint())
            IconButton(onClick = {}) {
                Icon(
                    imageVector = LineAwesomeIcons.PlusSolid,
                    contentDescription = "Add module",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1f))
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(BorderStroke(1.dp, Color.Black)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = lazyListState
        ) {
            state[position]?.let { modules ->
                items(modules, key = { it }) { module ->
                    ReorderableItem(reorderableLazyListState, key = module) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                        ModuleBox(Modifier.shadow(elevation), this, module, false)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnComponent(
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = LineAwesomeIcons.PlusSolid,
                    contentDescription = "Add module",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1f))
        }

//        Text(position.prettyPrint())

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
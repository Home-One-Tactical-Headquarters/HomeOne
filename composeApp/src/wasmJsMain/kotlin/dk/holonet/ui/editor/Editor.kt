package dk.holonet.ui.editor

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dk.holonet.core.Position
import dk.holonet.ui.prettyPrint
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
            modifier = Modifier.fillMaxHeight().weight(4f).background(Color.LightGray),
            viewModel = viewModel
        )
    }
}

@Composable
private fun BorderPane(
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
                .padding(vertical = 64.dp) // Same height used for top/bottom bars
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
                    .weight(1f) // You can adjust the weight to change relative height
            ) {
                // upper_third
                RowComponent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    viewModel = viewModel,
                    position = Position.UPPER_THIRD,
                    state = state
                )

                // middle_center
                RowComponent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    viewModel = viewModel,
                    position = Position.MIDDLE_THIRD,
                    state = state
                )

                // lower_third
                RowComponent(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
                    .weight(2f)
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
    state: Map<Position, List<Module>>,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.LightGray)
            .border(BorderStroke(1.dp, Color.Black)),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Text(position.prettyPrint())
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.LightGray)
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
    state: Map<Position, List<Module>>
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier
            .wrapContentHeight()
            .background(Color.LightGray)
            .border(BorderStroke(1.dp, Color.Black)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(position.prettyPrint())

        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(Color.LightGray)
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

@Composable
private fun ModulesList(
    modifier: Modifier,
    viewModel: EditorViewModel
) {
    val state by viewModel.modules.collectAsState()

    LazyColumn(
        modifier = modifier.background(Color.DarkGray),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state) { module ->
            Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Red)) {
                Text(module.name)
            }
        }
    }
}

@Composable
private fun ModuleBox(
    modifier: Modifier,
    scope: ReorderableCollectionItemScope,
    module: Module,
    fillMaxWidth: Boolean = true,
) {
    val newModifier = if (fillMaxWidth) {
        modifier.fillMaxWidth().height(48.dp).background(Color.Red)
    } else {
        modifier.fillMaxHeight().fillMaxWidth(0.25f).background(Color.Red)
    }

    Box(with(scope) {
        newModifier.draggableHandle()
    }) {
        Text(module.name)
    }
}
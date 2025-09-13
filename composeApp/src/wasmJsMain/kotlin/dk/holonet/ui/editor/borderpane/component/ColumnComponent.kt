package dk.holonet.ui.editor.borderpane.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.holonet.config.ModuleConfig
import dk.holonet.core.Position
import dk.holonet.ui.dialogs.ConfigEntry
import dk.holonet.ui.dialogs.asJsonElement
import dk.holonet.ui.editor.EditorViewModel
import dk.holonet.ui.editor.borderpane.ModuleBox
import kotlinx.serialization.json.JsonElement
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun ColumnComponent(
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel,
    position: Position,
    state: Map<Position, List<ModuleConfig>>
) {
    val (currentModuleConfig, setCurrentModuleConfig) = remember { mutableStateOf<ModuleConfig?>(null) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Header(position)
        ContentColumn(modifier, viewModel, lazyListState, state, position, reorderableLazyListState, setCurrentModuleConfig, currentModuleConfig)
    }
}

@Composable
private fun ContentColumn(
    modifier: Modifier,
    viewModel: EditorViewModel,
    lazyListState: LazyListState,
    state: Map<Position, List<ModuleConfig>>,
    position: Position,
    reorderableLazyListState: ReorderableLazyListState,
    setCurrentModuleConfig: (ModuleConfig?) -> Unit,
    currentModuleConfig: ModuleConfig?
) {
    FloatingActionButtonWrapper(viewModel, currentModuleConfig, setCurrentModuleConfig) {
        Row(
            modifier = modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(0.3f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                state[position]?.let { modules ->
                    items(modules, key = { it }) { module ->
                        ReorderableItem(reorderableLazyListState, key = module) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                            ModuleBox(Modifier.shadow(elevation), this, module) {
                                setCurrentModuleConfig(module)
                            }
                        }
                    }
                }
            }

            val editorModifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
                .weight(0.7f)
                .fillMaxHeight()

            when (currentModuleConfig) {
                null -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = editorModifier
                    ) {
                        Text(
                            "Select a module to view/edit its configuration",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = editorModifier,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentModuleConfig.config.entries.toList(), key = { it.key }) { (key, value) ->
                            val configMap = mutableMapOf<String, JsonElement>()

                            currentModuleConfig.config.forEach { (k, v) ->
                                configMap[k] = currentModuleConfig.instance?.config?.get(k)
                                    ?: v.default?.asJsonElement()
                                            ?: "".asJsonElement()
                            }
                            ConfigEntry(
                                key,
                                value,
                                onValueChange = { newValue ->
                                    configMap[key] = newValue.asJsonElement()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}




package dk.holonet.ui.editor.borderpane.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
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
internal fun RowComponent(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier,
    position: Position,
    state: Map<Position, List<ModuleConfig>>,
) {
    val (currentModuleConfig, setCurrentModuleConfig) = remember { mutableStateOf<ModuleConfig?>(null) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.reorderModules(position, from.index, to.index)
    }

    Column(
        modifier = modifier.fillMaxWidth().height(80.dp).border(BorderStroke(1.dp, Color.Black)),
        verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header(position)
        ContentRow(
            modifier, viewModel, lazyListState, state, position, reorderableLazyListState, setCurrentModuleConfig,
            currentModuleConfig
        )
    }
}

@Composable
private fun ContentRow(
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
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            LazyRow(
                modifier = Modifier.weight(0.1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), state = lazyListState
            ) {
                state[position]?.let { modules ->
                    val equalWidthFraction = 1f / modules.size.coerceAtLeast(1)
                    val minWidthFraction = 0.05f // 5% minimum
                    val widthFraction = maxOf(equalWidthFraction, minWidthFraction)

                    items(modules, key = { it }) { module ->
                        ReorderableItem(reorderableLazyListState, key = module) { isDragging ->
                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                            ModuleBox(
                                Modifier.shadow(elevation).width(200.dp), this@ReorderableItem, module, false
                            ) {
                                setCurrentModuleConfig(module)
                            }
                        }
                    }
                }
            }

            val editorModifier =
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp).weight(0.9f).fillMaxWidth()

            when (currentModuleConfig) {
                null -> {
                    Row(
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                        modifier = editorModifier
                    ) {
                        Text(
                            "Select a module to view/edit its configuration", textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = editorModifier, verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentModuleConfig.config.entries.toList(), key = { it.key }) { (key, value) ->
                            val configMap = mutableMapOf<String, JsonElement>()

                            currentModuleConfig.config.forEach { (k, v) ->
                                configMap[k] = currentModuleConfig.instance?.config?.get(k) ?: v.default?.asJsonElement()
                                        ?: "".asJsonElement()
                            }
                            ConfigEntry(
                                key, value, onValueChange = { newValue ->
                                    configMap[key] = newValue.asJsonElement()
                                })
                        }
                    }
                }
            }
        }
    }
}
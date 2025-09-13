package dk.holonet.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.holonet.config.ConfigProperty
import dk.holonet.config.ModuleConfig
import dk.holonet.core.Position
import dk.holonet.ui.editor.EditorViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.compose.koinInject

@Composable
fun ModuleConfigDialog(
    moduleConfig: ModuleConfig,
    position: Position,
    editorViewModel: EditorViewModel = koinInject(),
    onDismissRequest: () -> Unit
) {
    // Initializing a mutable map to hold the configuration values with default values
    val configMap = mutableMapOf<String, JsonElement>()
    moduleConfig.config.forEach { (key, value) ->
        println("Instance config: ${moduleConfig.instance?.config.toString()}")

        configMap[key] = moduleConfig.instance?.config?.get(key)
            ?: value.default?.asJsonElement()
                    ?: "".asJsonElement()
    }

    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = moduleConfig.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopCenter)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f)
            ) {
                items(moduleConfig.config.size) { index ->
                    val (key, value) = moduleConfig.config.entries.elementAt(index)
                    ConfigEntry(
                        key,
                        value,
                        onValueChange = { newValue ->
                            configMap[key] = newValue.asJsonElement()
                        })
                    if (index < moduleConfig.config.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                /*moduleConfig.config.forEach { (key, value) ->
                    ConfigEntry(key, value)
                    Spacer(modifier = Modifier.height(8.dp))
                }*/
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {}) {
                    Text("Cancel")
                }

                TextButton(onClick = {
                    // Save the configuration
                    editorViewModel.updateModuleConfig(position, moduleConfig, configMap)
                    onDismissRequest()
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEntry(
    key: String,
    config: ConfigProperty,
    onValueChange: ((String) -> Unit),
) {
    val name = if (config.required) {
        buildAnnotatedString {
            append(key)
            append(" ")
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.tertiary)) {
                append("*")
            }
        }
    } else {
        buildAnnotatedString { append(key) }
    }

    // General Text field
    var input by remember { mutableStateOf(config.default ?: "") }

    // Only for exposed dropdown selection
    val options: List<String> = config.values?.map { it } ?: emptyList()
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = name)
            config.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        val inputModifier = Modifier.weight(1f)

        when {
            config.values.isNullOrEmpty() -> {
                TextField(
                    value = input,
                    onValueChange = {
                        input = it
                        onValueChange(it)
                    },
                    label = { Text(key) },
                    modifier = inputModifier,
                )
            }

            config.values.isNotEmpty() -> {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = inputModifier
                ) {
                    TextField(
                        value = input,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        onValueChange = {
                            input = it
                            onValueChange(it)
                        },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    input = option
                                    expanded = false
                                    onValueChange(option)
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }
    }
}

fun String.asJsonElement(): JsonElement = Json.encodeToJsonElement(this)
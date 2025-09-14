package dk.holonet.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dk.holonet.config.ConfigProperty
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

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
            modifier = Modifier.weight(2f),
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
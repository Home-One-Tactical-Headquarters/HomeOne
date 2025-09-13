package dk.holonet.config

import dk.holonet.core.ModuleConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ModuleConfig(
    val name: String,
    val description: String? = null,
    val author: String? = null,
    val version: String,
    val config: Map<String, ConfigProperty>,
    var instance: ModuleConfiguration? = null // Specific instance of the module configuration. Defined later
)

@Serializable
data class ConfigProperty(
    val type: String,
    val description: String? = null,
    val default: String? = null,
    val required: Boolean = false,
    val values: List<String>? = null
)

private val json = Json { ignoreUnknownKeys = true }

fun loadConfig(jsonString: String): ModuleConfig {
    return json.decodeFromString<ModuleConfig>(jsonString)
}
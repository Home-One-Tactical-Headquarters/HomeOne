package dk.holonet.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ModuleConfig(
    val name: String,
    val description: String? = null,
    val author: String? = null,
    val version: String,
    val config: Map<String, ConfigProperty>
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
package dk.holonet.config

import dk.holonet.core.HolonetConfiguration
import dk.holonet.core.HolonetSchema
import dk.holonet.core.ModuleConfiguration
import dk.holonet.core.Position
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val json = Json { ignoreUnknownKeys = true }

fun loadConfig(jsonString: String): HolonetSchema {
    return json.decodeFromString<HolonetSchema>(jsonString)
}

/**
 * Converts a map of positions and their associated module schemas into a HolonetConfiguration.
 *
 * @return A [HolonetConfiguration] object.
 */
fun Map<Position, List<HolonetSchema>>.toHolonetConfiguration(): HolonetConfiguration {
    val moduleConfigurations = mutableMapOf<String, ModuleConfiguration>()
    this.forEach { (position, schemas) ->
        schemas.forEachIndexed { index, schema ->
            val config = schema.instance?.config ?: JsonObject(emptyMap())
            moduleConfigurations[schema.name.lowercase()] = ModuleConfiguration(
                position = position,
                priority = index,
                config = config
            )
        }
    }
    return HolonetConfiguration(modules = moduleConfigurations)
}
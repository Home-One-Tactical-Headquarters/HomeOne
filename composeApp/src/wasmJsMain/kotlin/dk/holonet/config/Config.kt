package dk.holonet.config

import dk.holonet.core.HolonetSchema
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun loadConfig(jsonString: String): HolonetSchema {
    return json.decodeFromString<HolonetSchema>(jsonString)
}
package dk.holonet.data

import dk.holonet.config.toHolonetConfiguration
import dk.holonet.core.HolonetConfiguration
import dk.holonet.core.HolonetSchema
import dk.holonet.core.ModuleConfiguration
import dk.holonet.core.Position
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class ModulesRepository(private val httpClient: HttpClient) {
    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    private val _positions = MutableStateFlow<Map<Position, List<HolonetSchema>>>(emptyMap())
    val positions: StateFlow<Map<Position, List<HolonetSchema>>> = _positions.asStateFlow()

    private val _modules = MutableStateFlow<List<HolonetSchema>>(emptyList())
    val modules: StateFlow<List<HolonetSchema>> = _modules.asStateFlow()

    private val _currentPosition = MutableStateFlow<Position?>(null)
    val currentPosition: StateFlow<Position?> = _currentPosition.asStateFlow()

    private val modulesMap: MutableMap<String, HolonetSchema> = mutableMapOf()

    init {
        loadConfiguration()
    }

    fun setCurrentPosition(position: Position?) {
        _currentPosition.value = position
    }

    fun reorderModules(position: Position, from: Int, to: Int) {
        _positions.update { currentPositions ->
            val listToReorder = currentPositions[position]?.toMutableList() ?: return@update currentPositions

            val moduleToMove = listToReorder.removeAt(from)
            listToReorder.add(to, moduleToMove)

            val updatedList = listToReorder.mapIndexed { index, module ->
                module.copy(instance = module.instance?.copy(priority = index))
            }
            currentPositions + (position to updatedList)
        }
        saveConfiguration()
    }

    fun updateModule(position: Position, module: HolonetSchema, isAdded: Boolean) {
        val currentPositions = _positions.value
        val currentList = currentPositions[position] ?: emptyList()
        val newList = if (isAdded) {
            val newModule = module.copy(instance = ModuleConfiguration(position, currentList.size))
            currentList + newModule
        } else {
            (currentList - module).mapIndexed { index, m ->
                m.copy(instance = m.instance?.copy(priority = index))
            }
        }
        _positions.update { it + (position to newList) }
        saveConfiguration()
    }

    fun updateModuleConfig(position: Position, module: HolonetSchema, newConfig: Map<String, JsonElement>) {
        val currentPositions = _positions.value
        val listToUpdate = currentPositions[position] ?: return
        val moduleIndex = listToUpdate.indexOf(module)
        if (moduleIndex == -1) return

        val oldModule = listToUpdate[moduleIndex]
        val updatedModule = oldModule.copy(
            instance = (oldModule.instance ?: ModuleConfiguration(position, moduleIndex)).copy(
                config = JsonObject(newConfig)
            )
        )

        val newList = listToUpdate.toMutableList().apply {
            this[moduleIndex] = updatedModule
        }

        _positions.update { it + (position to newList) }
        saveConfiguration()
    }

    fun saveConfiguration() {
        val holonetConfiguration = _positions.value.toHolonetConfiguration()
        repositoryScope.launch {
            httpClient.post("/update") {
                contentType(ContentType.Application.Json)
                setBody(holonetConfiguration)
            }
        }
    }

    suspend fun uploadModules(modules: List<PlatformFile>, overwrite: Boolean = false): List<String>? {
        val filesData = modules.map { file ->
            file.name to file.readBytes()
        }

        val url = if (overwrite) "/modules/overwrite" else "/modules"

        val response = httpClient.post(url) {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        filesData.forEach { (fileName, fileBytes) ->
                            append("files", fileBytes, Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Application.Zip.toString())
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    }
                )
            )
            onUpload { bytesSentTotal, contentLength ->
                val progress = if (contentLength!! > 0) {
                    bytesSentTotal.toDouble() / contentLength.toDouble()
                } else {
                    0.0
                }
                val percentage = (progress * 100).toInt()
//                    println("Upload progress: $percentage%")
            }
        }

        if (!response.status.isSuccess()) return null
        val errors = response.body<List<String>>()

        println("Errors: $errors")

        loadModules()
        delay(1000) // Small delay to ensure server processes the new modules
        loadConfiguration()

        return errors
    }

    suspend fun deleteModules(modules: List<String>) {
        httpClient.delete("/modules") {
            contentType(ContentType.Application.Json)
            setBody(modules)
        }

        loadModules()
        delay(1000) // Small delay to ensure server processes the new modules
        loadConfiguration()
    }

    private fun loadConfiguration() {
        repositoryScope.launch {
            loadModules()

            val holonetConfig = httpClient.get("/configuration").body<HolonetConfiguration>()
            val availableSchemas = modulesMap
            val newPositions = Position.entries.associateWith { mutableListOf<HolonetSchema>() }.toMutableMap()

            holonetConfig.modules.forEach { (name, config) ->
                availableSchemas[name]?.let { schema ->
                    val moduleWithInstance = schema.copy(instance = config)
                    newPositions[config.position]?.add(moduleWithInstance)
                } ?: println("No schema found for module name: $name")
            }

            _positions.value = newPositions.mapValues { (_, modules) ->
                modules.sortBy { it.instance?.priority ?: Int.MAX_VALUE }
                modules.toList()
            }
        }
    }

    private suspend fun loadModules() {
        val response = httpClient.get("/modules")
        modulesMap.putAll(response.body() as Map<String, HolonetSchema>)
        _modules.value = modulesMap.values.toList()
    }
}

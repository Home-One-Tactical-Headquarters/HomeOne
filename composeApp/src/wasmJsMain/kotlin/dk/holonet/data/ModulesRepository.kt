package dk.holonet.data

import dk.holonet.config.toHolonetConfiguration
import dk.holonet.core.*
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    private val modulesMap = mutableMapOf<String, HolonetSchema>()

    init {
        loadConfiguration()
    }

    fun setCurrentPosition(position: Position?) {
        _currentPosition.value = position
    }

    fun reorderModules(position: Position, from: Int, to: Int) {
        _positions.update { currentPositions ->
            val listToReorder = currentPositions[position]?.toMutableList()
                ?: return@update currentPositions

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
        _positions.update { currentPositions ->
            val currentList = currentPositions[position] ?: emptyList()
            val newList = if (isAdded) {
                buildAddedModuleList(currentList, module, position)
            } else {
                buildRemovedModuleList(currentList, module)
            }
            currentPositions + (position to newList)
        }
        saveConfiguration()
    }

    fun updateModuleConfig(position: Position, module: HolonetSchema, newConfig: Map<String, JsonElement>) {
        _positions.update { currentPositions ->
            val listToUpdate = currentPositions[position] ?: return@update currentPositions
            val moduleIndex = listToUpdate.indexOf(module)
            if (moduleIndex == -1) return@update currentPositions

            val updatedList = listToUpdate.toMutableList().apply {
                this[moduleIndex] = createUpdatedModule(this[moduleIndex], position, moduleIndex, newConfig)
            }
            currentPositions + (position to updatedList)
        }
        saveConfiguration()
    }

    fun saveConfiguration() {
        repositoryScope.launch {
            val holonetConfiguration = _positions.value.toHolonetConfiguration()
            httpClient.post("/configuration") {
                contentType(ContentType.Application.Json)
                setBody(holonetConfiguration)
            }
        }
    }

    suspend fun uploadModules(modules: List<PlatformFile>, overwrite: Boolean = false): List<String>? {
        val filesData = modules.map { it.name to it.readBytes() }
        val url = if (overwrite) MODULES_OVERWRITE_ENDPOINT else MODULES_ENDPOINT

        val response = httpClient.post(url) {
            setBody(createMultipartFormData(filesData))
            onUpload { bytesSentTotal, contentLength ->
                logUploadProgress(bytesSentTotal, contentLength)
            }
        }

        if (!response.status.isSuccess()) return null

        val errors = response.body<List<String>>()
        reloadModulesAndConfiguration()
        return errors
    }

    suspend fun deleteModules(modules: List<String>) {
        httpClient.delete(MODULES_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(modules)
        }
        reloadModulesAndConfiguration()
    }

    private fun buildAddedModuleList(
        currentList: List<HolonetSchema>,
        module: HolonetSchema,
        position: Position
    ): List<HolonetSchema> {
        val newModule = module.copy(
            instance = ModuleConfiguration(position, currentList.size)
        )
        return currentList + newModule
    }

    private fun buildRemovedModuleList(
        currentList: List<HolonetSchema>,
        module: HolonetSchema
    ): List<HolonetSchema> =
        (currentList - module).mapIndexed { index, m ->
            m.copy(instance = m.instance?.copy(priority = index))
        }

    private fun createUpdatedModule(
        oldModule: HolonetSchema,
        position: Position,
        moduleIndex: Int,
        newConfig: Map<String, JsonElement>
    ): HolonetSchema {
        val instance = oldModule.instance ?: ModuleConfiguration(position, moduleIndex)
        return oldModule.copy(
            instance = instance.copy(config = JsonObject(newConfig))
        )
    }

    private fun createMultipartFormData(filesData: List<Pair<String, ByteArray>>): MultiPartFormDataContent =
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

    private fun logUploadProgress(bytesSentTotal: Long, contentLength: Long?) {
        val progress = contentLength?.let {
            if (it > 0) bytesSentTotal.toDouble() / it.toDouble() else 0.0
        } ?: 0.0
        // Uncomment to see upload progress
        // println("Upload progress: ${(progress * 100).toInt()}%")
    }

    private suspend fun reloadModulesAndConfiguration() {
        loadModules()
        delay(RELOAD_DELAY_MS)
        loadConfiguration()
    }

    private fun loadConfiguration() {
        repositoryScope.launch {
            loadModules()

            val holonetConfig = httpClient.get(CONFIGURATION_ENDPOINT).body<HolonetConfiguration>()
            val newPositions = buildPositionsMap(holonetConfig)

            _positions.value = newPositions.mapValues { (_, modules) ->
                modules.sortedBy { it.instance?.priority ?: Int.MAX_VALUE }
            }
        }
    }

    private fun buildPositionsMap(holonetConfig: HolonetConfiguration): Map<Position, List<HolonetSchema>> {
        val newPositions = Position.entries.associateWith { mutableListOf<HolonetSchema>() }

        holonetConfig.modules.forEach { (name, config) ->
            modulesMap[name]?.let { schema ->
                val moduleWithInstance = schema.copy(instance = config)
                newPositions[config.position]?.add(moduleWithInstance)
            } ?: println("Warning: No schema found for module name: $name")
        }

        return newPositions
    }

    private suspend fun loadModules() {
        val response = httpClient.get(MODULES_ENDPOINT)
        val modules = response.body<Map<String, HolonetSchema>>()
        modulesMap.clear()
        modulesMap.putAll(modules)
        _modules.value = modulesMap.values.toList()
    }

    private companion object {
        const val CONFIGURATION_ENDPOINT = "/configuration"
        const val MODULES_ENDPOINT = "/modules"
        const val MODULES_OVERWRITE_ENDPOINT = "/modules/overwrite"
        const val RELOAD_DELAY_MS = 1000L
    }
}

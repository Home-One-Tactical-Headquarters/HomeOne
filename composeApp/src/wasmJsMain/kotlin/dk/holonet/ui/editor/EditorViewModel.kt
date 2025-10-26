package dk.holonet.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.holonet.config.toHolonetConfiguration
import dk.holonet.core.HolonetConfiguration
import dk.holonet.core.HolonetSchema
import dk.holonet.core.ModuleConfiguration
import dk.holonet.core.Position
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class EditorViewModel(
    private val httpClient: HttpClient
) : ViewModel() {
    private val _positions: MutableStateFlow<Map<Position, List<HolonetSchema>>> = MutableStateFlow(emptyMap())
    val positions: StateFlow<Map<Position, List<HolonetSchema>>> = _positions.asStateFlow()

    private val _modules: MutableStateFlow<List<HolonetSchema>> = MutableStateFlow(emptyList())
    val modules: StateFlow<List<HolonetSchema>> = _modules.asStateFlow()

    private val _currentPosition: MutableStateFlow<Position?> = MutableStateFlow(null)
    val currentPosition: StateFlow<Position?> = _currentPosition.asStateFlow()

    private val modulesMap: MutableMap<String, HolonetSchema> = mutableMapOf()

    init {
        loadConfiguration()
    }

    fun reorderModules(position: Position, from: Int, to: Int) {
        val currentPositions = _positions.value
        val listToReorder = currentPositions[position]?.toMutableList() ?: return

        val moduleToMove = listToReorder.removeAt(from)
        listToReorder.add(to, moduleToMove)

        // Update priorities for all modules in the affected list
        val updatedList = listToReorder.mapIndexed { index, module ->
            module.copy(instance = module.instance?.copy(priority = index))
        }

        _positions.update { currentPositions + (position to updatedList) }
    }

    private fun updateModule(position: Position, module: HolonetSchema, isAdded: Boolean) {
        val currentPositions = _positions.value
        val currentList = currentPositions[position] ?: emptyList()
        val newList = if (isAdded) {
            // Add module with new instance and priority
            val newModule = module.copy(instance = ModuleConfiguration(position, currentList.size))
            currentList + newModule
        } else {
            // Remove module
            currentList - module
        }

        _positions.update { currentPositions + (position to newList) }
    }

    fun addModule(module: HolonetSchema) {
        currentPosition.value?.let {
            updateModule(it, module, true)
        }
    }

    fun removeModule(module: HolonetSchema) {
        currentPosition.value?.let {
            updateModule(it, module, false)
        }
    }

    fun setCurrentPosition(position: Position?) {
        _currentPosition.value = position
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

        _positions.update { currentPositions + (position to newList) }
        saveConfiguration(_positions.value.toHolonetConfiguration())
    }

    fun saveConfiguration() {
        // TODO: Why is positions value different?
        println("positions.value2: ${positions.value}")
        val holonetConfiguration = positions.value.toHolonetConfiguration()
        println("Saving configuration: $holonetConfiguration")
        saveConfiguration(holonetConfiguration)
    }

    private fun saveConfiguration(configuration: HolonetConfiguration) {
        viewModelScope.launch {
            httpClient.post("/update") {
                contentType(ContentType.Application.Json)
                setBody(configuration)
            }
        }
    }

    private fun loadConfiguration() {
        viewModelScope.launch {
            if (_modules.value.isEmpty()) {
                loadModules()
            }

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
                modules.toList() // Convert to immutable List
            }
        }
    }

    private suspend fun loadModules() {
        val response = httpClient.get("/modules")
        modulesMap.putAll(response.body() as Map<String, HolonetSchema>)
        _modules.value = modulesMap.values.toList()
    }
}
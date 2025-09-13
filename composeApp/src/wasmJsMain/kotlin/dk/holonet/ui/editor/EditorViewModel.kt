package dk.holonet.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.holonet.config.ModuleConfig
import dk.holonet.config.loadConfig
import dk.holonet.core.ModuleConfiguration
import dk.holonet.core.Position
import dk.holonet.example_config.calendarConfig
import dk.holonet.example_config.clockConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class EditorViewModel: ViewModel() {
    private val _positions: MutableStateFlow<MutableMap<Position, List<ModuleConfig>>> = MutableStateFlow(mutableMapOf())
    val positions: StateFlow<Map<Position, List<ModuleConfig>>> = _positions.asStateFlow()

    private val _modules: MutableStateFlow<List<ModuleConfig>> = MutableStateFlow(emptyList())
    val modules: StateFlow<List<ModuleConfig>> = _modules.asStateFlow()

    private val _currentPosition: MutableStateFlow<Position?> = MutableStateFlow(null)
    val currentPosition: StateFlow<Position?> = _currentPosition.asStateFlow()

    init {
        Position.entries.forEach { position ->
            _positions.value[position] = mutableListOf()
        }

        loadModules()

        _positions.value.forEach {
            if (it.value.isEmpty()) {
                _positions.value[it.key] = _modules.value
            }
        }
    }

    fun reorderModules(position: Position, from: Int, to: Int) {
        viewModelScope.launch {
            val newList = _positions.value[position]?.toMutableList() ?: mutableListOf()
            val module = newList.removeAt(from)
            module.instance = module.instance?.copy(position = position)
            newList.add(to, module)

            val newState = _positions.value.toMutableMap()
            newState[position] = newList
            _positions.value = newState
        }
    }

    fun updateModule(position: Position, module: ModuleConfig, isAdded: Boolean) {
        viewModelScope.launch {
            val newList = _positions.value[position]?.toMutableList() ?: mutableListOf()

            if (!isAdded && newList.contains(module)) {
                newList.remove(module)
            } else if (isAdded && !newList.contains(module)) {
                module.instance = ModuleConfiguration(position, newList.size)
                newList.add(module)
            }

            val newState = _positions.value.toMutableMap()
            newState[position] = newList
            _positions.value = newState
        }
    }

    fun addModule(module: ModuleConfig) {
        if (currentPosition.value == null) return
        updateModule(currentPosition.value!!, module, true)
    }

    fun removeModule(module: ModuleConfig) {
        if (currentPosition.value == null) return
        updateModule(currentPosition.value!!, module, false)
    }

    fun setCurrentPosition(position: Position?) {
        _currentPosition.value = position
    }

    fun updateModuleConfig(position: Position, module: ModuleConfig, newConfig: Map<String, JsonElement>) {
        viewModelScope.launch {
            val newList = _positions.value[position]?.toMutableList() ?: mutableListOf()
            val index = newList.indexOf(module)
            if (index != -1) {
                val oldModule = newList[index]

                if (oldModule.instance == null) {
                    oldModule.instance = ModuleConfiguration(position, index)
                }

                val updatedModule = oldModule.copy(instance = oldModule.instance?.copy(config = JsonObject(newConfig)))
                newList[index] = updatedModule
                val newState = _positions.value.toMutableMap()
                newState[position] = newList
                _positions.value = newState
            }
        }
    }

    fun saveConfiguration() {
        viewModelScope.launch {
            println("Module configurations: ")
            _positions.value.forEach { (position, modules) ->
                println("Position: $position")
                modules.forEach { module ->
                    println("Module: ${module.name}, Config: ${module.instance?.config}")
                }
            }
        }
    }

    private fun loadModules() {
        viewModelScope.launch {
            // Simulate config loading
            val configs = listOf(calendarConfig, clockConfig)
            _modules.value = configs.map {
                loadConfig(it)
            }
        }
    }
}
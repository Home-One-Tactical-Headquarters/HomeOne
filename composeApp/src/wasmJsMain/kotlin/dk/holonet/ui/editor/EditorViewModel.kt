package dk.holonet.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.holonet.config.ModuleConfig
import dk.holonet.config.loadConfig
import dk.holonet.core.Position
import dk.holonet.example_config.calendarConfig
import dk.holonet.example_config.clockConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditorViewModel: ViewModel() {
    private val _positions: MutableStateFlow<MutableMap<Position, List<ModuleConfig>>> = MutableStateFlow(mutableMapOf())
    val positions: StateFlow<Map<Position, List<ModuleConfig>>> = _positions.asStateFlow()

    private val _modules: MutableStateFlow<List<ModuleConfig>> = MutableStateFlow(emptyList())
    val modules: StateFlow<List<ModuleConfig>> = _modules.asStateFlow()

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
            newList.add(to, module)

            val newState = _positions.value.toMutableMap()
            newState[position] = newList
            _positions.value = newState
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
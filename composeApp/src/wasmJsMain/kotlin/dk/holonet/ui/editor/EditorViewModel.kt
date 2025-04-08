package dk.holonet.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.holonet.core.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditorViewModel: ViewModel() {
    private val _positions: MutableStateFlow<MutableMap<Position, List<Module>>> = MutableStateFlow(mutableMapOf())
    val positions: StateFlow<Map<Position, List<Module>>> = _positions.asStateFlow()

    private val _modules: MutableStateFlow<List<Module>> = MutableStateFlow(emptyList())
    val modules: StateFlow<List<Module>> = _modules.asStateFlow()

    init {
        Position.entries.forEach { position ->
            _positions.value[position] = mutableListOf()
        }

        val testList = listOf(Module("Example1"), Module("Example2"))
//        _positions.value[Position.TOP_BAR] = testList

        _positions.value.forEach {
            if (it.value.isEmpty()) {
                _positions.value[it.key] = testList
            }
        }

        _modules.value = testList

        viewModelScope.launch {
            _positions.collectLatest {

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
}

data class Module(
    val name: String
)
package dk.holonet.ui.editor.borderpane

import androidx.lifecycle.ViewModel
import dk.holonet.core.HolonetSchema
import dk.holonet.core.Position
import dk.holonet.data.ModulesRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement

class BorderPaneViewModel(
    private val repository: ModulesRepository
) : ViewModel() {
    val positions: StateFlow<Map<Position, List<HolonetSchema>>> = repository.positions
    val modules: StateFlow<List<HolonetSchema>> = repository.modules

    fun reorderModules(position: Position, from: Int, to: Int) {
        repository.reorderModules(position, from, to)
    }

    fun onDragEnd() {
        repository.saveConfiguration()
    }

    fun removeModule(module: HolonetSchema) {
        repository.currentPosition.value?.let {
            repository.updateModule(it, module, false)
        }
    }

    fun setCurrentPosition(position: Position?) {
        repository.setCurrentPosition(position)
    }

    fun updateModuleConfig(position: Position, module: HolonetSchema, newConfig: Map<String, JsonElement>) {
        repository.updateModuleConfig(position, module, newConfig)
    }
}
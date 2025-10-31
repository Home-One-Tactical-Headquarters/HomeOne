package dk.holonet.ui.editor.modulelist

import androidx.lifecycle.ViewModel
import dk.holonet.core.HolonetSchema
import dk.holonet.core.Position
import dk.holonet.data.ModulesRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.StateFlow

class ModuleListViewModel(
    private val repository: ModulesRepository
): ViewModel() {
    val modules: StateFlow<List<HolonetSchema>> = repository.modules
    val currentPosition: StateFlow<Position?> = repository.currentPosition

    fun addModule(module: HolonetSchema) {
        repository.currentPosition.value?.let {
            repository.updateModule(it, module, true)
        }
    }

    fun uploadModules(modules: List<PlatformFile>?) {
        if (modules == null) {
            println("No modules to upload")
            return
        }
        repository.uploadModules(modules)
    }
}
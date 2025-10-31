package dk.holonet.ui.editor.modulelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.holonet.core.HolonetSchema
import dk.holonet.core.Position
import dk.holonet.data.ModulesRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModuleListViewModel(
    private val repository: ModulesRepository
) : ViewModel() {
    val modules: StateFlow<List<HolonetSchema>> = repository.modules
    val currentPosition: StateFlow<Position?> = repository.currentPosition

    private val _overwriteConfirmation = MutableStateFlow<List<PlatformFile>?>(null)
    val overwriteConfirmation: StateFlow<List<PlatformFile>?> = _overwriteConfirmation.asStateFlow()

    private var filesToUpload: List<PlatformFile> = emptyList()

    fun addModule(module: HolonetSchema) {
        repository.currentPosition.value?.let {
            repository.updateModule(it, module, true)
        }
    }

    fun uploadModules(modules: List<PlatformFile>?) {
        viewModelScope.launch {
            if (modules.isNullOrEmpty()) {
                println("No modules to upload")
                return@launch
            }

            val existingModuleNames = repository.getModuleNames()

            filesToUpload = modules

            val conflictingFiles = modules.filter {
                val moduleNameWithoutExt = it.name.removeSuffix(".zip").removeSuffix(".rar")
                val moduleName = moduleNameWithoutExt.replace(Regex("(-\\d+(\\.\\d+)*)$"), "")
                existingModuleNames.contains(moduleName)
            }

            if (conflictingFiles.isNotEmpty()) {
                _overwriteConfirmation.value = conflictingFiles
            } else {
                confirmUpload()
            }
        }
    }

    fun confirmUpload() {
        if (filesToUpload.isNotEmpty()) {
            repository.uploadModules(filesToUpload)
        }
        filesToUpload = emptyList()
        _overwriteConfirmation.value = null
    }

    fun cancelUpload() {
        filesToUpload = emptyList()
        _overwriteConfirmation.value = null
    }
}
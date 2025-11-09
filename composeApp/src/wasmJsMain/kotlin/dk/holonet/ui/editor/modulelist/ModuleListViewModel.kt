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

    fun addModule(module: HolonetSchema) {
        currentPosition.value?.let { position ->
            repository.updateModule(position, module, isAdded = true)
        }
    }

    fun removeModule(pluginIds: List<String>) {
        viewModelScope.launch {
            repository.deleteModules(pluginIds)
        }
    }

    fun uploadModules(modules: List<PlatformFile>?) {
        if (modules.isNullOrEmpty()) {
            println("No modules to upload")
            return
        }

        viewModelScope.launch {
            val response = repository.uploadModules(modules) ?: run {
                // TODO: Handle upload error
                return@launch
            }

            if (response.isNotEmpty()) {
                handleExistingModules(modules, response)
            }
        }
    }

    fun confirmUpload() {
        viewModelScope.launch {
            _overwriteConfirmation.value?.takeIf { it.isNotEmpty() }?.let { modules ->
                repository.uploadModules(modules, overwrite = true)
            }
            clearOverwriteConfirmation()
        }
    }

    fun cancelUpload() {
        clearOverwriteConfirmation()
    }

    private fun handleExistingModules(
        modules: List<PlatformFile>,
        existingModuleNames: List<String>
    ) {
        _overwriteConfirmation.value = modules.filter { it.name in existingModuleNames }
    }

    private fun clearOverwriteConfirmation() {
        _overwriteConfirmation.value = null
    }
}
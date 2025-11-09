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
        repository.currentPosition.value?.let {
            repository.updateModule(it, module, true)
        }
    }

    fun removeModule(pluginIds: List<String>) {
        viewModelScope.launch {
            repository.deleteModules(pluginIds)
        }
    }

    fun uploadModules(modules: List<PlatformFile>?) {
        viewModelScope.launch {
            if (modules.isNullOrEmpty()) {
                println("No modules to upload")
                return@launch
            }

            val response = repository.uploadModules(modules)

            if (response == null) {
                // Handle error
            } else {
                if (response.isEmpty()) {
                    // Successful upload
                    return@launch
                }

                // Handle existing modules
                _overwriteConfirmation.value = modules.filter { it.name in response }
            }
        }
    }

    fun confirmUpload() {
        viewModelScope.launch {
            if (_overwriteConfirmation.value?.isEmpty() == false) {
                repository.uploadModules(_overwriteConfirmation.value!!, true)
            }
            _overwriteConfirmation.value = null
        }
    }

    fun cancelUpload() {
        _overwriteConfirmation.value = null
    }
}
package dk.holonet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val httpClient: HttpClient
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state = _state.asStateFlow()

    fun fetch() {
        viewModelScope.launch {
            println("Fetching data...")
            val response = httpClient.get("/")
            println("Response: $response")
            _state.emit(UiState.Success(response.bodyAsText()))
        }
    }

    fun update() {
        viewModelScope.launch {
            println("Updating data...")
            val response = httpClient.post("/update") {
                contentType(ContentType.Application.Json)
                setBody(testJson)
            }
            println("Response: $response")
            if (response.status.value == 200) {
                _state.emit(UiState.Success(response.bodyAsText()))
            } else {
                _state.emit(UiState.Error("Failed to update configuration"))
            }
        }
    }


    sealed class UiState {
        data object Loading : UiState()
        data class Success(val data: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}

private val testJson: String = """
{
    "modules": {
        "clock": {
            "position": "top_left",
            "priority": 0
        },
        "calendar": {
            "position": "top_left",
            "priority": 1,
            "config": {
                "url": "https://calendar.google.com/calendar/ical/2vtvf94piqaq2fkc3eju74rjjc%40group.calendar.google.com/private-7feea036760e905ce7e11ead218b804a/basic.ics"
            }
        }
    }
}
""".trimIndent()
package dk.holonet.di

import dk.holonet.ui.AppViewModel
import dk.holonet.ui.editor.EditorViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val diModules = module {
    single { HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        defaultRequest {
            // This configures the client to properly handle CORS
            headers.append(HttpHeaders.Accept, "application/json")
            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8081
            }
        }
    }}
    viewModel { AppViewModel(get()) }
    viewModel { EditorViewModel(get()) }
}
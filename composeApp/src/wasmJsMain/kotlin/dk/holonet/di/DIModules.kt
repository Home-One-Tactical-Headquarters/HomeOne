package dk.holonet.di

import dk.holonet.data.ModulesRepository
import dk.holonet.ui.AppViewModel
import dk.holonet.ui.editor.borderpane.BorderPaneViewModel
import dk.holonet.ui.editor.modulelist.ModuleListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.browser.window
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
                val location = window.location // Use the same host and port that served this page
                protocol = if (location.protocol == "https:") URLProtocol.HTTPS else URLProtocol.HTTP
                host = location.hostname
                port = location.port.toIntOrNull() ?: if (protocol == URLProtocol.HTTPS) 443 else 8080
            }
        }
    }}
    single { ModulesRepository(get()) }
    viewModel { AppViewModel(get()) }
    viewModel { BorderPaneViewModel(get()) }
    viewModel { ModuleListViewModel(get()) }
}
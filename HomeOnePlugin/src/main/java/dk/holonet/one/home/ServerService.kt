package dk.holonet.one.home

import dk.holonet.core.HolonetConfiguration
import dk.holonet.core.services.ConfigurationService
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.io.File

class ServerService(
    private val configurationService: ConfigurationService
) {
    private lateinit var server: EmbeddedServer<*, *>

    suspend fun start(path: String) = coroutineScope {
        server = embeddedServer(Netty, port = SERVER_PORT) {
            module(configurationService, path)
        }
        server.start(wait = true)
    }

    fun stop() {
        server.stop(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
    }

    private companion object {
        const val SERVER_PORT = 8081
        const val STOP_GRACE_PERIOD_MS = 1000L
        const val STOP_TIMEOUT_MS = 1000L
    }
}

fun Application.module(
    configurationService: ConfigurationService,
    path: String
) {
    configureContentNegotiation()
    configureCors()
    configureRouting(configurationService, path)
}

private fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

private fun Application.configureCors() {
    install(CORS) {
        anyHost() // TODO: Replace with specific host
        allowCredentials = true
        allowHeaders { true }
        allowHeader(HttpHeaders.ContentType)
    }
}

private fun Application.configureRouting(
    configurationService: ConfigurationService,
    path: String
) {
    routing {
        configureSinglePageApplication(path)
        configurationRoutes(configurationService)
        moduleRoutes(configurationService)
    }
}

private fun Route.configureSinglePageApplication(path: String) {
    singlePageApplication {
        useResources = false
        filesPath = path
        defaultPage = "index.html"
    }
}

private fun Route.configurationRoutes(configurationService: ConfigurationService) {
    route("/configuration") {
        get {
            val config = configurationService.cachedConfig.value ?: HolonetConfiguration()
            call.respond(config)
        }

        post {
            val newConfig = call.receive<HolonetConfiguration>()
            configurationService.updateConfiguration(newConfig)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.moduleRoutes(configurationService: ConfigurationService) {
    route("/modules") {
        get {
            val schemas = configurationService.fetchConfigurationSchema()
            call.respond(schemas)
        }

        delete {
            val pluginIds = call.receive<List<String>>()
            configurationService.deleteModules(pluginIds)
            call.respond(HttpStatusCode.OK)
        }

        post {
            val files = call.receiveModuleFiles()
            val response = configurationService.addModules(files)
            call.respond(response)
        }

        post("/overwrite") {
            val files = call.receiveModuleFiles()
            val response = configurationService.addModules(files, overwrite = true)
            call.respond(response)
        }
    }
}

/**
 * Receives multipart files from the call and saves them to temporary files.
 * @return A list of [PlatformFile] pointing to the saved temporary files.
 */
private suspend fun ApplicationCall.receiveModuleFiles(): List<PlatformFile> = buildList {
    receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                val fileName = requireNotNull(part.originalFileName) { "File name is required" }
                val tempFile = createTempFile(fileName)
                part.provider().copyAndClose(tempFile.writeChannel())
                add(PlatformFile(tempFile))
            }
            else -> part.dispose()
        }
    }
}

/**
 * Creates a temporary file with the given name, deleting any existing file with the same name.
 */
private fun createTempFile(fileName: String): File {
    val tempDir = File(System.getProperty("java.io.tmpdir"))
    return File(tempDir, fileName).apply {
        if (exists()) delete()
    }
}
package dk.holonet.one.home

import dk.holonet.core.HolonetConfiguration
import dk.holonet.core.services.ConfigurationService
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.singlePageApplication
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.io.File

class ServerService(
    private val configurationService: ConfigurationService
) {
    private lateinit var server: EmbeddedServer<*, *>

    suspend fun start(path: String) {
        coroutineScope {
            server = embeddedServer(Netty, port = 8081, module = { module(configurationService, path) })
            server.start(wait = true)
        }
    }

    fun stop() {
        server.stop(1000, 1000)
    }
}

fun Application.module(
    configurationService: ConfigurationService,
    path: String
) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CORS) {
        anyHost() // TODO: Replace with specific host
        allowCredentials = true
        allowHeaders { true }
        allowHeader(HttpHeaders.ContentType)
    }

    routing {
        singlePageApplication {
            useResources = false
            filesPath = path
            defaultPage = "index.html"
        }

        get("/configuration") {
            val config: HolonetConfiguration = configurationService.cachedConfig.value ?: HolonetConfiguration()
            call.respond(config)
        }

        get("/modules") {
            val schemas = configurationService.fetchConfigurationSchema()
            call.respond(HttpStatusCode.OK, schemas)
        }

        delete("/modules") {
            val pluginIds = call.receive<List<String>>()
            configurationService.deleteModules(pluginIds)
            call.respond(HttpStatusCode.OK)
        }

        post("/modules") {
            val files = call.receiveModuleFiles()
            val response = configurationService.addModules(files)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/modules/overwrite") {
            val files = call.receiveModuleFiles()
            val response = configurationService.addModules(files, true)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/update") {
            val newConfig: HolonetConfiguration = call.receive()
            configurationService.updateConfiguration(newConfig)
            call.respond(HttpStatusCode.OK)
        }

    }
}

/**
 * Receives multipart files from the call and saves them to temporary files.
 * @return A list of [PlatformFile] pointing to the saved temporary files.
 */
private suspend fun ApplicationCall.receiveModuleFiles(): List<PlatformFile> {
    val files = mutableListOf<PlatformFile>()
    val multipart = receiveMultipart()

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                val fileName = part.originalFileName as String
                val tempDir = File(System.getProperty("java.io.tmpdir"))
                val tempFile = File(tempDir, fileName)
                if (tempFile.exists()) {
                    tempFile.delete()
                }
                part.provider().copyAndClose(tempFile.writeChannel())
                files.add(PlatformFile(tempFile))
            }
            else -> part.dispose()
        }
    }
    return files
}
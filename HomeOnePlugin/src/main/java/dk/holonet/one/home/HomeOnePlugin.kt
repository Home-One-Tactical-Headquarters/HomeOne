package dk.holonet.one.home

import androidx.compose.runtime.Composable
import dk.holonet.core.HoloNetModule
import dk.holonet.core.HoloNetPlugin
import dk.holonet.core.services.ConfigurationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.pf4j.Extension
import org.pf4j.PluginWrapper
import java.io.File
import java.util.jar.JarFile

class HomeOnePlugin(wrapper: PluginWrapper) : HoloNetPlugin(wrapper), KoinComponent {

    private val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val configurationService: ConfigurationService by inject()
    private val serverService: ServerService = ServerService(configurationService)

    private var extractedHomeOneDir: File? = null

    override fun start() {
        super.start()

        pluginScope.launch {
            val extractedDir = extractHomeOneFromJar("${wrapper.pluginPath}")
            println("HomeOnePlugin started server on port 8081\nUsing files from ${extractedDir.absolutePath}")
            serverService.start(path = extractedDir.absolutePath)
        }
    }

    override fun stop() {
        super.stop()
        println("HomeOnePlugin stopping server")
        serverService.stop()
        pluginScope.cancel()
        extractedHomeOneDir?.deleteRecursively()
        extractedHomeOneDir = null
    }

    @Extension
    class HomeOneModule() : HoloNetModule() {
        @Composable
        override fun render() {
            // No UI for this module
        }
    }

    private fun extractHomeOneFromJar(jarPath: String): File {
        val tempDir = File(System.getProperty("java.io.tmpdir"))
        val destDir = File(tempDir, "HomeOne_${System.currentTimeMillis()}")
        val jarFile = JarFile(jarPath)
        jarFile.entries().asSequence()
            .filter { it.name.startsWith("HomeOne/") && !it.isDirectory }
            .forEach { entry ->
                val outFile = File(destDir, entry.name.removePrefix("HomeOne/"))
                outFile.parentFile.mkdirs()
                jarFile.getInputStream(entry).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        jarFile.close()
        extractedHomeOneDir = destDir
        return destDir
    }
}

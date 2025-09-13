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

class HomeOnePlugin(wrapper: PluginWrapper) : HoloNetPlugin(wrapper), KoinComponent {

    private val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val configurationService: ConfigurationService by inject()
    private val serverService: ServerService = ServerService(configurationService)

    override fun start() {
        super.start()

        pluginScope.launch {
            println("HomeOnePlugin started server on port 8081\nUsing files from ${wrapper.pluginPath}\\classes\\HomeOne")
            serverService.start(path = "${wrapper.pluginPath}\\classes\\HomeOne")
        }
    }

    override fun stop() {
        super.stop()
        println("HomeOnePlugin stopping server")
        serverService.stop()
        pluginScope.cancel()
    }

    @Extension
    class HomeOneModule() : HoloNetModule() {
        @Composable
        override fun render() {
            // No UI for this module
        }
    }
}

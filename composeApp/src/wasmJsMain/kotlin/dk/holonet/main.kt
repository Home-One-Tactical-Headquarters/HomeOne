package dk.holonet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import androidx.navigation.compose.rememberNavController
import dk.holonet.di.diModules
import dk.holonet.ui.App
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {

    startKoin {
        modules(diModules)
    }

    ComposeViewport(document.body!!) {
        App(onNavHostReady = { it.bindToBrowserNavigation() })
    }
}
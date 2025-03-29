package dk.holonet

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dk.holonet.di.diModules
import dk.holonet.ui.App
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    startKoin {
        modules(diModules)
    }

    ComposeViewport(document.body!!) {
        App()
    }
}
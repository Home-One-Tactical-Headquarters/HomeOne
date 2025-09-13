package dk.holonet.ui.editor.borderpane

import dk.holonet.core.Position

internal sealed class Screen(val route: String) {
    data object Main : Screen("/main")
    data class Config(val position: Position) : Screen("/config/$position") {
        companion object {
            const val ROUTE_PATTERN = "/config/{position}"
        }
    }
}
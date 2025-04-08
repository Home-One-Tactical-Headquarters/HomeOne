package dk.holonet.ui

import dk.holonet.core.Position

fun Position.prettyPrint(): String {
    return when (this) {
        Position.TOP_BAR -> "Top Bar"
        Position.BOTTOM_BAR -> "Bottom Bar"
        Position.TOP_LEFT -> "Top Left"
        Position.TOP_CENTER -> "Top Center"
        Position.TOP_RIGHT -> "Top Right"
        Position.BOTTOM_LEFT -> "Bottom Left"
        Position.BOTTOM_CENTER -> "Bottom Center"
        Position.BOTTOM_RIGHT -> "Bottom Right"
        Position.UPPER_THIRD -> "Upper Third"
        Position.MIDDLE_THIRD -> "Middle Third"
        Position.LOWER_THIRD -> "Lower Third"
    }
}
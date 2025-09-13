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

fun Position.toNavigatorString(): String {
    return when (this) {
        Position.TOP_BAR -> "top_bar"
        Position.BOTTOM_BAR -> "bottom_bar"
        Position.TOP_LEFT -> "top_left"
        Position.BOTTOM_LEFT -> "bottom_left"
        Position.TOP_RIGHT -> "top_right"
        Position.BOTTOM_RIGHT -> "bottom_right"
        Position.TOP_CENTER -> "top_center"
        Position.BOTTOM_CENTER -> "bottom_center"
        Position.UPPER_THIRD -> "upper_third"
        Position.MIDDLE_THIRD -> "middle_third"
        Position.LOWER_THIRD -> "lower_third"
    }
}

fun String.fromNavigatorString(): Position {
    return when (this) {
        "top_bar" -> Position.TOP_BAR
        "bottom_bar" -> Position.BOTTOM_BAR
        "top_left" -> Position.TOP_LEFT
        "bottom_left" -> Position.BOTTOM_LEFT
        "top_right" -> Position.TOP_RIGHT
        "bottom_right" -> Position.BOTTOM_RIGHT
        "top_center" -> Position.TOP_CENTER
        "bottom_center" -> Position.BOTTOM_CENTER
        "upper_third" -> Position.UPPER_THIRD
        "middle_third" -> Position.MIDDLE_THIRD
        "lower_third" -> Position.LOWER_THIRD
        else -> Position.TOP_CENTER // Default fallback
    }
}

fun Position.isVertical(): Boolean {
    return when (this) {
        Position.TOP_LEFT,
        Position.TOP_CENTER,
        Position.TOP_RIGHT,
        Position.BOTTOM_LEFT,
        Position.BOTTOM_CENTER,
        Position.BOTTOM_RIGHT -> true

        else -> false
    }
}
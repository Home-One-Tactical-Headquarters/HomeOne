package dk.holonet.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val colors = darkColors(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    primaryVariant = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    error = Color.Red,
    onError = Color.White
)

@Composable
fun HoloNetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = colors,
        content = content
    )
}
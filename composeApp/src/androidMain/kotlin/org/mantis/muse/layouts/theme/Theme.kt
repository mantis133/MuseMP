package org.mantis.muse.layouts.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColourScheme = darkColorScheme(
    background = Color.Black,
    onBackground = Color.White,
    primary = Purple80,
    onPrimary = Color.Black,
    secondary = Purple40,
)

private val LightColourScheme = lightColorScheme(
    background = Purple40
)


@Composable
fun MuseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colours = if (darkTheme) DarkColourScheme else LightColourScheme

    MaterialTheme(
        colorScheme = colours,
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}

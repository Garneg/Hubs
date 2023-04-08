package com.garnegsoft.hubs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

private val DarkColorPalette = darkColors(
    primary = Color(0xFF23CF00),
    primaryVariant = Color(0xFF4CB43B),
    secondary = Color(0xFF8AD582),
    background = Color(0, 0, 0, 255),
    surface = Color(20, 20, 20, 255)
)

@Composable
private fun LightColorPalette() = lightColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryVariantColor,
    secondary = SecondaryColor,
    background = Color(240, 240, 240),
    surface = Color.White,
    error = Color(0xFFEB4242),


    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

// TODO add support for dark theme
@Composable
fun HubsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette()
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
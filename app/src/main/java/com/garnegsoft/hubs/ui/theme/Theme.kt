package com.garnegsoft.hubs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

private val DarkColorPalette = darkColors(
    primary = Color(0xFFE7E7E7),
    primaryVariant = Color(0xFFDADEDF),
    secondary = Color(0xCDFFFFFF),
    onSecondary = Color(0x88FFFFFF),
    background = Color(32, 32, 32, 255),
    surface = Color(49, 49, 49, 255),
    onSurface = Color(0xFFDADADA),
    onBackground = Color(0xFFC2C2C2),
    onError = Color.White
)


private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryVariantColor,
    secondary = SecondaryColor,
    secondaryVariant = SecondaryVariantColor,
    onSecondary = Color(0xFF5B768B),
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
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
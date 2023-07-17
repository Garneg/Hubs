package com.garnegsoft.hubs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.Switch
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material3.MaterialTheme as Material3Theme

private val DarkColorPalette = darkColors(
    primary = Color(0xFFE7E7E7),
    primaryVariant = Color(0xFFDADEDF),
    secondary = Color(0xFFECECEC),
    onSecondary = Color(0x88FFFFFF),
    background = Color(32, 32, 32, 255),
    surface = Color(49, 49, 49, 255),
    onSurface = Color(0xFFDADADA),
    onBackground = Color(0xFFC2C2C2),
    secondaryVariant = Color(0xFFB4B4B4),
    onError = Color.White
)


private val LightColorPalette = lightColors(
    primary = PrimaryColor,
    primaryVariant = PrimaryVariantColor,
    secondary = SecondaryColor,
    secondaryVariant = SecondaryVariantColor,
    onSecondary = Color(0xFFF7FCFF),
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
    val systemUiController = rememberSystemUiController()
    val colors = if (darkTheme) {
        systemUiController.setSystemBarsColor(DarkColorPalette.surface)
        DarkColorPalette
    } else {
        systemUiController.setSystemBarsColor(LightColorPalette.primary)
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

val m3LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    inversePrimary = Color(0xFFFF0000),
    secondary = SecondaryColor,
    primaryContainer = PrimaryVariantColor,
    surfaceVariant = Color(0xFFEBF0F7),
    outline = Color(0xFFB3B7BB)
)

@Preview
@Composable
fun HubsM3Theme() {
    Material3Theme(
        m3LightColorScheme
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            var checked by remember {
                mutableStateOf(false)
            }
            Switch(checked = checked, onCheckedChange = { checked = !checked })

        }

    }
}
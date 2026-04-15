package com.garnegsoft.hubs.ui.theme

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import kotlinx.coroutines.flow.first


val DarkColorSchemeM3 = darkColorScheme(
    
)

val LightColorSchemeM3 = lightColorScheme(

)

@Composable
fun HubsWidgetThemeM3(
    useAdaptiveColors: Boolean,
    content: @Composable () -> Unit) {

    GlanceTheme(
        colors = if (Build.VERSION.SDK_INT >= 31 && useAdaptiveColors){
            GlanceTheme.colors
        } else {
            HubsWidgetColorProviders()
        },
        content = content
    )

}

fun HubsWidgetColorProviders(): ColorProviders {
    return colorProviders(
        primary = ColorProvider(day = PrimaryColor, night = Color(49, 49, 49, 255)),
        primaryContainer = ColorProvider(day = Color.White, night = Color(49, 49, 49, 255)),
        widgetBackground = ColorProvider(day = Color(240, 240, 240), night = Color(32, 32, 32, 255)),
        onPrimary = ColorProvider(day = Color.White, night = Color.White),
        onPrimaryContainer = ColorProvider(day = Color.Black, night = Color.White),
        secondaryContainer = ColorProvider(day = Color.White, night = Color(49, 49, 49, 255)),
        onSecondaryContainer = ColorProvider(day = Color.Black, night = Color.White),
        onSurface = ColorProvider(day = Color.Black, night = Color.White),

        secondary = ColorProvider(day = Color.White, night = Color.Black),
        onSecondary = ColorProvider(day = Color.White, night = Color.Black),

        tertiary = ColorProvider(day = Color.White, night = Color.Black),
        onTertiary = ColorProvider(day = Color.White, night = Color.Black),
        tertiaryContainer = ColorProvider(day = Color.White, night = Color.Black),
        onTertiaryContainer = ColorProvider(day = Color.White, night = Color.Black),
        error = ColorProvider(day = Color.White, night = Color.Black),
        errorContainer = ColorProvider(day = Color.White, night = Color.Black),
        onError = ColorProvider(day = Color.White, night = Color.Black),
        onErrorContainer = ColorProvider(day = Color.White, night = Color.Black),
        background = ColorProvider(day = Color.White, night = Color.Black),
        onBackground = ColorProvider(day = Color.White, night = Color.Black),

        surface = ColorProvider(day = Color.White, night = Color.Black),
        surfaceVariant = ColorProvider(day = Color.White, night = Color.Black),
        onSurfaceVariant = ColorProvider(day = Color.White, night = Color.Black),
        outline = ColorProvider(day = Color.White, night = Color.Black),
        inverseOnSurface = ColorProvider(day = Color.White, night = Color.Black),
        inverseSurface = ColorProvider(day = Color.White, night = Color.Black),
        inversePrimary = ColorProvider(day = Color.White, night = Color.Black),
    )
}
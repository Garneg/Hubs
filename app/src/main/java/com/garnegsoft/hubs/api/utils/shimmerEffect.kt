package com.garnegsoft.hubs.api.utils

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.emptyCacheFontFamilyResolver
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection


@Composable
fun Modifier.shimmerEffect(
    enabled: Boolean,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.onBackground.copy(0.1f)
): Modifier {
    val enabledTransition = updateTransition(enabled)
    val animatedAlpha by enabledTransition.animateFloat(transitionSpec = { tween(durationMillis = 300) }) { if (it) 1f else 0f }
    // TODO: implement shimmer animation
    return this.drawWithContent {
        if (enabled) {
            val densityStruct = Density(this.density, this.fontScale)
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, densityStruct),
                color = color
            )
        } else {
            if (animatedAlpha > 0f){
                val densityStruct = Density(this.density, this.fontScale)
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, densityStruct),
                    color = color.copy(alpha = color.alpha * animatedAlpha)
                )
            }
            drawContent()
        }
    }
}
package com.garnegsoft.hubs.api.utils

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.Density


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
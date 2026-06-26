package com.garnegsoft.hubs.ui.screens.user

import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.garnegsoft.hubs.api.user.User


@Composable
fun Badge(badge: User.WhoIs.Badge) {
    val badgeColor = MaterialTheme.colors.secondaryVariant
    var showDescriptionPopup by remember { mutableStateOf(false) }
    val popupTransition = updateTransition(targetState = showDescriptionPopup)
    var popupPointerPosition by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable {
                showDescriptionPopup = true
            }
            .border(
                1.dp,
                badgeColor,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = badgeColor,
            text = badge.title
        )
        val popupAlphaAnimated by popupTransition.animateFloat(
            transitionSpec = { tween(durationMillis = 100) }
        ) {
            if (it) 1f else 0f
        }

        val popupOffsetAnimated by popupTransition.animateDp {
            if (it) 0.dp else 16.dp
        }

        val popupScaleAnimated by popupTransition.animateFloat(
            transitionSpec = { tween(durationMillis = 150) }
        ) {
            if (it) 1f else 0.8f
        }

        val density = LocalDensity.current
        if (popupTransition.currentState || showDescriptionPopup) {
            Popup(
                onDismissRequest = {
                    showDescriptionPopup = false
                },
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        popupPointerPosition = anchorBounds.width / 2
                        Log.i("popupPointer", "anchorBounds: topCenter:${anchorBounds.topCenter}; topLeft:${anchorBounds.topLeft}")
                        return IntOffset(anchorBounds.left - (2 * density.density).toInt(), anchorBounds.top - popupContentSize.height + (6 * density.density).toInt())
                    }
                }
            ) {
                val surfaceColor = MaterialTheme.colors.surface

                Row(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = popupScaleAnimated
                            scaleY = popupScaleAnimated
                            alpha = popupAlphaAnimated
                            translationY = popupOffsetAnimated.toPx()

                        }
                        .padding(2.dp)
                        .padding(bottom = 20.dp)

                        .drawWithCache {
                            val path = Path().apply {
                                // TODO: Pointer sometimes is off center because popup sometimes clips to window size, should be noticed
                                moveTo(popupPointerPosition.toFloat() - 10f * density.density, size.height - 2f)
                                relativeLineTo(20f * density.density, 0f)
                                relativeLineTo(-10f * density.density, 10f * density.density)
                                this.close()
                            }
                            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                this.setShadowLayer(10f, 0f, -2f, android.graphics.Color.argb(0.5f, 0f, 0f, 0f))
                                
                            }
                            onDrawWithContent {
                                drawIntoCanvas {
                                    it.nativeCanvas.drawPath(path.asAndroidPath(), shadowPaint)
                                }
                                drawContent()
                                drawPath(path, color = surfaceColor)



                            }
                        }
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colors.surface)
                        .padding(12.dp)
                        .widthIn(max = 200.dp)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onSurface.copy(0.5f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = badge.description,
                        color = MaterialTheme.colors.onSurface.copy(0.5f)
                    )
                }
            }
        }
    }
}
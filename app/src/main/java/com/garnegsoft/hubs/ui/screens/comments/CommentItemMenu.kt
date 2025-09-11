package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.common.BaseMenuContainer
import com.garnegsoft.hubs.ui.common.MenuItem
import kotlin.math.roundToInt


@Composable
fun CommentItemMenu(
    show: Boolean,
    onCollapseCommentClick: () -> Unit,
    onCollapseThreadClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val transition = updateTransition(show)
    val density = LocalDensity.current

    val alpha by transition.animateFloat { if (it) 1f else 0f }
    val offset by transition.animateIntOffset {
        if (it) IntOffset.Zero
        else IntOffset(0, (-6 * density.density).roundToInt())
    }


    if (show || transition.currentState) {
        Popup(
            properties = PopupProperties(focusable = true),
            onDismissRequest = onDismiss
        ) {
            BaseMenuContainer(
                modifier = Modifier
					.offset {
						offset
					}
					.graphicsLayer {
						this.alpha = alpha
					}
            ) {
                MenuItem(
                    title = "Свернуть комментарий",
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.collapse),
                            contentDescription = "Свернуть комментарий"
                        )
                    },
                    onClick = onCollapseCommentClick
                )
                MenuItem(
                    title = "Свернуть всю ветку",
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.collapse_double),
                            contentDescription = "Свернуть всю ветку"
                        )
                    },
                    onClick = onCollapseThreadClick
                )
            }
        }
    }
}
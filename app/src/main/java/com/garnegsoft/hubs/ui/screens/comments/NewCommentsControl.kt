package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun NewCommentsControl(
    modifier: Modifier = Modifier,
    newCommentsCount: Int,
    currentCommentNumber: Int,
    nextCommentButtonClick: () -> Unit,
    previousCommentButtonClick: () -> Unit,
    currentCommentButtonClick: () -> Unit,
    onGoToNewCommentsLabelClick: () -> Unit,
    showGoToNewCommentsLabel: Boolean,
) {
    Box(
        modifier = modifier
			.shadow(5.dp, shape = CircleShape)
			.clip(CircleShape)
			.border(1.dp, MaterialTheme.colors.onSurface.copy(0.1f), CircleShape)

			.background(MaterialTheme.colors.surface),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = showGoToNewCommentsLabel,
            contentAlignment = Alignment.Center
        ) {
            if (it) {
                Row(
                    modifier = modifier
						.clip(CircleShape)
						.clickable(onClick = onGoToNewCommentsLabelClick)
						.matchParentSize()
//						.background(MaterialTheme.colors.surface)
//						.border(1.dp, MaterialTheme.colors.onSurface.copy(0.1f), CircleShape)
						.heightIn(48.dp)
						.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "К новым комментариям")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
            } else {
                Row(
                    modifier = Modifier
						.heightIn(max = 48.dp)
						.clickable(
							onClick = currentCommentButtonClick,
							indication = null,
							interactionSource = null
						),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = previousCommentButtonClick,
                    ) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }
                    Row(
                        modifier = Modifier
							.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        AnimatedContent(targetState = currentCommentNumber,
                            transitionSpec = {
                                if (initialState < targetState) {
                                    (slideInVertically(tween(delayMillis = 100)) { -it / 2 } + fadeIn(
                                        tween(delayMillis = 100)
                                    ))
                                        .togetherWith(slideOutVertically { it / 2 } + fadeOut())
                                } else {
                                    (slideInVertically(tween(delayMillis = 100)) { it / 2 } + fadeIn(
                                        tween(delayMillis = 100)
                                    ))
                                        .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                                }
                            }) {
                            Text(text = "$it")
                        }
                        Text(text = "/$newCommentsCount")
                    }
                    IconButton(
                        onClick = nextCommentButtonClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewCommentsControl(
    modifier: Modifier = Modifier,
    state: CommentsScreenNavigationState.NewCommentsNavigationControlState
) {
    val coroutineScope = rememberCoroutineScope()
    NewCommentsControl(
        modifier = modifier,
        newCommentsCount = state.newCommentsAmount,
        currentCommentNumber = state.currentCommentNumber,
        nextCommentButtonClick = { coroutineScope.launch { state.scrollToNextComment() } },
        previousCommentButtonClick = { coroutineScope.launch { state.scrollToPreviousComment() } },
        currentCommentButtonClick = { coroutineScope.launch { state.scrollToCurrentComment() } },
        onGoToNewCommentsLabelClick = { coroutineScope.launch { state.scrollToFirst() } },
        showGoToNewCommentsLabel = state.showGoToNewCommentsLabel
    )
}
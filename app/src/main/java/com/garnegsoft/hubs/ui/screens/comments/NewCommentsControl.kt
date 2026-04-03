package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
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
                                    (slideInVertically(tween(delayMillis = 100, easing = EaseOutQuad)) { -it / 2 } + fadeIn(
                                        tween(delayMillis = 100)
                                    ))
                                        .togetherWith(slideOutVertically { it / 2 } + fadeOut())
                                } else {
                                    (slideInVertically(tween(delayMillis = 100, easing = EaseOutQuad)) { it / 2 } + fadeIn(
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
        showGoToNewCommentsLabel = state.showGoToNewCommentsButton
    )
}
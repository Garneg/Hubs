package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*


@Composable
fun NewCommentsControl(
	modifier: Modifier = Modifier,
	newCommentsCount: Int,
	currentCommentNumber: Int,
	onUpButtonClick: () -> Unit,
	onDownButtonClick: () -> Unit,
	onGoToNewCommentsLabelClick: () -> Unit,
	showGoToNewCommentsLabel: Boolean,
) {
	Box(
		modifier = modifier
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
					modifier = Modifier.heightIn(max = 42.dp),
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically
				) {
					IconButton(onClick = onUpButtonClick) {
						Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
					}
					AnimatedContent(targetState = currentCommentNumber,
						transitionSpec = {
							if (initialState < targetState) {
								(slideInVertically { -it / 2 } + fadeIn())
									.togetherWith(slideOutVertically { it / 2 } + fadeOut())
							} else {
								(slideInVertically { it / 2 } + fadeIn())
									.togetherWith(slideOutVertically { -it / 2 } + fadeOut())
							}
						}) {
						Text(text = "$it")
					}
					Text(text = "/$newCommentsCount")
					IconButton(onClick = onDownButtonClick) {
						Icon(
							imageVector = Icons.Default.KeyboardArrowDown,
							contentDescription = null
						)
					}
				}
			} else {
				Row(
					modifier = modifier
						.clip(CircleShape)
						.clickable(onClick = onGoToNewCommentsLabelClick)
						.matchParentSize()
//						.background(MaterialTheme.colors.surface)
//						.border(1.dp, MaterialTheme.colors.onSurface.copy(0.1f), CircleShape)
						.heightIn(42.dp)
						.padding(horizontal = 16.dp),
					horizontalArrangement = Arrangement.Center,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(text = "К новым комментариям")
					Spacer(modifier = Modifier.width(4.dp))
					Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
				}
			}
		}
	}
}
package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R


@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun BlockUserButton(
	blocked: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val backgroundColor by animateColorAsState(targetValue = if (blocked) MaterialTheme.colors.onSurface else Color.Transparent)
	val contentColor by animateColorAsState(targetValue = if (blocked) MaterialTheme.colors.surface else MaterialTheme.colors.onSurface)
	Box(modifier = Modifier
		.padding(8.dp)
		.height(45.dp)
		.fillMaxWidth()
		.clip(RoundedCornerShape(10.dp))
		.background(backgroundColor)
		.border(
			width = 1.dp,
			shape = RoundedCornerShape(10.dp),
			color = if (blocked) Color.Transparent else MaterialTheme.colors.onSurface
		)
		.clickable(onClick = onClick)
	) {
		Row(
			modifier = Modifier.align(Alignment.Center),
			verticalAlignment = Alignment.CenterVertically
		) {
			val animatedIcon = AnimatedImageVector.animatedVectorResource(id = R.drawable.add_block_animated)
			
				Icon(
					modifier = Modifier.size(24.dp),
					painter = rememberAnimatedVectorPainter(animatedImageVector = animatedIcon, atEnd = !blocked),
					tint = contentColor,
					contentDescription = null
				)
			
			Spacer(modifier = Modifier.width(4.dp))
			
			Text(
				modifier = Modifier.animateContentSize(),
				text = if (blocked) "Показывать публикации" else "Скрыть публикации",
				fontWeight = FontWeight.W500,
				color = contentColor,
				textAlign = TextAlign.Center
			)
		}
		
	}
}
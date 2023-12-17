package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun DeletedCommentItem(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(26.dp))
			.background(MaterialTheme.colors.surface)
			.padding(horizontal = 16.dp, vertical = 48.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "НЛО прилетело и опубликовало эту надпись здесь",
			textAlign = TextAlign.Center,
			color = MaterialTheme.colors.onSurface.copy(0.5f))
	}
}
package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun ArticleAuthorElement(
	onClick: () -> Unit,
	userAvatarUrl: String,
	fullName: String?,
	alias: String,
) {
	Row(
		modifier = Modifier
			.height(IntrinsicSize.Min)
			.fillMaxWidth()
			.clip(RoundedCornerShape(20.dp))
			.clickable(onClick = onClick)
			.padding(8.dp)
	) {
		AsyncImage(
			modifier = Modifier
				.size(58.dp)
				.clip(RoundedCornerShape(12.dp)),
			model = userAvatarUrl, contentDescription = null)
		Spacer(modifier = Modifier.width(12.dp))
		 Column(
			 modifier = Modifier.fillMaxHeight(),
			 verticalArrangement = Arrangement.Center) {
			 Text(
				 text = fullName ?: "@$alias",
				 color = MaterialTheme.colors.onBackground,
				 style = MaterialTheme.typography.subtitle1
			 )
			 fullName?.let {
				 Text(text = "@$alias", color = MaterialTheme.colors.onSurface.copy(0.5f))
			 }
//			 Spacer(modifier = Modifier.weight(1f))
//			OutlinedButton(
//				modifier = Modifier.fillMaxWidth(),
//				onClick = { /*TODO*/ }) {
//				Text(text = "Подписаться")
//			}
		 }
	}
}
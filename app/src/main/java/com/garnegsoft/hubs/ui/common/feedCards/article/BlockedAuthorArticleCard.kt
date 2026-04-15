package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip


@Composable
fun BlockedAuthorArticleCard(
	cardData: ArticleCardData,
	articleCardConfiguration: ArticleCardConfiguration,
	onAuthorClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Text(
		modifier = modifier
			.clip(articleCardConfiguration.cardShape)
			.clickable(onClick = onAuthorClick)
			.padding(articleCardConfiguration.innerPadding),
		text = "Вы скрыли публикации @${cardData.author?.alias} из ленты",
		color = MaterialTheme.colors.onBackground.copy(0.5f)
	)
}
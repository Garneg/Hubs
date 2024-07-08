package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.article.Article
import com.garnegsoft.hubs.data.article.list.ArticleSnippet


@Composable
fun BlockedAuthorArticleCard(
	article: ArticleSnippet,
	articleCardStyle: ArticleCardStyle,
	onAuthorClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Text(
		modifier = modifier
			.clip(articleCardStyle.cardShape)
			.clickable(onClick = onAuthorClick)
			.padding(articleCardStyle.innerPadding),
		text = "Вы скрыли публикации @${article.author?.alias} из ленты",
		color = MaterialTheme.colors.onBackground.copy(0.5f)
	)
}
package com.garnegsoft.hubs.ui.common.feedCards.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.article.list.Post
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.RenderHtml
import com.garnegsoft.hubs.ui.screens.article.parseElement


@Composable
fun PostCard(
	post: Post,
	style: ArticleCardStyle
) {
	Column(modifier = Modifier
		.clip(style.cardShape)
		.background(style.backgroundColor)
		.padding(style.innerPadding)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			AsyncImage(modifier = Modifier
				.size(style.authorAvatarSize)
				.clip(style.innerElementsShape),
				model = post.author.avatarUrl, contentDescription = null)
			Spacer(modifier = Modifier.width(4.dp))
			Text(text = post.author.alias, style = style.authorTextStyle)
		}
		Spacer(modifier = Modifier.height(style.innerPadding))
		RenderHtml(html = post.contentHtml, elementSettings = remember { ElementSettings(16.sp, 18.sp ,false)})
		
	}
}
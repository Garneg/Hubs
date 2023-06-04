package com.garnegsoft.hubs.ui.screens.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.ui.common.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle

@Composable
fun OfflineArticleCard(
    article: OfflineArticleSnippet,
    onClick: () -> Unit,
    style: ArticleCardStyle = defaultArticleCardStyle()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(style.cardShape)
            .background(style.backgroundColor)
            .clickable(onClick = onClick)
            .padding(style.innerPadding)
    ) {
        article.authorName?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    modifier = Modifier
                        .size(style.authorAvatarSize)
                        .clip(style.innerElementsShape),
                    model = article.authorAvatarUrl, contentDescription = ""
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = it, style = style.authorTextStyle)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(text = article.title, style = style.titleTextStyle)
        if (article.thumbnailUrl != null) {
            AsyncImage(
                model = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(style.innerElementsShape)
                    .background(
                        if (MaterialTheme.colors.isLight)
                            Color.Companion.Transparent
                        else
                            MaterialTheme.colors.onSurface.copy(0.2f)
                    ),
                contentDescription = ""
            )
        }

    }
}
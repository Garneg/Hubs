package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.article.Article


@Composable
fun TranslationMessage(
    modifier: Modifier = Modifier,
    translationInfo: Article.TranslationData,
    onOriginClick: () -> Unit
) {
    if (translationInfo.isTranslation){
        Box(modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.onBackground.copy(0.05f))
            .clickable(
                enabled = translationInfo.originUrl != null,
                onClick = onOriginClick
            )
            .padding(12.dp)) {
            when{
                translationInfo.originalAuthorName != null -> {
                    Text(
                        text = "Автор оригинала: ${translationInfo.originalAuthorName}",
                        color = MaterialTheme.colors.onBackground.copy(0.5f)
                    )
                }
                else -> {
                    Text(
                        text = "Статья является переводом"
                    )
                }
            }
        }
    }
}
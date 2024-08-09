package com.garnegsoft.hubs.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.history.HistoryArticle
import com.garnegsoft.hubs.api.history.HistoryEntity
import com.garnegsoft.hubs.api.utils.formatTimestamp
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.theme.HubsTheme


@Composable
fun ArticleHistoryCard(
	entity: HistoryEntity,
	articleData: HistoryArticle,
	onClick: () -> Unit,
	style: ArticleCardStyle
) {
	
		Column() {
//			Row(
//				modifier = Modifier
//					.padding(horizontal = 12.dp)
//					.fillMaxWidth(),
//			) {
//				Text(text = "Статья", color = MaterialTheme.colors.onBackground.copy(0.4f),
//					fontWeight = FontWeight.W500,)
//				Spacer(modifier = Modifier.weight(1f))
//				Text(
//					text = remember { formatTimestamp(entity.timestamp) }, color = MaterialTheme.colors.onBackground.copy(0.4f),
//					fontWeight = FontWeight.W500,
//					textAlign = TextAlign.End)
//			}
//
//			Spacer(modifier = Modifier.height(8.dp))
			Row(
				modifier = Modifier
					.clip(style.cardShape)
					.clickable(onClick = onClick)
					.background(style.backgroundColor)
					.padding(style.innerPadding)
			) {
				Column(modifier = Modifier.weight(1f)) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						AsyncImage(
							modifier = Modifier
								.size(style.authorAvatarSize)
								.clip(style.innerElementsShape),
							model = articleData.authorAvatarUrl,
							contentDescription = null)
						Spacer(modifier = Modifier.width(6.dp))
						Text(
							text = articleData.authorAlias,
							fontWeight = FontWeight.W500,
							color = MaterialTheme.colors.onSurface.copy(0.5f)
						)
					}
					Spacer(modifier = Modifier.height(2.dp))
					Text(
						text = articleData.title,
						fontSize = 18.sp,
						fontWeight = FontWeight.W500
					)
				}
				articleData.thumbnailUrl?.let {
					Spacer(modifier = Modifier.width(style.innerPadding.div(2)))
					AsyncImage(
						modifier = Modifier
							.size(80.dp)
							.clip(style.innerElementsShape),
						model = articleData.thumbnailUrl,
						contentDescription = null,
						contentScale = ContentScale.Crop
					)
				}
				
			}
		}
	
}
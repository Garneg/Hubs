package com.garnegsoft.hubs.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.garnegsoft.hubs.ui.theme.HubsTheme


@Composable
fun ArticleHistoryCard(
	entity: HistoryEntity,
	articleData: HistoryArticle
) {
	HubsTheme {
		Column() {
			Row(
				modifier = Modifier
					.padding(horizontal = 12.dp)
					.fillMaxWidth(),
			) {
				Text(text = "Статья", color = MaterialTheme.colors.onBackground.copy(0.4f),
					fontWeight = FontWeight.W500,)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = remember { formatTimestamp(entity.timestamp) }, color = MaterialTheme.colors.onBackground.copy(0.4f),
					fontWeight = FontWeight.W500,
					textAlign = TextAlign.End)
			}
			
			Spacer(modifier = Modifier.height(8.dp))
			Row(
				modifier = Modifier
					.clip(RoundedCornerShape(26.dp))
					.background(MaterialTheme.colors.surface)
					.padding(14.dp)
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = articleData.authorAlias,
						fontWeight = FontWeight.W500,
						color = MaterialTheme.colors.onSurface.copy(0.5f)
					)
					Text(
						text = articleData.title,
						fontSize = 18.sp,
						fontWeight = FontWeight.W500
					)
				}
				AsyncImage(
					modifier = Modifier
						.size(80.dp)
						.clip(RoundedCornerShape(14.dp)),
					model = articleData.thumbnailUrl,
					contentDescription = null,
					contentScale = ContentScale.Crop
				)
			}
		}
	}
}
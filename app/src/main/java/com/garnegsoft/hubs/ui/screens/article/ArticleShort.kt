package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.data.article.list.ArticleSnippet
import com.garnegsoft.hubs.data.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor

@Composable
fun ArticleShort(
	article: ArticleSnippet,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier.clip(RoundedCornerShape(24.dp))
			//.background(MaterialTheme.colors.background)
			.clickable(onClick = onClick)
			//.padding(bottom = 8.dp)
			.padding(12.dp)
			.height(IntrinsicSize.Min)
	) {
		Column(
			modifier = Modifier
				.weight(1f)
				
		) {
			Text(
				//modifier = Modifier.weight(1f),
				text = article.title,
				color = MaterialTheme.colors.onSurface,
				fontSize = 18.sp,
				fontWeight = FontWeight.W600,
				maxLines = 4
			)
			Spacer(modifier = Modifier.height(8.dp))
			Row(
				modifier = Modifier.padding(4.dp),
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						painter = painterResource(id = R.drawable.rating),
						contentDescription = null,
						tint = MaterialTheme.colors.onSurface,
						modifier = Modifier.size(18.dp),
					)
					Spacer(modifier = Modifier.width(2.dp))
					if (article.statistics.score > 0) {
						Text(
							text = '+' + article.statistics.score.toString(),
							color = RatingPositiveColor,
						)
					} else {
						if (article.statistics.score < 0) {
							Text(
								text = article.statistics.score.toString(),
								color = RatingNegativeColor
							)
						} else {
							Text(
								text = article.statistics.score.toString(),
								color = MaterialTheme.colors.onSurface
							)
						}
					}
				}
				
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						painter = painterResource(id = R.drawable.views_icon),
						contentDescription = null,
						tint = MaterialTheme.colors.onSurface,
						modifier = Modifier.size(18.dp),
					)
					Spacer(modifier = Modifier.width(3.dp))
					Text(
						text = formatLongNumbers(article.statistics.readingCount),
						color = MaterialTheme.colors.onSurface
					)
				}
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						painter = painterResource(id = R.drawable.comments_icon),
						contentDescription = null,
						tint = MaterialTheme.colors.onSurface,
						modifier = Modifier.size(18.dp),
					)
					Spacer(modifier = Modifier.width(3.dp))
					Text(
						text = formatLongNumbers(article.statistics.commentsCount),
						overflow = TextOverflow.Clip,
						maxLines = 1,
						color = MaterialTheme.colors.onSurface
					)
				}
				
			}
		}
		
		}
}
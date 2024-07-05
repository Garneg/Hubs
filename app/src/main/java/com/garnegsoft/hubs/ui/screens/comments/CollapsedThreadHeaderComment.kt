package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.data.comment.Comment
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.RenderHtml


@Composable
fun CollapsedThreadHeaderComment(
	onExpandClick: () -> Unit,
	comment: Comment,
	modifier: Modifier = Modifier,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(26.dp))
			.background(MaterialTheme.colors.surface)
			.padding(16.dp)
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.weight(1f)
					.clip(RoundedCornerShape(10.dp))
					//.clickable(onClick = onAuthorClick)
					//.background(commentFlagColor)
//					.border(
//						width = 1.5.dp,
//						color = if (highlight) commentFlagColor.copy(0.5f) else Color.Unspecified,
//						shape = RoundedCornerShape(10.dp)
//					)
			) {
				AsyncImage(
					modifier = Modifier
						.size(34.dp)
						.clip(RoundedCornerShape(10.dp)),
					model = comment.author.avatarUrl, contentDescription = "authorAvatar"
				)
				
				Spacer(modifier = Modifier.width(4.dp))
				Column {
					Text(text = comment.author.alias)
					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(text = comment.publishedTime, fontSize = 12.sp, color = Color.Gray)
						if (comment.edited)
							Text(text = " (изм.)", fontSize = 12.sp, color = Color.Gray)
					}
				}
				
			}
//			if (menu != null && onMenuButtonClick != null) {
//				Spacer(modifier = Modifier.width(4.dp))
//				Box {
//					Box(
//						modifier = Modifier
//							.size(34.dp)
//							.clip(CircleShape)
//							.clickable(onClick = onMenuButtonClick),
//						contentAlignment = Alignment.Center
//					) {
//						Icon(
//							imageVector = Icons.Default.MoreVert,
//							contentDescription = "Меню комментария"
//						)
//					}
//					menu()
//				}
//			}
		}
		Spacer(modifier = Modifier.height(4.dp))
		val gradientColor = MaterialTheme.colors.surface
		Box(
			modifier = Modifier.drawWithContent {
				drawContent()
				drawRect(Brush.linearGradient(0f to Color.Transparent, 1f to gradientColor, start = Offset.Zero, end = Offset(0f, size.height)))
			}
		) {
			Box(
				modifier = Modifier
					.heightIn(max = 100.dp)
					
					.verticalScroll(rememberScrollState(), false)
			) {
				RenderHtml(
					html = comment.message,
					elementSettings = ElementSettings(16.sp, 16.sp, false)
				)
			}
		}
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		){
			TextButton(
				onClick = onExpandClick,
				colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.secondary)
			) {
				Text(text = "Раскрыть ветку")
				Spacer(modifier = Modifier.width(2.dp))
				Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
			}
			
		}
	}
}
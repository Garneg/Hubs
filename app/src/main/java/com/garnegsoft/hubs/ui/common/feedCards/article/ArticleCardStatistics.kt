package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.HubSubscribedColor
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleStats(
	statistics: Article.Statistics,
	addedToBookmarks: Boolean,
	bookmarksCount: Int,
	onAddToBookmarksClicked: () -> Unit,
	onCommentsClick: () -> Unit,
	unreadCommentsCount: Int,
	bookmarksButtonEnabled: Boolean,
	onShowSavingPopup: () -> Unit,
	saveArticlePopup: @Composable (bounds: IntSize) -> Unit,
	style: ArticleCardStyle,
	ratingIconPainter: Painter = painterResource(id = R.drawable.rating),
	viewsIconPainter: Painter = painterResource(id = R.drawable.views_icon),
	bookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark),
	filledBookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark_filled),
	commentIconPainter: Painter = painterResource(id = R.drawable.comments_icon),
) {
	//Stats
	Row(
		modifier = Modifier
			.padding(horizontal = style.innerPadding)
			.padding(top = style.innerPadding / 2f, bottom = style.innerPadding),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(14.dp)
	) {
		
		//Rating
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		) {
			Icon(
				painter = ratingIconPainter,
				contentDescription = null,
				modifier = Modifier.size(18.dp),
				tint = style.statisticsColor
			)
			Spacer(modifier = Modifier.width(2.dp))
			if (statistics.score > 0) {
				Text(
					text = '+' + statistics.score.toString(),
					style = style.statisticsTextStyle,
					color = RatingPositiveColor
				)
			} else
				if (statistics.score < 0) {
					Text(
						text = statistics.score.toString(),
						style = style.statisticsTextStyle,
						color = RatingNegativeColor
					)
				} else {
					Text(
						text = statistics.score.toString(),
						style = style.statisticsTextStyle
					)
				}
		}
		
		//Views
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		
		) {
			Icon(
				painter = viewsIconPainter,
				contentDescription = null,
				modifier = Modifier.size(18.dp),
				tint = style.statisticsColor
			)
			Spacer(modifier = Modifier.width(2.dp))
			Text(
				text = formatLongNumbers(statistics.readingCount),
				style = style.statisticsTextStyle
			)
		}
		
		var bounds by remember {
			mutableStateOf(IntSize.Zero)
		}
		
		
		
		//Added to bookmarks
//		Row(
//			verticalAlignment = Alignment.CenterVertically,
//			modifier = Modifier
//				.padding(vertical = style.innerPadding)
//				.fillMaxHeight()
//				.combinedClickable(
//					onClick = onAddToBookmarksClicked,
//					onLongClick = {
//						onShowSavingPopup()
//
//					},
//					enabled = style.bookmarksButtonAllowedBeEnabled && bookmarksButtonEnabled,
//				)
//				.onGloballyPositioned {
//					bounds = it.size
//				},
//			horizontalArrangement = Arrangement.Center
//		) {
//			Icon(
//
//				painter =
//					if (addedToBookmarks)
//						filledBookmarkIconPainter
//					else
//						bookmarkIconPainter,
//				contentDescription = null,
//				modifier = Modifier.size(18.dp),
//				tint = style.statisticsColor
//			)
//			Spacer(modifier = Modifier.width(4.dp))
//			Text(
//				text = bookmarksCount.toString(),
//				style = style.statisticsTextStyle
//			)
//			saveArticlePopup(bounds)
//		}
		
		val commentsInteractionSource = remember { MutableInteractionSource() }
		//Comments
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier
				.clickable(
					interactionSource = commentsInteractionSource,
					indication = null,
					enabled = style.commentsButtonEnabled,
					onClick = onCommentsClick
				)
				.fillMaxHeight()
				.clickable(
					enabled = style.commentsButtonEnabled,
					onClick = onCommentsClick
				)
		) {
				Row(
					modifier = Modifier.padding(horizontal = 0.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						painter = commentIconPainter,
						contentDescription = null,
						modifier = Modifier.size(17.dp),
						tint = style.statisticsColor
					)
					Spacer(modifier = Modifier.width(4.dp))
					Text(
						text = formatLongNumbers(statistics.commentsCount),
						style = style.statisticsTextStyle,
						overflow = TextOverflow.Clip,
						maxLines = 1
					)
					if (unreadCommentsCount > 0) {
						Spacer(modifier = Modifier.width(2.dp))
						Text(
							text = "+" + formatLongNumbers(unreadCommentsCount),
							style = style.statisticsTextStyle.copy(color = HubSubscribedColor),
							overflow = TextOverflow.Clip,
							maxLines = 1
						)
					}
				}
		}
	}
}
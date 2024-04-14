package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.data.article.offline.OfflineArticlesDatabase


@Composable
fun SaveArticlePopup(
	show: Boolean,
	bounds: IntSize,
	cardStyle: ArticleCardStyle,
	onSaveClick: () -> Unit,
	onDeleteClick: () -> Unit,
	onDismissRequest: () -> Unit,
	articleId: Int
) {
	val density = LocalDensity.current.density
	val context = LocalContext.current
	
	val positionProvider = object : PopupPositionProvider {
		override fun calculatePosition(
			anchorBounds: IntRect,
			windowSize: IntSize,
			layoutDirection: LayoutDirection,
			popupContentSize: IntSize
		): IntOffset {
			return IntOffset(anchorBounds.left, anchorBounds.top - popupContentSize.height)
		}
		
	}
	val transition = updateTransition(targetState = show)
	
	val animatedSize by transition.animateFloat {
		if (it) 1f else 0.8f
	}
	
	if (show || transition.currentState) {
		val isDownloaded by OfflineArticlesDatabase.getDb(context = context).articlesDao().existsFlow(articleId).collectAsState(
			initial = null
		)
		Popup(popupPositionProvider = positionProvider,
			properties = PopupProperties(true),
			onDismissRequest = onDismissRequest
		) {
			Box(
				modifier = Modifier
					.graphicsLayer(
						scaleX = animatedSize,
						scaleY = animatedSize,
						alpha = animatedSize
					)
					.size((bounds.width / density).dp, (bounds.height / density).dp)
					.shadow(2.dp, shape = CircleShape)
					.clip(CircleShape)
					.background(cardStyle.backgroundColor)
					.border(
						width = 0.5.dp,
						color = MaterialTheme.colors.onSurface.copy(0.1f),
						shape = CircleShape
					)
					.clickable(onClick = if (isDownloaded == false) onSaveClick else onDeleteClick),
				contentAlignment = Alignment.Center
			) {
				isDownloaded?.let {
					if (it) {
						Icon(
							modifier = Modifier.size(18.dp),
							imageVector = Icons.Outlined.Delete,
							contentDescription = null,
							tint = cardStyle.statisticsColor)
					}
					else {
						Icon(
							modifier = Modifier.size(18.dp),
							painter = painterResource(id = R.drawable.download),
							contentDescription = null,
							tint = cardStyle.statisticsColor)
					}
				}
				
			}
		}
	}
}


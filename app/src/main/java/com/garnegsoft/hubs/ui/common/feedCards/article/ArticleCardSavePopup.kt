package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesDatabase
import com.garnegsoft.hubs.ui.navigation.Transitions.transitionDuration
import kotlin.math.roundToInt


@Composable
fun SaveArticlePopup(
    show: Boolean,
    bounds: IntSize,
    cardStyle: ArticleCardConfiguration,
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
			return IntOffset(anchorBounds.left - (2 * density).toInt(), anchorBounds.top - popupContentSize.height)
		}
		
	}
	val transition = updateTransition(targetState = show)

	val animatedOffset by transition.animateIntOffset {
		if (it) IntOffset.Zero
		else IntOffset(0, (16f * density).roundToInt())
	}
	val animatedSize by transition.animateFloat(
		transitionSpec = { tween(150) },
	) {
		if (it) 1f else 0.8f
	}

	val animatedAlpha by transition.animateFloat(
		transitionSpec = { tween(durationMillis = 100) }
	) {
		if (it) 1f else 0f
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
					.offset { animatedOffset.copy() }
					.graphicsLayer {
						scaleX = animatedSize
						scaleY = animatedSize
						shape = cardStyle.innerElementsShape
						alpha = animatedAlpha
					}
					.padding(2.dp)
					.size((bounds.width / density).dp, (bounds.height / density).dp)
					.shadow(1.dp, shape = cardStyle.innerElementsShape)
					.clip(cardStyle.innerElementsShape)
					.background(cardStyle.backgroundColor)
					.border(
						width = 0.5.dp,
						color = MaterialTheme.colors.onSurface.copy(0.1f),
						shape = cardStyle.innerElementsShape
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


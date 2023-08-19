package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.garnegsoft.hubs.api.article.Article


@Composable
fun VotesCountIndicator(
	show: Boolean,
	stats: Article.Statistics,
	color: Color,
	onDismiss: () -> Unit,
) {
	val positionProvider = object : PopupPositionProvider {
		override fun calculatePosition(
			anchorBounds: IntRect,
			windowSize: IntSize,
			layoutDirection: LayoutDirection,
			popupContentSize: IntSize
		): IntOffset {
			return IntOffset(
				anchorBounds.left,
				anchorBounds.top - popupContentSize.height - 10
			)
		}
		
	}
	val transition = updateTransition(targetState = show)
	val offset by transition.animateFloat{
		if (it) 0f else 8f
	}
	
	val alpha by transition.animateFloat{
		if (it) 1f else 0.0f
	}
	
	if (show || transition.currentState) {
		Popup(
			popupPositionProvider = positionProvider,
			onDismissRequest = onDismiss
		) {
			Box(
				modifier = Modifier
					.offset(0.dp, offset.dp)
					.alpha(alpha)
					.padding(2.dp)
					.shadow(1.5.dp, shape = RoundedCornerShape(8.dp))
					.clip(RoundedCornerShape(8.dp))
					.border(width = 0.5.dp, color = MaterialTheme.colors.onSurface.copy(0.1f), shape = RoundedCornerShape(8.dp))
					.background(MaterialTheme.colors.surface)
					.padding(8.dp)
			) {
				Text(
					text = "Всего голосов " +
						"${stats.votesCountMinus + stats.votesCountPlus}: " +
						"￪${stats.votesCountPlus} и " +
						"￬${stats.votesCountMinus}",
					color = color
				)
			}
			
		}
		
	}
}
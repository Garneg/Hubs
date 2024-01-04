package com.garnegsoft.hubs.ui.screens.offline

import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.ui.common.MenuBaseContainer
import com.garnegsoft.hubs.ui.common.MenuItem


@Composable
fun OfflineArticleScreenMenu(
	show: Boolean,
	onDismiss: () -> Unit,
	onDeleteClick: () -> Unit,
	onSwitchToNormalMode: () -> Unit,
) {
	val showTransition = updateTransition(targetState = show)
	if (showTransition.targetState || showTransition.currentState) {
		Popup(
			popupPositionProvider = object : PopupPositionProvider {
				override fun calculatePosition(
					anchorBounds: IntRect,
					windowSize: IntSize,
					layoutDirection: LayoutDirection,
					popupContentSize: IntSize
				): IntOffset {
					val shit = anchorBounds.right
					return IntOffset(shit - popupContentSize.width, anchorBounds.bottom)
				}
				
			},
			properties = PopupProperties(true),
			onDismissRequest = onDismiss
		) {
			
			MenuBaseContainer {
				MenuItem(
					title = "Перейти в обычный режим",
					icon = {
						Icon(
							imageVector = Icons.Outlined.ExitToApp,
							contentDescription = null
						)
					},
					onClick = onSwitchToNormalMode
				)
				
				MenuItem(
					title = "Удалить",
					icon = {
						Icon(
							imageVector = Icons.Outlined.Delete,
							contentDescription = null
						)
					},
					onClick = onDeleteClick
				)
			}
		}
	}
}

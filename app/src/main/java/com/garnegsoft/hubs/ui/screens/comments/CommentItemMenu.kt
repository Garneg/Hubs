package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.common.BaseMenuContainer
import com.garnegsoft.hubs.ui.common.MenuItem


@Composable
fun CommentItemMenu(
	onCollapseCommentClick: () -> Unit,
	onCollapseThreadClick: () -> Unit,
	onDismiss: () -> Unit
) {
	Popup(
		properties = PopupProperties(focusable = true),
		onDismissRequest = onDismiss
	) {
		BaseMenuContainer {
			MenuItem(
				title = "Свернуть комментарий",
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.collapse),
						contentDescription = "Свернуть комментарий"
					)
				},
				onClick = onCollapseCommentClick
			)
			MenuItem(
				title = "Свернуть всю ветку",
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.collapse_double),
						contentDescription = "Свернуть всю ветку"
					)
				},
				onClick = onCollapseThreadClick
			)
		}
	}
}
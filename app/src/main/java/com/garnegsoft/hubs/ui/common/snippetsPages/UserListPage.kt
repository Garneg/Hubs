package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.garnegsoft.hubs.api.user.UsersListModel
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@Composable
fun UsersListPage(
	listModel: UsersListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filter: (@Composable () -> Unit)? = null,
	cardIndicator: @Composable (UserSnippet) -> Unit = {
		Text(
			text = it.rating.toString(),
			fontWeight = FontWeight.W400,
			color = DefaultRatingIndicatorColor
		)
	},
	onUserClick: (alias: String) -> Unit
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = filter
	) {
		UserCard(
			user = it,
			onClick = { onUserClick(it.alias) },
			indicator = { cardIndicator(it) }
		)
	}
}
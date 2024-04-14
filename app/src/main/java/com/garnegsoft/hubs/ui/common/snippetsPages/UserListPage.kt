package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.garnegsoft.hubs.data.user.UsersListModel
import com.garnegsoft.hubs.data.user.list.UserSnippet
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@OptIn(ExperimentalFoundationApi::class)
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
			modifier = Modifier.animateItemPlacement(),
			user = it,
			onClick = { onUserClick(it.alias) },
			indicator = { cardIndicator(it) }
		)
	}
}
package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.user.UsersListModel
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCardDefaultIndicator
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@Composable
fun UsersListPage(
	listModel: UsersListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filter: (@Composable () -> Unit)? = null,
	cardIndicator: @Composable (UserSnippet) -> Unit = { UserCardDefaultIndicator(user = it) },
	onUserClick: (alias: String) -> Unit
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = filter,
		verticalArrangement = Arrangement.spacedBy(1.dp)
	) {
		UserCard(
			user = it,
			onClick = { onUserClick(it.alias) },
			indicator = { cardIndicator(it) }
		)
	}
}
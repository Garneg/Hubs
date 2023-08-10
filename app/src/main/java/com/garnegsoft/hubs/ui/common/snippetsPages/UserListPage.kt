package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.api.user.UsersListModel
import com.garnegsoft.hubs.ui.common.UserCard


@Composable
fun UsersListPage(
	listModel: UsersListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filter: (@Composable () -> Unit)? = null,
	onUserClick: (alias: String) -> Unit
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = filter
	) {
		UserCard(
			user = it,
			onClick = { onUserClick(it.alias) }
		)
	}
}
package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.data.CollapsingContentState
import com.garnegsoft.hubs.data.hub.HubsListModel
import com.garnegsoft.hubs.data.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.hub.HubCard


@Composable
fun HubsListPage(
	listModel: HubsListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	onHubClick: (alias: String) -> Unit
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = collapsingBar,
		doInitialLoading = doInitialLoading,
		collapsingContentState = collapsingContentState
	) {
		HubCard(hub = it, onClick = { onHubClick(it.alias) })
	}
}
package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.hub.HubsListModel
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.hub.HubCard
import com.garnegsoft.hubs.ui.common.feedCards.hub.HubCardDefaultIndicator


@Composable
fun HubsListPage(
	listModel: HubsListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	onHubClick: (alias: String) -> Unit
) {
	val indicatorIconPainter = painterResource(id = R.drawable.profile_rating)
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = collapsingBar,
		doInitialLoading = doInitialLoading,
		collapsingContentState = collapsingContentState,
		verticalArrangement = Arrangement.spacedBy(0.dp)
	) {
		HubCard(hub = it, onClick = { onHubClick(it.alias) }) {
			HubCardDefaultIndicator(hub = it, indicatorIconPainter)
		}
	}
}
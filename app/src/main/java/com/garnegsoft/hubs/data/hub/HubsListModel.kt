package com.garnegsoft.hubs.data.hub

import com.garnegsoft.hubs.data.Filter
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.article.AbstractSnippetListModel
import com.garnegsoft.hubs.data.hub.list.HubSnippet
import com.garnegsoft.hubs.data.hub.list.HubsListController
import kotlinx.coroutines.CoroutineScope

class HubsListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Filter? = null,
) : AbstractSnippetListModel<HubSnippet>(
	path = path,
	coroutineScope = coroutineScope,
	baseArgs = baseArgs.toMap(),
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<HubSnippet>? =
		HubsListController.get(path, args)
	
}
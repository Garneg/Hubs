package com.garnegsoft.hubs.api.hub

import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import kotlinx.coroutines.CoroutineScope

class HubsListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Map<String, String> = emptyMap()
) : AbstractSnippetListModel<HubSnippet>(
	path = path,
	coroutineScope = coroutineScope,
	baseArgs = baseArgs.toMap(),
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<HubSnippet>? =
		HubsListController.get(path, args)
}
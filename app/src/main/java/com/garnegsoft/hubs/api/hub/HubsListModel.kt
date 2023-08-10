package com.garnegsoft.hubs.api.hub

import androidx.lifecycle.ViewModel
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
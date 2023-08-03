package com.garnegsoft.hubs.api.user

import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import kotlinx.coroutines.CoroutineScope

class UsersListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Map<String, String> = emptyMap()
) : AbstractSnippetListModel<UserSnippet>(
	path = path,
	coroutineScope = coroutineScope,
	baseArgs = baseArgs.toMap(),
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<UserSnippet>? =
		UsersListController.get(path, args)
}
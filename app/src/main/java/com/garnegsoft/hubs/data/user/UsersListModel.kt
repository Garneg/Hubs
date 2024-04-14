package com.garnegsoft.hubs.data.user

import com.garnegsoft.hubs.data.Filter
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.article.AbstractSnippetListModel
import com.garnegsoft.hubs.data.user.list.UserSnippet
import com.garnegsoft.hubs.data.user.list.UsersListController
import kotlinx.coroutines.CoroutineScope

class UsersListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Filter? = null,
) : AbstractSnippetListModel<UserSnippet>(
	path = path,
	coroutineScope = coroutineScope,
	baseArgs = baseArgs.toMap(),
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<UserSnippet>? =
		UsersListController.get(path, args)
}
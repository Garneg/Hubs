package com.garnegsoft.hubs.api.article

import ArticlesListController
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.list.Post
import kotlinx.coroutines.CoroutineScope

class PostsListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Filter? = null,
) : AbstractSnippetListModel<Post>(
	path = path,
	baseArgs = baseArgs.toMap(),
	coroutineScope = coroutineScope,
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<Post>? {
		return ArticlesListController.getPosts(path, args)
	}
}
package com.garnegsoft.hubs.api.article

import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import kotlinx.coroutines.CoroutineScope

class ArticlesListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Filter? = null,
) : AbstractSnippetListModel<ArticleSnippet>(
	path = path,
	baseArgs = baseArgs.toMap(),
	coroutineScope = coroutineScope,
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<ArticleSnippet>? =
		ArticlesListController.getArticlesSnippets(path, args)
	
}
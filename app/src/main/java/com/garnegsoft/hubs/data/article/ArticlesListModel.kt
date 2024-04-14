package com.garnegsoft.hubs.data.article

import com.garnegsoft.hubs.data.Filter
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.article.list.ArticleSnippet
import kotlinx.coroutines.CoroutineScope

/**
 * Implementation of AbstractSnippetListModel for articles.
 * @param path permanent url path of snippets
 * @param coroutineScope coroutine scope for all network tasks
 * @param baseArgs requests arguments part, that won't change
 * @param initialFilter initial filter values. Filters contain the other part of requests args, that change.
 * Change filter values by calling editFilter(). This field optional, as well as filter itself
 * @see AbstractSnippetListModel
 */
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

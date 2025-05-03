package com.garnegsoft.hubs.api.article

import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardData
import com.garnegsoft.hubs.ui.common.feedCards.article.toArticleCardData
import kotlinx.coroutines.CoroutineScope


/// ORIGINAL ONE
///**
// * Implementation of AbstractSnippetListModel for articles.
// * @param path permanent url path of snippets
// * @param coroutineScope coroutine scope for all network tasks
// * @param baseArgs requests arguments part, that won't change
// * @param initialFilter initial filter values. Filters contain the other part of requests args, that change.
// * Change filter values by calling editFilter(). This field optional, as well as filter itself
// * @see AbstractSnippetListModel
// */
//class ArticlesListModel(
//	override val path: String,
//	override val coroutineScope: CoroutineScope,
//	vararg baseArgs: Pair<String, String>,
//	initialFilter: Filter? = null,
//) : AbstractSnippetListModel<ArticleSnippet>(
//	path = path,
//	baseArgs = baseArgs.toMap(),
//	coroutineScope = coroutineScope,
//	initialFilter = initialFilter
//) {
//	override fun load(args: Map<String, String>): HabrList<ArticleSnippet>? =
//		ArticlesListController.getArticlesSnippets(path, args)
//
//}


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
) : AbstractSnippetListModel<ArticleCardData>(
	path = path,
	baseArgs = baseArgs.toMap(),
	coroutineScope = coroutineScope,
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<ArticleCardData>? {
		return ArticlesListController.getArticlesSnippets(path, args)?.let {
			HabrList(it.list.map { it.toArticleCardData() }, it.pagesCount)
		}
	}
}
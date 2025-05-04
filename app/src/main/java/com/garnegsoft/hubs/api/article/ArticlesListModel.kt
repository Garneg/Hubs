package com.garnegsoft.hubs.api.article

import ArticleController
import androidx.compose.ui.util.fastAny
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardData
import com.garnegsoft.hubs.ui.common.feedCards.article.toArticleCardData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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

	val listOfModelArticles = mutableListOf<ArticleSnippet>()

	private fun isArticleTypeNews(articleId: Int): Boolean
		= listOfModelArticles.any { it.type == PostType.News && it.id == articleId }


	override fun refresh() {
		listOfModelArticles.clear()
		super.refresh()
	}

	override fun load(args: Map<String, String>): HabrList<ArticleCardData>? {
		return ArticlesListController.getArticlesSnippets(path, args)?.let {
			listOfModelArticles.addAll(it.list)
			HabrList(it.list.map { it.toArticleCardData() }, it.pagesCount)
		}
	}



	suspend fun toggleArticleBookmark(targetStateBookmarked: Boolean, articleId: Int): Boolean {
		var result: Boolean
		withContext(Dispatchers.IO) {
			val isNews = isArticleTypeNews(articleId)
			if (targetStateBookmarked){
                result = ArticleController.addToBookmarks(articleId, isNews)
			} else {
				result = ArticleController.removeFromBookmarks(articleId, isNews)
			}
		}
		return result
	}
}
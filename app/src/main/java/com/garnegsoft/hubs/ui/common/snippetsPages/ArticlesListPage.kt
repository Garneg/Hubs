package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCard
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardConfiguration
import com.garnegsoft.hubs.ui.common.FilterElement
import com.garnegsoft.hubs.ui.common.feedCards.article.BlockedAuthorArticleCard
import com.garnegsoft.hubs.ui.screens.main.ArticlesFilter


private object dummyFilter : Filter {
	override fun toArgsMap(): Map<String, String> {
		TODO("Not yet implemented")
	}

	override fun getTitle(): String {
		TODO("Not yet implemented")
	}

}

@Composable
fun ArticlesListPage(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true,
	ignoreBlackList: Boolean = false
) {
	ArticlesListPageWithFilter<dummyFilter>(
		listModel = listModel,
		lazyListState = lazyListState,
		onArticleSnippetClick = onArticleSnippetClick,
		onArticleAuthorClick = onArticleAuthorClick,
		onArticleCommentsClick = onArticleCommentsClick,
		doInitialLoading = doInitialLoading,
		ignoreBlackList = ignoreBlackList,
		filterDialog = null,
	)
}

@Composable
fun <F : Filter> ArticlesListPageWithFilter(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	filter: (@Composable (onClick: () -> Unit) -> Unit) = { onClick ->
		listModel.filter.observeAsState().value?.let {
			FilterElement(title = it.getTitle(), onClick = { onClick() })
		}
	},
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true,
	ignoreBlackList: Boolean = false,
	filterDialog: (@Composable (defaultValues: F, onDismiss: () -> Unit, onDone: (F) -> Unit) -> Unit)?,
) {
	val context = LocalContext.current
	val userAuthenticated by AuthDataController.isAuthorizedFlow(context).collectAsState(false)
	val cardsStyle = ArticleCardConfiguration.defaultArticleCardStyle()?.copy(bookmarksButtonAllowedBeEnabled = userAuthenticated)
	val ratingIconPainter = painterResource(id = R.drawable.rating)
	val viewsIconPainter = painterResource(id = R.drawable.views_icon)
	val bookmarkIconPainter = painterResource(id = R.drawable.bookmark)
	val filledBookmarkIconPainter = painterResource(id = R.drawable.bookmark_filled)
	val commentIconPainter = painterResource(id = R.drawable.comments_icon)
	val complexityIconPainter = painterResource(id = R.drawable.speedmeter_hard)
	val readingTimeIconPainter = painterResource(id = R.drawable.clock_icon)
	val translationIconPainter = painterResource(id = R.drawable.translation)
	cardsStyle?.let { articleCardsStyle ->
		CommonPageWithFilter(
			listModel = listModel, filterDialog = filterDialog,
			filter = filter,
			doInitialLoading = doInitialLoading,
			lazyListState = lazyListState,
			collapsingContentState = collapsingContentState
		) {
			if (it.author?.isBlockListed == true && !ignoreBlackList) {
				BlockedAuthorArticleCard(
					cardData = it,
					articleCardConfiguration = articleCardsStyle,
					onAuthorClick = { onArticleAuthorClick(it.author.alias) })
			} else {
				ArticleCard(
					cardData = it,
					onClick = { onArticleSnippetClick(it.id) },
					onAuthorClick = { it.author?.alias?.let { onArticleAuthorClick(it) } },
					onCommentsClick = { onArticleCommentsClick(it.id) },
					configuration = articleCardsStyle,
					toggleBookmark = { addToBookmarks, articleId ->  listModel.toggleArticleBookmark(addToBookmarks, articleId) },
					ratingIconPainter = ratingIconPainter,
					viewsIconPainter = viewsIconPainter,
					bookmarkIconPainter = bookmarkIconPainter,
					filledBookmarkIconPainter = filledBookmarkIconPainter,
					commentIconPainter = commentIconPainter,
					complexityIconPainter = complexityIconPainter,
					readingTimeIconPainter = readingTimeIconPainter,
					translationIconPainter = translationIconPainter,
				)
			}
		}
	}
	
}
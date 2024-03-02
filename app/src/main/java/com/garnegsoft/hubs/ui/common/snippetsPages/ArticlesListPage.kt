package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCard
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.FilterElement
import kotlinx.coroutines.launch


@Composable
fun ArticlesListPage(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filterIndicator: (@Composable () -> Unit)? = null,
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true,
) {
	val cardsStyle = ArticleCardStyle.defaultArticleCardStyle()
	val ratingIconPainter = painterResource(id = R.drawable.rating)
	val viewsIconPainter = painterResource(id = R.drawable.views_icon)
	val bookmarkIconPainter = painterResource(id = R.drawable.bookmark)
	val filledBookmarkIconPainter = painterResource(id = R.drawable.bookmark_filled)
	val commentIconPainter = painterResource(id = R.drawable.comments_icon)
	val complexityIconPainter = painterResource(id = R.drawable.speedmeter_hard)
	val readingTimeIconPainter = painterResource(id = R.drawable.clock_icon)
	val translationIconPainter = painterResource(id = R.drawable.translation)
	
	cardsStyle?.let { articleCardStyle ->
		CommonPage(
			listModel = listModel,
			lazyListState = lazyListState,
			collapsingBar = filterIndicator,
			doInitialLoading = doInitialLoading
		) {
			ArticleCard(
				article = it,
				onClick = { onArticleSnippetClick(it.id) },
				onAuthorClick = { it.author?.alias?.let { onArticleAuthorClick(it) } },
				onCommentsClick = { onArticleCommentsClick(it.id) },
				style = articleCardStyle,
				ratingIconPainter,
				viewsIconPainter,
				bookmarkIconPainter,
				filledBookmarkIconPainter,
				commentIconPainter,
				complexityIconPainter,
				readingTimeIconPainter,
				translationIconPainter
			)
		}
	}
}

@Composable
fun <F : Filter> ArticlesListPageWithFilter(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	filter: (@Composable (onClick: () -> Unit) -> Unit) = { onClick ->
		listModel.filter.observeAsState().value?.let {
			val corscop = rememberCoroutineScope()
			FilterElement(title = it.getTitle(), onClick = {
				corscop.launch {
					collapsingContentState.animateShow()
				}
				onClick()
			})
		}
	},
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true,
	filterDialog: @Composable (defaultValues: F, onDismiss: () -> Unit, onDone: (F) -> Unit) -> Unit,
) {
	val cardsStyle = ArticleCardStyle.defaultArticleCardStyle()
	val ratingIconPainter = painterResource(id = R.drawable.rating)
	val viewsIconPainter = painterResource(id = R.drawable.views_icon)
	val bookmarkIconPainter = painterResource(id = R.drawable.bookmark)
	val filledBookmarkIconPainter = painterResource(id = R.drawable.bookmark_filled)
	val commentIconPainter = painterResource(id = R.drawable.comments_icon)
	val complexityIconPainter = painterResource(id = R.drawable.speedmeter_hard)
	val readingTimeIconPainter = painterResource(id = R.drawable.clock_icon)
	val translationIconPainter = painterResource(id = R.drawable.translation)
	cardsStyle?.let { style ->
		CommonPageWithFilter(
			listModel = listModel, filterDialog = filterDialog,
			filter = filter,
			doInitialLoading = doInitialLoading,
			lazyListState = lazyListState,
			collapsingContentState = collapsingContentState
		) {
			ArticleCard(
				article = it,
				onClick = { onArticleSnippetClick(it.id) },
				onAuthorClick = { it.author?.alias?.let { onArticleAuthorClick(it) } },
				onCommentsClick = { onArticleCommentsClick(it.id) },
				style = style,
				ratingIconPainter,
				viewsIconPainter,
				bookmarkIconPainter,
				filledBookmarkIconPainter,
				commentIconPainter,
				complexityIconPainter,
				readingTimeIconPainter,
				translationIconPainter
			)
		}
	}
	
}
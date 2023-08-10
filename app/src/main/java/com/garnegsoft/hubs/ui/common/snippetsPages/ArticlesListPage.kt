package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.FilterElement


@Composable
fun ArticlesListPage(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filterIndicator: (@Composable () -> Unit)? = null,
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true
) {
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
			onCommentsClick = { onArticleCommentsClick(it.id) }
		)
	}
}

@Composable
fun <F : Filter> ArticlesListPageWithFilter(
	listModel: ArticlesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filter: (@Composable (onClick: () -> Unit) -> Unit) = { onClick ->
		listModel.filter.observeAsState().value?.let {
			FilterElement(title = it.getTitle(), onClick = onClick)
		}
	},
	onArticleSnippetClick: (articleId: Int) -> Unit,
	onArticleAuthorClick: (authorAlias: String) -> Unit,
	onArticleCommentsClick: (articleId: Int) -> Unit,
	doInitialLoading: Boolean = true,
	filterDialog: @Composable (defaultValues: F, onDismiss: () -> Unit, onDone: (F) -> Unit) -> Unit,
) {
	CommonPageWithFilter(
		listModel = listModel, filterDialog = filterDialog,
		filter = filter,
		doInitialLoading = doInitialLoading,
		lazyListState = lazyListState
	) {
		ArticleCard(
			article = it,
			onClick = { onArticleSnippetClick(it.id) },
			onAuthorClick = { it.author?.alias?.let { onArticleAuthorClick(it) } },
			onCommentsClick = { onArticleCommentsClick(it.id) }
		)
	}
}
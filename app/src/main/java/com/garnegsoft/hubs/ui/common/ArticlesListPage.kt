package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.api.article.ArticlesListModel

@Composable
fun ArticlesListPage(
        listModel: ArticlesListModel,
        lazyListState: LazyListState = rememberLazyListState(),
        filterIndicator: (@Composable () -> Unit)? = null,
        onArticleSnippetClick: (articleId: Int) -> Unit,
        onArticleAuthorClick: (authorAlias: String) -> Unit,
        onArticleCommentsClick: (articleId: Int) -> Unit
) {
    CommonPage(
            listModel = listModel,
            lazyListState = lazyListState,
            filter = filterIndicator,
    ) {
        ArticleCard(
                article = it,
                onClick = { onArticleSnippetClick(it.id) },
                onAuthorClick = { it.author?.alias?.let { onArticleAuthorClick(it) } },
                onCommentsClick = { onArticleCommentsClick(it.id) }
        )
    }
}
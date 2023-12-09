package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.article.PostsListModel
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.feedCards.post.PostCard


@Composable
fun PostsListPage(
	data: PostsListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
) {
	ArticleCardStyle.defaultArticleCardStyle()?.let { style ->
		CommonPage(listModel = data) {
			PostCard(post = it, style = style)
		}
	}
	
}
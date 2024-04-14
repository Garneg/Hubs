package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.data.comment.CommentsListModel
import com.garnegsoft.hubs.ui.common.feedCards.comment.CommentCard


@Composable
fun CommentsListPage(
	listModel: CommentsListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filterIndicator: (@Composable () -> Unit)? = null,
	onArticleClick: (articleId: Int) -> Unit,
	onCommentClick: (articleId: Int, commentId: Int) -> Unit,
	onUserClick: (userAlias: String) -> Unit,
	doInitialLoading: Boolean = true
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = filterIndicator,
		doInitialLoading = doInitialLoading
	) {
		CommentCard(
			comment = it,
			onCommentClick = { onCommentClick(it.parentPost.id, it.id) },
			onAuthorClick = { onUserClick(it.author.alias) },
			onParentPostClick = { onArticleClick(it.parentPost.id) }
		)
	}
}
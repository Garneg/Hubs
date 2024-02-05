package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.garnegsoft.hubs.api.comment.CommentsCollection


class CommentsScreenState(
	val lazyListState: LazyListState,
	val commentsCollection: CommentsCollection? // null indicates that comments are not loaded yet
) {
	private val commentsIds: List<Int> = commentsCollection?.comments?.map { it.id } ?: emptyList()
	
	private val _collapsedComments: MutableState<List<Int>> = mutableStateOf(emptyList())
	val collapsedComments: State<List<Int>> = _collapsedComments
	
	// Necessary for showing that thread were collapsed with special "header"
	private val _collapsedCommentsParents: MutableState<List<Int>> = mutableStateOf(emptyList())
	val collapsedCommentsParents: State<List<Int>> = _collapsedCommentsParents
	
	suspend fun scrollToComment(commentId: Int, offset: Int) {
		lazyListState.animateScrollToItem(calculateIndexOfComment(commentId), offset)
	}
	
	fun collapseThread(commentId: Int) {
		_collapsedCommentsParents.value = _collapsedCommentsParents.value + commentId
		addCommentsToSkipList(getChildrenOfComment(commentId))
	}
	
	fun expandThread(commentId: Int) {
		_collapsedCommentsParents.value = _collapsedCommentsParents.value.filterNot { it == commentId }
		removeCommentsFromSkipList(getChildrenOfComment(commentId))
	}
	
	private fun getChildrenOfComment(commentId: Int): List<Int> {
		val commentIndex = commentsIds.indexOf(commentId)
		val comment = commentsCollection!!.comments.elementAt(commentIndex)
		val commentLevel = comment.level
		
		val ids = mutableListOf<Int>()
		
		for (index in (commentIndex + 1)..<commentsIds.size) {
			if (commentsCollection.comments[index].level > commentLevel) {
				ids.add(commentsIds[index])
			} else {
				break
			}
		}
		return ids
	}
	
	private fun calculateIndexOfComment(commentId: Int): Int {
		if (commentsIds.contains(commentId)){
			val pinnedCommentsCount = commentsCollection!!.pinnedComments.size
			val commentListIndex = commentsIds.indexOf(commentId)
			
			return commentListIndex + pinnedCommentsCount + 1 // first article item
		}
		throw Exception("No comment with such id: $commentId")
	}
	
	private fun addCommentsToSkipList(ids: List<Int>) {
		_collapsedComments.value = _collapsedComments.value + ids
	}
	
	private fun removeCommentsFromSkipList(ids: List<Int>){
		_collapsedComments.value = _collapsedComments.value.filterNot { ids.contains(it) }
	}
	
}

@Composable
fun rememberCommentsScreenState(
	commentsList: CommentsCollection?,
	lazyListState: LazyListState = rememberLazyListState()
): CommentsScreenState {
	return remember(commentsList) { CommentsScreenState(lazyListState, commentsList) }
}
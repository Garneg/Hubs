package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.garnegsoft.hubs.api.comment.CommentsCollection
import com.garnegsoft.hubs.api.utils.animateShortScrollToItem


class CommentsScreenNavigationState(
    val lazyListState: LazyListState,
    val commentsCollection: CommentsCollection? // null indicates that comments are not loaded yet
) {
    private val commentsIds: List<Int> = commentsCollection?.comments?.map { it.id } ?: emptyList()

    val newCommentsIds: List<Int> =
        commentsCollection?.comments?.filter { it.isNew }?.map { it.id } ?: emptyList()

    private val _collapsedComments: MutableState<List<Int>> = mutableStateOf(emptyList())
    val collapsedComments: List<Int> by _collapsedComments

    // Necessary for showing which thread was collapsed 
    private val _collapsedCommentsParents: MutableState<List<Int>> = mutableStateOf(emptyList())
    val collapsedCommentsParents: List<Int> by _collapsedCommentsParents

    fun setScrollBaseOffset(offset: Int) {
        baseOffset = offset
    }

    private var baseOffset: Int = 0

    suspend fun scrollToComment(commentId: Int, offset: Int = 0) {
        lazyListState.animateShortScrollToItem(
            calculateIndexOfComment(commentId),
            offset + baseOffset,
            teleportItemOffset = 2
        )
    }

    fun collapseComment(commentId: Int) {
        _collapsedCommentsParents.value = _collapsedCommentsParents.value + commentId
        addCommentsToSkipList(getChildrenOfComment(commentId))
    }

    suspend fun collapseThread(childCommentId: Int) {
        val rootCommentId = getRootCommentId(childCommentId)
        collapseComment(rootCommentId)
        scrollToComment(rootCommentId)
    }

    fun getRootCommentId(childCommentId: Int): Int {
        val comment = commentsCollection!!.comments.find { it.id == childCommentId }
        var parentId = comment!!.parentCommentId ?: return childCommentId
        while (true) {
            val comm = commentsCollection.comments.find { it.id == parentId }
            comm!!.parentCommentId?.let {
                parentId = it
            } ?: return comm.id

        }

    }

    fun expandThread(commentId: Int) {
        _collapsedCommentsParents.value =
            _collapsedCommentsParents.value.filterNot { it == commentId }
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
        if (commentsIds.contains(commentId)) {
            val pinnedCommentsCount = commentsCollection!!.pinnedComments.size
            val commentListIndex = commentsIds.indexOf(commentId)

            return commentListIndex + pinnedCommentsCount + 1 // first article item
        }
        throw Exception("No comment with such id: $commentId")
    }

    private fun addCommentsToSkipList(ids: List<Int>) {
        _collapsedComments.value = _collapsedComments.value + ids
    }

    private fun removeCommentsFromSkipList(ids: List<Int>) {
        _collapsedComments.value = _collapsedComments.value.filterNot { ids.contains(it) }
    }


    var showNewCommentsNavigationControl: Boolean by mutableStateOf(false)
    private var _newCommentsControlState: NewCommentsNavigationControlState? by mutableStateOf(null)
    val newCommentsNavigationControlState: NewCommentsNavigationControlState? by derivedStateOf { _newCommentsControlState }

    init {
        if (newCommentsIds.isNotEmpty() && _newCommentsControlState == null) {
            showNewCommentsNavigationControl = true
            _newCommentsControlState = NewCommentsNavigationControlState(this)
        }
    }

    companion object {
        fun saver(
            lazyListState: LazyListState,
            commentsCollection: CommentsCollection?
        ): Saver<CommentsScreenNavigationState, *> = listSaver(
            save = {
                listOf(
                    it.newCommentsNavigationControlState?.showGoToNewCommentsButton,
                    it.newCommentsNavigationControlState?.currentCommentIndex,
                    it.collapsedComments,
                    it.collapsedCommentsParents
                )
            },
            restore = {
                CommentsScreenNavigationState(
                    lazyListState,
                    commentsCollection,
                    it[0] as Boolean?,
                    it[1] as Int?,
                    it[2] as List<Int>,
                    it[3] as List<Int>
                )
            }
        )
    }

    // private constructor for saver
    private constructor(
        lazyListState: LazyListState,
        commentsCollection: CommentsCollection?,
        newCommentsControlShowGoToNewButton: Boolean?,
        newCommentsControlInitialCommentIndex: Int?,
        collapsedCommentsList: List<Int>,
        collapsedCommentsParentsList: List<Int>
    ) : this(lazyListState, commentsCollection) {
        if (newCommentsIds.isNotEmpty()) {
            showNewCommentsNavigationControl = true
            _newCommentsControlState = NewCommentsNavigationControlState(
                this,
                newCommentsControlShowGoToNewButton!!,
                newCommentsControlInitialCommentIndex!!
            )
        }
        this._collapsedComments.value = collapsedCommentsList
        this._collapsedCommentsParents.value = collapsedCommentsParentsList
    }

    class NewCommentsNavigationControlState(
        val commentsScreenState: CommentsScreenNavigationState,
        showGoToNewCommentsButton: Boolean = true,
        initialCommentIndex: Int = 0,
    ) {

        var showGoToNewCommentsButton: Boolean by mutableStateOf(showGoToNewCommentsButton)
        var currentCommentIndex: Int by mutableIntStateOf(initialCommentIndex)

        val newCommentsAmount: Int = commentsScreenState.newCommentsIds.size
        val currentCommentNumber: Int by derivedStateOf { currentCommentIndex + 1 }

        suspend fun scrollToNextComment() {
            if (currentCommentIndex < commentsScreenState.newCommentsIds.lastIndex) {
                currentCommentIndex++
                scrollToCurrentComment()
            } else {
                scrollToFirst()
            }
        }

        suspend fun scrollToPreviousComment() {
            if (currentCommentIndex > 0) {
                currentCommentIndex--
            } else {
                currentCommentIndex = commentsScreenState.newCommentsIds.lastIndex
            }
            scrollToCurrentComment()
        }

        suspend fun scrollToCurrentComment() {
            val commentId = commentsScreenState.newCommentsIds[currentCommentIndex]
            commentsScreenState.expandThread(commentId)
            commentsScreenState.scrollToComment(
                commentId,
                0
            )
        }

        /**
         * Scrolls to first of new comments.
         * Also sets _showGoToNewCommentsLabel_ to false, which triggers control to show counter and buttons
         */
        suspend fun scrollToFirst() {
            showGoToNewCommentsButton = false
            currentCommentIndex = 0
            scrollToCurrentComment()
        }
    }
}

@Composable
fun rememberCommentsScreenNavigationState(
    commentsList: CommentsCollection?,
    lazyListState: LazyListState = rememberLazyListState()
): CommentsScreenNavigationState {
    return rememberSaveable(
        commentsList,
        saver = CommentsScreenNavigationState.saver(lazyListState, commentsList)
    ) { CommentsScreenNavigationState(lazyListState, commentsList) }
}


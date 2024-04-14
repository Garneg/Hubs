package com.garnegsoft.hubs.data.comment

import com.garnegsoft.hubs.data.article.Article

class Comment (
    val id: Int,
    val parentCommentId: Int?,
    val level: Int,
    val publishedTime: String,
    val message: String,
    val score: Int?,
    val votesCount: Int?,
    val deleted: Boolean,
    val author: Article.Author,
    val isArticleAuthor: Boolean,
    val isNew: Boolean,
    val isUserAuthor: Boolean,
    val edited: Boolean,
    val inModeration: Boolean
) : CommentPlaceholder

class CommentsCollection(
    val comments: ArrayList<Comment>,
    val commentAccess: CommentAccess,
    val pinnedComments: List<Int>
) {
    class CommentAccess(
        val canComment: Boolean,
        val cantCommentReason: String?
    )
}

class Threads(
    val threads: List<ThreadSnippet>,
    val commentAccess: CommentsCollection.CommentAccess
)

class Thread(
    val root: Comment,
    val children: List<Comment>
)

data class ThreadSnippet(
    val root: Comment,
    val threadChildrenCommentsCount: Int,
    val threadNewChildrenCommentsCount: Int,
)

data class ViewThreadLabel(
    val threadId: Int,
    val hiddenCommentsCount: Int,
    
) : CommentPlaceholder

interface CommentPlaceholder {

}

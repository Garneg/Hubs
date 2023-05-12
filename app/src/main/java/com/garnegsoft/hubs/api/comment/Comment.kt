package com.garnegsoft.hubs.api.comment

import com.garnegsoft.hubs.api.article.Article

class Comment (
    val id: Int,
    val parentCommentId: Int?,
    val level: Int,
    val publishedTime: String,
    val message: String,
    val score: Int?,
    val deleted: Boolean,
    val author: Article.Author,
    val isArticleAuthor: Boolean,
    val isNew: Boolean,
    val isUserAuthor: Boolean,
    val edited: Boolean,
    val inModeration: Boolean
)

class ArticleComments(
    val comments: ArrayList<Comment>,
    val commentAccess: CommentAccess
) {
    class CommentAccess(
        val canComment: Boolean,
        val cantCommentReason: String?
    )
}

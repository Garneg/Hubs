package com.garnegsoft.hubs.api.comment

import com.garnegsoft.hubs.api.article.Article

class Comment (
    val id: Int,
    val parentCommentId: Int?,
    val level: Int,
    val publishedTime: String,
    val message: String,
    val score: Int,
    val children: MutableList<Comment>,
    val deleted: Boolean = false,
    val author: Article.Author,
    val isArticleAuthor: Boolean,
    val edited: Boolean
)

package com.garnegsoft.hubs.data.comment.list

import com.garnegsoft.hubs.data.HabrSnippet
import com.garnegsoft.hubs.data.article.Article

/**
 * Snippet of comment, should be used in comments list, has info about parent port
 */
data class CommentSnippet(

    override val id: Int,

    val parentPost: ParentPost,

    val text: String,

    val timePublished: String,

    val score: Int,

    val author: Article.Author

) : HabrSnippet {
    data class ParentPost(

        val id: Int,

        val title: String,

    )
}
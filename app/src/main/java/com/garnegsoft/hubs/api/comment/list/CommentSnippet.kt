package com.garnegsoft.hubs.api.comment.list

import com.garnegsoft.hubs.api.HubsLazyListItem
import com.garnegsoft.hubs.api.article.Article

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

) : HubsLazyListItem {
    data class ParentPost(

        val id: Int,

        val title: String,

    )
}
package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.ui.text.AnnotatedString
import com.garnegsoft.hubs.api.PublicationComplexity

data class ArticleCardData(
    val articleTitle: String,
    val articleStatistics: Statistics,
    val articleComplexity: PublicationComplexity,
    val timeToRead: Int,
    val hubs: AnnotatedString,
    val articleTextSnippet: String,
    val articleBookmarked: Boolean,
    val articlePublicationDate: String,
    val articleImageUrl: String?,
    val author: Author,
    ) {
    data class Statistics(
        val rating: String,
        val views: String,
        val bookmarks: Int,
        val comments: String,
    )

    data class Author(
        val alias: String,
        val avatarUrl: String
    )
}

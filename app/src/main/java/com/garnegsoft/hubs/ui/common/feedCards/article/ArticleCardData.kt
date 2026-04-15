package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.garnegsoft.hubs.api.HubsLazyListItem
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.HubSubscribedColor


data class ArticleCardData(
    override val id: Int,
    val articleTitle: String,
    val articleStatistics: Statistics,
    val articleComplexity: PublicationComplexity,
    val timeToRead: Int,
    val hubs: AnnotatedString,
    val articleTextSnippet: String,
    val articleBookmarked: Boolean,
    val articlePublicationDate: String,
    val articleImageUrl: String?,
    val isArticleTranslation: Boolean,
    val author: Author?,
    ) : HubsLazyListItem {
    data class Statistics(
        val rating: Int,
        val views: String,
        val bookmarks: Int,
        val comments: String,
        val hasUnreadComments: Boolean
    )

    data class Author(
        val alias: String,
        val avatarUrl: String,
        val isBlockListed: Boolean
    )
}

fun ArticleSnippet.toArticleCardData(): ArticleCardData {

    return ArticleCardData(
        id = id,
        articleTitle = title,
        articleStatistics = ArticleCardData.Statistics(
            rating = statistics.score,
            views = formatLongNumbers(statistics.readingCount),
            bookmarks = statistics.bookmarksCount,
            comments = formatLongNumbers(statistics.commentsCount),
            hasUnreadComments = relatedData?.unreadComments?.let { it < statistics.commentsCount } ?: false
        ),
        articleComplexity = complexity,
        timeToRead = this.readingTime,
        hubs = buildAnnotatedString {
            hubs!!.forEachIndexed { index, it ->
                val textFunc = if (it.isProfiled) {
                    { append((it.title + "*").replace(" ", "\u00A0")) }
                } else {
                    { append(it.title.replace(" ", "\u00A0")) }
                }
                if (it.relatedData != null && it.relatedData.isSubscribed) {
                    withStyle(SpanStyle(color = HubSubscribedColor)) {
                        textFunc()
                    }
                } else {
                    textFunc()
                }
                if (index < hubs.size - 1) {
                    append(", ")
                }
            }
        },
        articleTextSnippet = textSnippet,
        articleBookmarked = relatedData?.bookmarked ?: false,
        articlePublicationDate = timePublished,
        articleImageUrl = imageUrl,
        isArticleTranslation = isTranslation,
        author = author?.let { ArticleCardData.Author(alias = it.alias, avatarUrl = it.avatarUrl, this.isInBlackList) }
    )
}

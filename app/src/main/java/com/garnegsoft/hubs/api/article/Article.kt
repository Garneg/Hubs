package com.garnegsoft.hubs.api.article

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import com.garnegsoft.hubs.api.ArticleFormat
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.list.ArticleSnippet


/**
 * Represents article
 */
class Article(

    val id: Int,

    val title: String,

    val timePublished: String,

    val author: Author?,

    val content: @Composable (SpanStyle) -> Unit,

    // TODO: remove
    val contentHtml: String,

    val isCorporative: Boolean,

    val editorVersion: EditorVersion,

    val hubs: List<Hub>,

    val postLabels: List<String>,

    val tags: List<String>,

    /**
     * Represents relations between user and article. Null if user is unauthorized
     */
    val userRelatedData: UserData?,

    val statistics: Statistics,

    val format: ArticleFormat?,

    val postType: PostType,

    val metadata: Metadata?,

    val readingTime: Int,

    val complexity: PostComplexity,

    val relatedData: RelatedData?

) {

    /**
     * Author of the post
     */
    data class Author(
        val alias: String,
        val fullname: String? = null,
        val avatarUrl: String? = null,
    )

    /**
     * Short information about article's hub.
     */

    data class Hub(
        val alias: String,
        val isProfiled: Boolean,
        val isCorporative: Boolean,
        val title: String,
        val relatedData: RelatedData?
    ){
        data class RelatedData(val isSubscribed: Boolean)
    }


    class FlowSnippet(
        val id: Int,
        val alias: String,
        val title: String,
    )


    class UserData(
        val unreadCommentsCount: Long?,
        //val vote: RelatedDataVote,
        val bookmarked: Boolean,
        val canComment: Boolean,
        val canEdit: Boolean,
        val canVote: Boolean,
        val canViewVotes: Boolean,
        val canVotePlus: Boolean,
        val canVoteMinus: Boolean,
        val canModerateComments: Boolean,
        val trackerSubscribed: Boolean,
        val emailSubscribed: Boolean
    )

    /**
     * Stats of article. If value is greater than 1000, it will be shorten to 1k
     */
    data class Statistics(
        val commentsCount: String,
        val favoritesCount: String,
        val readingCount: String,
        val score: Int,
        val votesCountPlus: Int,
        val votesCountMinus: Int
    )

    data class Metadata(
        val mainImageUrl: String?
    )

    data class RelatedData(
        val bookmarked: Boolean,
        val canVotePlus: Boolean,
        val canVoteMinus: Boolean
    )
}

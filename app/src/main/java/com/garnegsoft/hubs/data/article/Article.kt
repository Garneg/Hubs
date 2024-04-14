package com.garnegsoft.hubs.data.article

import com.garnegsoft.hubs.data.ArticleFormat
import com.garnegsoft.hubs.data.EditorVersion
import com.garnegsoft.hubs.data.PublicationComplexity
import com.garnegsoft.hubs.data.PostType


/**
 * Represents article
 */
data class Article(
	
	val id: Int,
	
	val title: String,
	
	val timePublished: String,
	
	val author: Author?,
	
	val contentHtml: String,
	
	val isCorporative: Boolean,
	
	val editorVersion: EditorVersion,
	
	val hubs: List<Hub>,
	
	val tags: List<String>,
	
	val statistics: Statistics,
	
	val format: ArticleFormat?,
	
	val postType: PostType,
	
	val metadata: Metadata?,
	
	val readingTime: Int,
	
	val complexity: PublicationComplexity,
	
	val relatedData: RelatedData?,
	
	val translationData: TranslationData,
	
	val polls: List<Poll>

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


    data class Statistics(
        val commentsCount: Int,
        val bookmarksCount: Int,
        val readingCount: Int,
        val score: Int,
        val votesCountPlus: Int,
        val votesCountMinus: Int
    )

    data class Metadata(
        val mainImageUrl: String?
    )

    data class RelatedData(
        val unreadComments: Int,
        val bookmarked: Boolean,
//        val canVotePlus: Boolean,
//        val canVoteMinus: Boolean
    )

    data class TranslationData(
        val isTranslation: Boolean,
        val originalAuthorName: String?,
        val originUrl: String?
    )

    data class Poll(
        val id: Int,
        val timeFinish: String?,
        val answersType: PollType,
        val votesCount: Int,
        val passCount: Int,
        val title: String,
        val relatedData: RelatedData?,
        val variants: List<Variant>
    ){
        enum class PollType{
            Radio,
            Checkbox
        }

        data class RelatedData(
            val canVote: Boolean
        )

        data class Variant(
            val id: Int,
            val text: String,
            val votesCount: Int,
            val percent: Float,
            val selected: Boolean
        )
    }
}



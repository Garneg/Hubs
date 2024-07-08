package com.garnegsoft.hubs.data.article.list

import androidx.compose.runtime.Immutable
import com.garnegsoft.hubs.data.*
import com.garnegsoft.hubs.data.article.Article

/**
 * Represents article at articles list
 */
@Immutable
data class ArticleSnippet(
	
	/**
     * Id of the post
     */
    override val id: Int,
	
	/**
     * Formatted time of publishing of the post
     */
    val timePublished: String,
	
	/**
     * Is corporative post or not
     */
    val isCorporative: Boolean,
	
	/**
     * Title of the post
     */
    val title: String,
	
	/**
     * Version of editor used for writing the post
     */
    val editorVersion: EditorVersion,
	
	/**
     * Type of the post
     */
    val type: PostType,
	
	/**
     * Labels of the post (e.g. translate)
     */
    val labels: List<String>?,
	
	/**
     * Author of the post
     */
    val author: Article.Author? = null,
	
	/**
     * Unformatted statistics data, better use formatted statistics field
     */
    val statistics: Article.Statistics,
	
	/**
     * Hubs that include the post
     */
    val hubs: List<Article.Hub>?,
	
	/**
     * Text snippet of the post, max length is 3500 characters
     */
    val textSnippet: String,
	
	/**
     * Url of image to draw attention (a.k.a. КДПВ)
     */
    val imageUrl: String?,
	
	/**
     * Format of the post
     */
    val format: ArticleFormat?,
	
	/**
     * Time to read article
     */
    val readingTime: Int,
	
	/**
     * Complexity of the article
     */
    val complexity: PublicationComplexity,
	
	/**
     * Data related to app user
     */
    val relatedData: Article.RelatedData?,
	
	val isTranslation: Boolean,
	
	/**
	 * Flag that indicates whether user added author of this publication in black list or not.
	 * If *true* article should be hidden or message about blocked author displayed instead
	 */
	val isInBlackList: Boolean

) : HabrSnippet
import com.garnegsoft.hubs.api.*
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.offline.HubsList
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup


class ArticleController {

    companion object {

        /**
         * Get Article object.
         * @return Article object with full info
         */
        fun get(
            id: Int,
            args: Map<String, String>? = null
        ): com.garnegsoft.hubs.api.article.Article? {
            return get(path = "articles/$id", args = args)
        }
        
        fun getOfflineArticle(
            id: Int,
            args: Map<String, String>? = null
        ): OfflineArticle? {
            val article = getArticle("articles/$id", args)
            
            return article?.let {
                OfflineArticle(
                    articleId = it.id.toInt(),
                    authorName = it.author?.alias,
                    authorAvatarUrl = if (article.author?.avatarUrl == null) {
                        placeholderAvatarUrl(article.author!!.alias)
                    }
                    else {
                        "https:" + article.author!!.avatarUrl
                    },
                    timePublished = it.timePublished,
                    title = Jsoup.parse(it.titleHtml).text(),
                    contentHtml = it.textHtml,
                    hubs = HubsList(it.hubs.map { it.title }),
                    readingTime = it.readingTime,
                    isTranslation = it.postLabels?.find { it.type == "translation" } != null
                )
            }
        
        }
        
        fun getOfflineArticleSnippet(
            id: Int,
            args: Map<String, String>? = null
        ): OfflineArticleSnippet? {
            val article = getArticle("articles/$id", args)
    
            return article?.let {
                OfflineArticleSnippet(
                    articleId = it.id.toInt(),
                    authorName = it.author?.alias,
                    authorAvatarUrl = if (article.author?.avatarUrl == null) {
                        placeholderAvatarUrl(article.author!!.alias)
                    }
                    else {
                        "https:" + article.author!!.avatarUrl
                    },
                    timePublished = it.timePublished,
                    title = Jsoup.parse(it.titleHtml).text(),
                    hubs = HubsList(it.hubs.map { it.title }),
                    readingTime = it.readingTime,
                    isTranslation = it.postLabels?.find { it.type == "translation" } != null,
                    thumbnailUrl = if (
                        it.leadData.imageUrl == null &&
                        it.leadData.textHtml.contains("<img")
                    ) {
                        Jsoup.parse(it.leadData.textHtml)
                            .getElementsByTag("img")[0]?.attr("src")
                    } else if (it.leadData.imageUrl == null && it.leadData.image?.url != null) {
                        it.leadData.image?.url
                    } else {
                        it.leadData.imageUrl
                    },
                    textSnippet = it.leadData.textHtml
                )
            }
        }

        /**
         * Get snippet of article. Snippets are objects that used to be in lists
         * @return snippet of article
         */
        fun getSnippet(
            id: Int
        ): ArticleSnippet? {
            return getSnippet("articles/$id")
        }

        private fun get(
            path: String,
            args: Map<String, String>? = null
        ): com.garnegsoft.hubs.api.article.Article? {
            val raw = this.getArticle(path, args)

            return raw?.let {
                val it = articleFormat(it)
                com.garnegsoft.hubs.api.article.Article(
                    id = it.id.toInt(),
                    title = Jsoup.parse(it.titleHtml).text(),
                    timePublished = it.timePublished,
                    author = if (it.author != null) {
                        com.garnegsoft.hubs.api.article.Article.Author(
                            alias = it.author!!.alias,
                            fullname = it.author!!.fullname,
                            avatarUrl = it.author!!.avatarUrl,
                        )
                    } else {
                        null
                    },
                    isCorporative = it.isCorporative,
                    editorVersion = EditorVersion.fromString(it.editorVersion),
                    format = if (it.format != null) ArticleFormat.fromString(it.format!!) else null,
                    statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
                        commentsCount = it.statistics.commentsCount,
                        bookmarksCount = it.statistics.favoritesCount,
                        readingCount = it.statistics.readingCount,
                        score = it.statistics.score,
                        votesCountMinus = it.statistics.votesCountMinus,
                        votesCountPlus = it.statistics.votesCountPlus
                    ),
                    hubs = it.hubs.map {
                        com.garnegsoft.hubs.api.article.Article.Hub(
                            alias = it.alias,
                            title = it.title,
                            isProfiled = it.isProfiled,
                            isCorporative = it.type == "corporative",
                            relatedData = it.relatedData?.let {
                                com.garnegsoft.hubs.api.article.Article.Hub.RelatedData(
                                    it.isSubscribed
                                )
                            }

                        )
                    },
                    tags = it.tags?.map { it.titleHtml } ?: emptyList(),
                    postType = PostType.fromString(it.postType),
                    metadata = if (it.metadata != null) com.garnegsoft.hubs.api.article.Article.Metadata(
                        it.metadata!!.mainImageUrl
                    ) else null,
                    complexity = PublicationComplexity.fromString(it.complexity),
                    readingTime = it.readingTime,
                    relatedData = it.relatedData?.let {
                        com.garnegsoft.hubs.api.article.Article.RelatedData(
                            unreadComments = it.unreadCommentsCount,
                            bookmarked = it.bookmarked,
                            canVoteMinus = it.canVoteMinus,
                            canVotePlus = it.canVotePlus
                        )
                    },
                    contentHtml = it.textHtml,
                    translationData = com.garnegsoft.hubs.api.article.Article.TranslationData(
                        isTranslation = it.postLabels?.find { it.type == "translation" } != null,
                        originalAuthorName = it.postLabels?.find { it.type == "translation" }?.data?.originalAuthorName,
                        originUrl = it.postLabels?.find { it.type == "translation" }?.data?.originalUrl
                    ),
                    polls = it.polls.map {
                        com.garnegsoft.hubs.api.article.Article.Poll(
                            id = it.id.toInt(),
                            timeFinish = it.timeElapsed,
                            answersType = when(it.answersType){
                                "checkbox" -> com.garnegsoft.hubs.api.article.Article.Poll.PollType.Checkbox
                                else -> com.garnegsoft.hubs.api.article.Article.Poll.PollType.Radio
                            },
                            votesCount = it.votesCount,
                            passCount = it.passCount,
                            title = it.textHtml,
                            relatedData = it.relatedData?.let {
                                com.garnegsoft.hubs.api.article.Article.Poll.RelatedData(it.canVote)
                            },
                            variants = it.variants.map {
                                com.garnegsoft.hubs.api.article.Article.Poll.Variant(
                                    id = it.id.toInt(),
                                    text = it.textHtml,
                                    votesCount = it.votesCount,
                                    percent = it.percent,
                                    selected = it.selected
                                )
                            }
                        )
                    }
                )
            }
        }


        private fun getSnippet(
            path: String,
            args: Map<String, String>? = null
        ): ArticleSnippet? {
            val raw = this.getArticle(path, args)
            return raw?.let {
                val formatted = articleFormat(it)
                ArticleSnippet(
                    id = formatted.id.toInt(),
                    timePublished = formatted.timePublished,
                    isCorporative = formatted.isCorporative,
                    title = Jsoup.parse(formatted.titleHtml).text(),
                    editorVersion = EditorVersion.fromString(formatted.editorVersion),
                    type = PostType.fromString(formatted.postType),
                    labels = null,
                    author = formatted.author?.let {
                        com.garnegsoft.hubs.api.article.Article.Author(
                            alias = formatted.author!!.alias,
                            fullname = formatted.author!!.fullname,
                            avatarUrl = formatted.author!!.avatarUrl,
                        )
                    },
                    statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
                        commentsCount = formatted.statistics.commentsCount,
                        bookmarksCount = formatted.statistics.favoritesCount,
                        readingCount = formatted.statistics.readingCount,
                        score = formatted.statistics.score,
                        votesCountMinus = formatted.statistics.votesCountMinus,
                        votesCountPlus = formatted.statistics.votesCountPlus
                    ),
                    imageUrl = if (
                        formatted.leadData.imageUrl == null &&
                        formatted.leadData.textHtml.contains("<img")
                    ) {
                        Jsoup.parse(formatted.leadData.textHtml)
                            .getElementsByTag("img")[0]?.attr("src")
                    } else if (formatted.leadData.imageUrl == null && formatted.leadData.image?.url != null) {
                        formatted.leadData.image?.url
                    } else {
                        formatted.leadData.imageUrl
                    },
                    format = formatted.format?.let { ArticleFormat.fromString(formatted.format!!) },
                    textSnippet = formatted.leadData.textHtml,
                    hubs = formatted.hubs.map {
                        com.garnegsoft.hubs.api.article.Article.Hub(
                            alias = it.alias,
                            title = it.title,
                            isProfiled = it.isProfiled,
                            isCorporative = it.type == "corporative",
                            relatedData = it.relatedData?.let {
                                com.garnegsoft.hubs.api.article.Article.Hub.RelatedData(
                                    it.isSubscribed
                                )
                            }
                        )

                    },
                    complexity = PublicationComplexity.fromString(formatted.complexity),
                    readingTime = formatted.readingTime,
                    relatedData = formatted.relatedData?.let {
                        com.garnegsoft.hubs.api.article.Article.RelatedData(
                            unreadComments = it.unreadCommentsCount,
                            bookmarked = it.bookmarked,
                            canVoteMinus = it.canVoteMinus,
                            canVotePlus = it.canVotePlus
                        )
                    },
                    isTranslation = formatted.postLabels?.find { it.type == "translation" } != null,
                )

            }
        }

        private fun getArticle(path: String, args: Map<String, String>? = null): Article? {
            val response = HabrApi.get(path, args)

            var article: Article? = null

            if (response?.body != null && response.code == 200) {
                article = HabrDataParser.parseJson<Article>(response.body!!.string())
            }

            return article
        }
        
        private fun articleFormat(article: Article): Article {
            return article.copy(
                timePublished = formatTime(article.timePublished),
                author = article.author?.copy(
                    avatarUrl =  if (article.author?.avatarUrl == null) {
                        placeholderAvatarUrl(article.author!!.alias)
                    }
                    else {
                        "https:" + article.author!!.avatarUrl
                    }
                )
            )
        }
        
        
        
        fun addToBookmarks(id: Int, isNews: Boolean): Boolean {
            val path = if (isNews) "news/$id/bookmarks" else "articles/$id/bookmarks"
            val response = HabrApi.post(path)
            if (response.code != 200)
                return false
            return true
        }

        fun removeFromBookmarks(id: Int, isNews: Boolean): Boolean {
            val path = if (isNews) "news/$id/bookmarks" else "articles/$id/bookmarks"
            
            val response = HabrApi.delete(path)
            if (response.code != 200)
                return false
            return true
        }

        @Serializable
        private data class PollVote(
            val id: List<String>
        )

        fun vote(pollId: Int, variantsIds: List<Int>): com.garnegsoft.hubs.api.article.Article.Poll? {
            val requestBody = Json.encodeToString(PollVote(variantsIds.map { it.toString() }))
            val response = HabrApi.post("polls/$pollId/vote", requestBody = requestBody.toRequestBody())
            if (response.code != 200)
                return null
            val raw = response.body?.string()?.let {
                HabrDataParser.parseJson<Article.Poll>(it)
            }
            return raw?.let {
                com.garnegsoft.hubs.api.article.Article.Poll(
                    id = it.id.toInt(),
                    timeFinish = it.timeElapsed,
                    answersType = when(it.answersType){
                        "checkbox" -> com.garnegsoft.hubs.api.article.Article.Poll.PollType.Checkbox
                        else -> com.garnegsoft.hubs.api.article.Article.Poll.PollType.Radio
                    },
                    votesCount = it.votesCount,
                    passCount = it.passCount,
                    title = it.textHtml,
                    relatedData = it.relatedData?.let {
                        com.garnegsoft.hubs.api.article.Article.Poll.RelatedData(it.canVote)
                    },
                    variants = it.variants.map {
                        com.garnegsoft.hubs.api.article.Article.Poll.Variant(
                            id = it.id.toInt(),
                            text = it.textHtml,
                            votesCount = it.votesCount,
                            percent = it.percent,
                            selected = it.selected
                        )
                    }
                )
            }
        }


    }

    @Serializable
    private data class Article(
        var id: String,
        var timePublished: String,
        var isCorporative: Boolean,
        var lang: String,
        var titleHtml: String,
        var leadData: ArticleLeadData,
        var editorVersion: String,
        var postType: String,
        var postLabels: List<ArticlePostLabel>? = null,
        var author: ArticleAuthor? = null,
        var statistics: ArticleStatistics,
        var hubs: List<ArticleHub>,
        var flows: List<ArticleFlow>? = null,
        var relatedData: ArticleRelatedData?,
        var textHtml: String,
        var tags: List<ArticleTag>? = null,
        var metadata: ArticleMetadata? = null,
        var polls: List<Poll>,
//        var commentsEnabled: Boolean,
        var rulesRemindEnabled: Boolean?,
        var votesEnabled: Boolean?,
        var status: String?,
        var plannedPublishTime: String? = null,
        var checked: Boolean? = null,
        var hasPinnedComments: Boolean,
        var format: String? = null,
        var isEditorial: Boolean,
        var readingTime: Int,
        var complexity: String?
    ) {

        @Serializable
        data class ArticleAuthor(
            var id: String,
            var alias: String,
            var fullname: String? = null,
            var avatarUrl: String? = null,
            var scoreStats: ArticleScoreStats,
            var rating: Float?,
            var relatedData: AuthorRelatedData?,
            var speciality: String?
        )

        @Serializable
        data class AuthorRelatedData(
            var isSubscribed: Boolean? = null
        )

        @Serializable
        data class ArticleScoreStats(
            var score: Int?,
            var votesCount: Int?
        )

        @Serializable
        data class ArticleFlow(
            var id: String,
            var alias: String,
            var title: String,

            var titleHtml: String
        )

        @Serializable
        data class ArticleHub(
            var id: String,
            var alias: String,
            var type: String,
            var title: String,
            var titleHtml: String,
            var isProfiled: Boolean,
            var relatedData: ArticleHubRelatedData?
        )

        @Serializable
        data class ArticleHubRelatedData(
            var isSubscribed: Boolean
        )

        @Serializable
        data class ArticleLeadData(
            var textHtml: String,
            var imageUrl: String? = null,
            var buttonTextHtml: String? = null,
            var image: ArticleImage? = null
        )

        @Serializable
        data class ArticleImage(
            var url: String,
            var fit: String?,
            var positionY: Float?,
            var positionX: Float?
        )

        @Serializable
        data class ArticleMetadata(
            var stylesUrls: ArrayList<String?>?,
            var scriptUrls: ArrayList<String?>?,

            var shareImageUrl: String?,

            var shareImageWidth: Int?,
            var shareImageHeight: Int?,

            var vkShareImageUrl: String?,

            var schemaJsonLd: String?,

            var metaDescription: String?,

            var mainImageUrl: String? = null,

            var amp: Boolean?,
        )

        @Serializable
        data class ArticlePostLabel(
            var type: String,
            var data: ArticlePostLabelData? = null
        ) {
            @Serializable
            class ArticlePostLabelData(
                var originalAuthorName: String? = null,
                var originalUrl: String? = null
            )
        }

        @Serializable
        data class ArticleRelatedData(
            var unreadCommentsCount: Int,
            var vote: RelatedDataVote,
            var bookmarked: Boolean,
            var canComment: Boolean,
            var canEdit: Boolean,
            var canViewVotes: Boolean,
            var canVotePlus: Boolean,
            var canVoteMinus: Boolean,
            var canModerateComments: Boolean,
            var trackerSubscribed: Boolean,
            var emailSubscribed: Boolean
        )

        @Serializable
        data class RelatedDataVote(
            var value: String? = null,
            var voteTimeExpired: String
        )

        @Serializable
        data class ArticleStatistics(
            var commentsCount: Int,
            var favoritesCount: Int,
            var readingCount: Int,
            var score: Int,
            var votesCount: Int,
            var votesCountPlus: Int,
            var votesCountMinus: Int
        )

        @Serializable
        data class ArticleTag(
            var titleHtml: String
        )

        @Serializable
        data class Poll(
            var id: String,
            var timeElapsed: String? = null,
            var answersType: String,
            var votesCount: Int,
            var passCount: Int,
            var textHtml: String,
            var relatedData: RelatedData? = null,
            var variants: List<Variant>
        ) {
            @Serializable
            data class RelatedData(var canVote: Boolean)
        }

        @Serializable
        data class Variant(
            var id: String,
            var textHtml: String,
            var votesCount: Int,
            var percent: Float,
            var selected: Boolean
        )
    }

}
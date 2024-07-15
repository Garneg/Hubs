import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.api.*
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.ui.screens.article.parseElement
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup


class ArticleController {
    companion object {
        private fun getArticle(path: String, args: Map<String, String>? = null): Article? {
            val response = HabrApi.get(path, args)

            var article: Article? = null

            if (response.body != null) {
                article = HabrDataParser.parseJson<Article>(response.body!!.string())
                article!!.timePublished = formatTime(article.timePublished)
                article.author?.avatarUrl?.let {
                    article.author!!.avatarUrl = "https:" + article.author!!.avatarUrl
                }
            }

            return article
        }

        fun get(
            path: String,
            args: Map<String, String>? = null
        ): com.garnegsoft.hubs.api.article.Article? {
            var raw = this.getArticle(path, args)

            var result: com.garnegsoft.hubs.api.article.Article? = null

            raw?.let {
                result = com.garnegsoft.hubs.api.article.Article(
                    id = it.id.toInt(),
                    title = it.titleHtml,
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
                    content = parseElement(
                        Jsoup.parse(it.textHtml),
                        SpanStyle(fontSize = 16.sp)
                    ).second!!,
                    snippet = ArticleSnippet(
                        id = it.id.toInt(),
                        timePublished = it.timePublished,
                        isCorporative = it.isCorporative,
                        title = Jsoup.parse(it.titleHtml).text(),
                        editorVersion = EditorVersion.fromString(it.editorVersion),
                        type = PostType.fromString(it.postType),
                        labels = null,
                        author = if (it.author != null) {
                            com.garnegsoft.hubs.api.article.Article.Author(
                                alias = it.author!!.alias,
                                fullname = it.author!!.fullname,
                                avatarUrl = it.author!!.avatarUrl,
                            )
                        } else {
                            null
                        },
                        statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
                            commentsCount = it.statistics.commentsCount.toString(),
                            favoritesCount = it.statistics.favoritesCount.toString(),
                            readingCount = it.statistics.readingCount.toString(),
                            score = it.statistics.score
                        ),
                        imageUrl = it.leadData.imageUrl,
                        format = if (it.format != null) ArticleFormat.fromString(it.format!!) else null,
                        textSnippet = it.leadData.textHtml,
                        hubs = listOf(),
                        complexity = PostComplexity.fromString(it.complexity),
                        readingTime = it.readingTime
                    ),
                    editorVersion = EditorVersion.fromString(it.editorVersion),
                    format = if (it.format != null) ArticleFormat.fromString(it.format!!) else null,
                    statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
                        commentsCount = it.statistics.commentsCount.toString(),
                        favoritesCount = it.statistics.favoritesCount.toString(),
                        readingCount = it.statistics.readingCount.toString(),
                        score = it.statistics.score
                    ),
                    //TODO
                    hubs = it.hubs.run {
                        val hubs = arrayListOf<com.garnegsoft.hubs.api.article.Article.Hub>()
                        this.forEach {
                            hubs.add(
                                com.garnegsoft.hubs.api.article.Article.Hub(
                                    alias = it.alias,
                                    title = it.title,
                                    isProfiled = it.isProfiled,
                                    isCorporative = it.type == "corporative"
                                )
                            )
                        }
                        hubs
                    },
                    postLabels = listOf(),
                    tags = arrayListOf<String>().apply {
                        it.tags?.forEach {
                            add(it.titleHtml)
                        }
                    },
                    userRelatedData = null,
                    postType = PostType.fromString(it.postType),
                    metadata = if (it.metadata != null) com.garnegsoft.hubs.api.article.Article.Metadata(
                        it.metadata!!.mainImageUrl
                    ) else null,
                    complexity = PostComplexity.fromString(it.complexity),
                    readingTime = it.readingTime
                )
            }
            return result
        }

        fun getSnippet(
            path: String,
            args: Map<String, String>? = null
        ): ArticleSnippet? {
            var raw = this.getArticle(path, args)

            var result: ArticleSnippet? = null

            raw?.let {
                result = ArticleSnippet(
                    id = it.id.toInt(),
                    timePublished = it.timePublished,
                    isCorporative = it.isCorporative,
                    title = Jsoup.parse(it.titleHtml).text(),
                    editorVersion = EditorVersion.fromString(it.editorVersion),
                    type = PostType.fromString(it.postType),
                    labels = null,
                    author = if (it.author != null) {
                        com.garnegsoft.hubs.api.article.Article.Author(
                            alias = it.author!!.alias,
                            fullname = it.author!!.fullname,
                            avatarUrl = it.author!!.avatarUrl,
                        )
                    } else {
                        null
                    },
                    statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
                        commentsCount = it.statistics.commentsCount.toString(),
                        favoritesCount = it.statistics.favoritesCount.toString(),
                        readingCount = it.statistics.readingCount.toString(),
                        score = it.statistics.score
                    ),
                    imageUrl = it.leadData.imageUrl,
                    format = if (it.format != null) ArticleFormat.fromString(it.format!!) else null,
                    textSnippet = it.leadData.textHtml,
                    hubs = it.hubs.run {
                        val hubs = arrayListOf<com.garnegsoft.hubs.api.article.Article.Hub>()
                        this.forEach {
                            hubs.add(
                                com.garnegsoft.hubs.api.article.Article.Hub(
                                    alias = it.alias,
                                    title = it.title,
                                    isProfiled = it.isProfiled,
                                    isCorporative = it.type == "corporative"
                                )
                            )
                        }
                        hubs
                    },
                    complexity = PostComplexity.fromString(it.complexity),
                    readingTime = it.readingTime
                )
            }

            return result
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
        //var polls: List<Any?>,
//        var commentsEnabled: Boolean,
//        var rulesRemindEnabled: Boolean?,
//        var votesEnabled: Boolean?,
//        var status: String?,
//        var plannedPublishTime: String? = null,
//        var checked: Boolean? = null,
//        var hasPinnedComments: Boolean,
        var format: String? = null,
//        var isEditorial: Boolean,
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
        )

        @Serializable
        data class ArticleRelatedData(
//            var unreadCommentsCount: Int?,
//            var vote: RelatedDataVote,
//            var bookmarked: Boolean,
            var canComment: Boolean,
//            var canEdit: Boolean,
//            var canVote: Boolean,
//            var canViewVotes: Boolean,
//            var canVotePlus: Boolean,
//            var canVoteMinus: Boolean,
//            var canModerateComments: Boolean,
//            var trackerSubscribed: Boolean,
//            var emailSubscribed: Boolean
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
            var votesCount: Int?,
            var votesCountPlus: Int?,
            var votesCountMinus: Int?
        )

        @Serializable
        data class ArticleTag(
            var titleHtml: String
        )
    }

}
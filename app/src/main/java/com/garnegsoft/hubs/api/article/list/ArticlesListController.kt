import android.util.Log
import com.garnegsoft.hubs.api.*
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.jsoup.Jsoup


class ArticlesListController {
	companion object {
		
		/**
		 * Get articles list, that contains articlesIds, articlesRefs, and pagesCount
		 * @param path articles path(still must contain articles!)
		 * @param args optional parameter
		 * @return articles list normally, if empty - no articles found with that path and/or args.
		 * *null* if path or/and args are invalid or status code is not 200
		 **/
		private fun get(path: String, args: Map<String, String>? = null): ArticlesList? {
			var response = HabrApi.get(path, args)
			
			if (response != null && response.code != 200) {
				return null
			}
			
			if (response?.body != null) {
				var responseJson = Json.parseToJsonElement(response!!.body!!.string())
				var articles = kotlin.run {
					var articlesIds =
						HabrDataParser.parseJson<List<Int>>((responseJson.jsonObject["publicationIds"] ?: responseJson.jsonObject["articleIds"])!!.jsonArray)
					var pagesCount = responseJson.jsonObject["pagesCount"]?.jsonPrimitive?.intOrNull
					
					
					var articleIdsfinal = mutableListOf<Int>()
					val articlesRefs = mutableMapOf<String, Article>()
					
					articlesIds.forEach {
						try {
							articlesRefs += mapOf(
								it.toString() to HabrDataParser.parseJson(
									(responseJson.jsonObject["publicationRefs"] ?: responseJson.jsonObject["articleRefs"])!!.jsonObject.get(it.toString())!!
								)
							)
							articleIdsfinal.add(it)
						} catch (ex: java.lang.Exception) {
							Log.e("ARTCL_PARS_ERR", "UNABLE TO PARSE ARTICLE")

//                            if (BuildConfig.DEBUG) {
//                                throw ex
//                            }
						}
					}
					
					
					ArticlesList(
						articleIds = articleIdsfinal,
						articleRefs = articlesRefs,
						pagesCount = pagesCount
					)
				}
				
				
				articles.articleRefs.values.forEach {
					it.apply {
						timePublished = formatTime(timePublished)
						if (author?.avatarUrl.isNullOrBlank() && author != null){
							author!!.avatarUrl = placeholderAvatarUrl(author!!.alias)
						} else {
							author?.avatarUrl =
								"https:" + author?.avatarUrl?.replace("habrastorage", "hsto")
						}
						if (leadData?.imageUrl == null && leadData?.textHtml!!.contains("<img"))
							leadData?.imageUrl = Jsoup.parse(leadData!!.textHtml!!)
								.getElementsByTag("img")[0]?.attr("src")
						
						leadData?.textHtml = Jsoup.parse(leadData!!.textHtml!!).text()
						
						
						if (leadData?.imageUrl == null && leadData?.image?.url != null) {
							leadData?.imageUrl = leadData?.image?.url
						}
						if (leadData.imageUrl != null) {
							leadData.imageUrl = leadData.imageUrl?.replace("http:", "https:")
						}
					}
				}
				
				return articles
			}
			return null
		}
		
		/**
		 * Return valid list of articles snippets. If list is empty, no articles has found.
		 * Null if request wasn't valid, or server error
		 */
		fun getArticlesSnippets(
			path: String,
			args: Map<String, String>? = null
		): HabrList<ArticleSnippet>? {
			var raw = get(path, args)
			
			var result: HabrList<ArticleSnippet>? = null
			
			var articles = arrayListOf<ArticleSnippet>()
			
			if (raw != null) {
				raw.articleIds?.forEach { id ->
					raw.articleRefs.get(id.toString())?.let {
						articles.add(
							ArticleSnippet(
								id = it.id,
								timePublished = it.timePublished,
								isCorporative = it.isCorporative,
								title = Jsoup.parse(it.titleHtml).text(),
								editorVersion = EditorVersion.fromString(it.editorVersion),
								type = PostType.fromString(it.postType),
								author = if (it.author != null) {
									com.garnegsoft.hubs.api.article.Article.Author(
										alias = it.author!!.alias,
										fullname = it.author!!.fullname,
										avatarUrl = it.author!!.avatarUrl,
									)
								} else
									null,
								format = if (it.format != null) ArticleFormat.fromString(it.format!!) else null,
								labels = listOf(),
								hubs = parseHubs(it.hubs),
								statistics = com.garnegsoft.hubs.api.article.Article.Statistics(
									score = it.statistics.score,
									readingCount = it.statistics.readingCount,
									commentsCount = it.statistics.commentsCount,
									bookmarksCount = it.statistics.favoritesCount,
									votesCountMinus = it.statistics.votesCountMinus,
									votesCountPlus = it.statistics.votesCountPlus
								),
								imageUrl = it.leadData.imageUrl,
								textSnippet = it.leadData.textHtml,
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
								isTranslation = it.postLabels?.contains(ArticlesListLabel("translation"))
									?: false
							)
						)
					}
				}
				
				result = HabrList(articles, raw.pagesCount ?: 0)
			}
			
			return result
		}
		
		
		private fun parseHubs(
			hubs: List<ArticlesListHub>
		): List<com.garnegsoft.hubs.api.article.Article.Hub> {
			var result = ArrayList<com.garnegsoft.hubs.api.article.Article.Hub>()
			
			hubs.forEach {
				result.add(
					com.garnegsoft.hubs.api.article.Article.Hub(
						alias = it.alias,
						isProfiled = it.isProfiled,
						isCorporative = it.type == "corporative",
						title = it.title,
						relatedData = it.relatedData?.let {
							com.garnegsoft.hubs.api.article.Article.Hub.RelatedData(
								it.isSubscribed
							)
						}
					
					)
				)
			}
			
			return result
		}
		
	}
	
	@Serializable
	data class ArticlesList(
		var articleIds: MutableList<Int>,
		var articleRefs: Map<String, Article>,
		var pagesCount: Int?
	)
	
	@Serializable
	data class Article(
		var id: Int,
		var timePublished: String,
		var isCorporative: Boolean,
		var titleHtml: String,
		var lang: String,
		var editorVersion: String,
		var postType: String,
		var postLabels: ArrayList<ArticlesListLabel>?,
		var author: ArticlesListAuthor? = null,
		var statistics: ArticlesListStatistics,
		var hubs: ArrayList<ArticlesListHub>,
		var flows: ArrayList<ArticlesListFlow>?,
		var leadData: ArticlesListLeadData,
		var status: String?,
		var tags: ArrayList<ArticlesListTag>? = null,
		var format: String?,
		var readingTime: Int,
		var complexity: String?,
		var relatedData: RelatedData?
	)
	
	@Serializable
	data class RelatedData(
		var unreadCommentsCount: Int,
		var vote: RelatedDataVote,
		var bookmarked: Boolean,
		var canComment: Boolean,
		var canEdit: Boolean,
		var canViewVotes: Boolean,
		var canVotePlus: Boolean,
		var canVoteMinus: Boolean
	) {
		@Serializable
		data class RelatedDataVote(
			var value: String? = null,
			var voteTimeExpired: String
		)
	}
	
	@Serializable
	data class ArticlesListAuthor(
		var id: String,
		var alias: String,
		var fullname: String? = null,
		var avatarUrl: String? = null,
		var speciality: String? = null
	)
	
	@Serializable
	data class ArticlesListLabel(
		var type: String
	)
	
	@Serializable
	data class ArticlesListFlow(
		var id: String,
		var alias: String,
		var title: String,
		var titleHtml: String?
	)
	
	@Serializable
	data class ArticlesListHub(
		var id: String,
		var alias: String,
		var type: String,
		var title: String,
		var isProfiled: Boolean,
		var relatedData: ArticlesListHubRelatedData? = null
	)
	
	@Serializable
	data class ArticlesListHubRelatedData(
		var isSubscribed: Boolean
	)
	
	@Serializable
	data class ArticlesListLeadData(
		var textHtml: String,
		var imageUrl: String?,
		var buttonTextHtml: String?,
		var image: Image? = null
	)
	
	@Serializable
	data class Image(
		val url: String?,
		val fit: String?,
		val positionY: Float?,
		val positionX: Float?
	)
	
	@Serializable
	data class ArticlesListStatistics(
		var commentsCount: Int,
		var favoritesCount: Int,
		var readingCount: Int,
		var score: Int,
		var votesCountPlus: Int,
		var votesCountMinus: Int,
		var votesCount: Int
	)
	
	@Serializable
	data class ArticlesListTag(
		var titleHtml: String
	)
	
	@Serializable
	data class ArticlesListSearchStatistics(
		var articlesCount: Int,
		var commentsCount: Int,
		var hubsCount: Int,
		var usersCount: Int,
		var companiesCount: Int
	)
	
}

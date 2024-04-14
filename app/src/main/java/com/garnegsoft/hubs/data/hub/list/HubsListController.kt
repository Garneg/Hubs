package com.garnegsoft.hubs.data.hub.list

import com.garnegsoft.hubs.data.HabrApi
import com.garnegsoft.hubs.data.HabrList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jsoup.Jsoup


class HubsListController{
    companion object {
        private fun getHubsList(path: String, args: Map<String, String>? = null): HubsList? {
            var response = HabrApi.get(path, args)

            if (response?.code != 200)
                return null

            var result: HubsList? = null

            if (response.body != null){
                var customJson = Json { ignoreUnknownKeys = true }
                result = customJson.decodeFromJsonElement(customJson.parseToJsonElement(response.body!!.string()))
                result?.hubRefs?.forEach {
                    it.value.imageUrl = "https:" + it.value.imageUrl
                    it.value.titleHtml = Jsoup.parse(it.value.titleHtml).text()
                }
            }

            return result
        }

        fun get(path: String, args: Map<String, String>? = null): HabrList<HubSnippet>? {
            var rawList = getHubsList(path, args)

            var result: HabrList<HubSnippet>? = null

            if (rawList != null){
                var hubsList = arrayListOf<HubSnippet>()
                rawList.hubIds.forEach {
                    rawList.hubRefs.get(it)?.let {
                        hubsList.add(
                            HubSnippet(
                                id = it.id.toInt(),
                                alias = it.alias,
                                description = it.descriptionHtml,
                                avatarUrl = it.imageUrl,
                                isProfiled = it.isProfiled,
                                isOfftop = it.isOfftop,
                                statistics = HubSnippet.Statistics(
                                    subscribersCount = it.statistics.subscribersCount,
                                    rating = it.statistics.rating,
                                    authorsCount = it.statistics.authorsCount,
                                    postsCount = it.statistics.postsCount
                                ),
                                tags = mutableListOf<String>().apply {
                                    it.commonTags.forEach { add(it) }
                                },
                                title = it.titleHtml,
                                relatedData = it.relatedData?.let { HubSnippet.RelatedData(it.isSubscribed) }
                            )
                        )
                    }
                }

                result = HabrList(hubsList, rawList.pagesCount)
            }

            return result
        }
    }
}

@Serializable
data class HubsList (
    var pagesCount: Int,

    var hubIds: List<String>,

    var hubRefs: Map<String, HubListItem>
)

@Serializable
data class HubListItem (
    var id: String,
    var alias: String,
    var titleHtml: String,
    var imageUrl: String,
    var descriptionHtml: String,
    var statistics: Statistics,
    var commonTags: List<String>,
    var isProfiled: Boolean,
    var isOfftop: Boolean,
    var relatedData: RelatedData?
){

    @Serializable
    data class Statistics (
        var subscribersCount: Int,
        var rating: Float,
        var authorsCount: Int,
        var postsCount: Int
    )

    @Serializable
    class RelatedData(var isSubscribed: Boolean)
}


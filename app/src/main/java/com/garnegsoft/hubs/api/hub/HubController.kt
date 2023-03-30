package com.garnegsoft.hubs.api.hub

import com.garnegsoft.hubs.api.HabrApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement


class HubController {
    companion object {
        private fun getProfile(path: String, args: Map<String, String>? = null): HubProfile? {
            var response = HabrApi.get(path, args)

            var profile: HubProfile? = null

            response.body?.string()?.let {
                var customJson = Json { ignoreUnknownKeys = true }
                profile = customJson.decodeFromJsonElement(customJson.parseToJsonElement(it))
                profile?.let {
                    profile!!.imageUrl = "https:" + profile!!.imageUrl
                }
            }

            return profile
        }

        fun get(path: String, args: Map<String, String>? = null): Hub? {
            var raw = getProfile(path, args)

            var result: Hub? = null
            
            raw?.let { 
                result = Hub(
                    alias = it.alias,
                    title = it.titleHtml,
                    description = it.descriptionHtml,
                    avatarUrl = it.imageUrl,
                    statistics = Hub.Statistics(
                        subscribersCount = it.statistics.subscribersCount,
                        authorsCount = it.statistics.authorsCount,
                        rating = it.statistics.rating,
                        postsCount = it.statistics.postsCount
                    ),
                    isProfiled = it.isProfiled,
                    relatedData = it.relatedData?.let { Hub.RelatedData(it.isSubscribed) }
                )
            }

            return result
        }


    }
}

@Serializable
data class HubProfile (
    var alias: String,
    var titleHtml: String,
    val descriptionHtml: String,
    val fullDescriptionHtml: String,
    var imageUrl: String,
    var statistics: Statistics,
    var flow: Flow,
    var isProfiled: Boolean,
    var relatedData: RelatedData?
) {
    @Serializable
    data class RelatedData(var isSubscribed: Boolean)
}
@Serializable
data class Flow (
    var id: String,
    var alias: String,
    var title: String,
    val titleHtml: String
)

@Serializable
data class Statistics (
    val subscribersCount: Int,
    val rating: Float,
    val authorsCount: Int,
    val postsCount: Int
)

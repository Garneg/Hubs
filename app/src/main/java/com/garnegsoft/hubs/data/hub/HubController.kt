package com.garnegsoft.hubs.data.hub

import com.garnegsoft.hubs.data.HabrApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*


class HubController {
    companion object {
        private fun getProfile(path: String, args: Map<String, String>? = null): HubProfile? {
            val response = HabrApi.get(path, args)

            var profile: HubProfile? = null

            if (response?.code != 200)
                return null

            response?.body?.string()?.let {
                var customJson = Json { ignoreUnknownKeys = true }
                profile = customJson.decodeFromJsonElement(customJson.parseToJsonElement(it))
                profile?.let {
                    profile!!.imageUrl = "https:" + profile!!.imageUrl
                }
            }

            return profile
        }
        
        fun get(alias: String): Hub? {
            return get(path = "hubs/$alias/profile")
        }

        private fun get(path: String, args: Map<String, String>? = null): Hub? {
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

        /**
         * Subscribe/unsubscribe to hub.
         * @return subscription status
         * @throws UnsupportedOperationException
         */
        fun subscription(alias: String): Boolean {
            val response = HabrApi.post("hubs/$alias/subscription")
            response.body?.string()?.let {
                return Json.parseToJsonElement(it).jsonObject["isSubscribed"]?.jsonPrimitive!!.boolean
            }
            throw UnsupportedOperationException("User is not authorized")

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

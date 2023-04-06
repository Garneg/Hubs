package com.garnegsoft.hubs.api.user

import android.util.Log
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.utils.formatBirthdate
import com.garnegsoft.hubs.api.utils.formatTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class UserController {
    companion object {
        private fun getRaw(path: String, args: Map<String, String>? = null): UserProfileData? {
            var response = HabrApi.get(path, args)

            var result: UserProfileData? = null

            response.body?.let {
                var customJson = Json { ignoreUnknownKeys = true }


                result =
                    customJson.decodeFromJsonElement(customJson.parseToJsonElement(it.string()))

                result?.let {
                    if (it.avatarUrl != null)
                        it.avatarUrl = "https:" + it.avatarUrl!!.replace("habrastorage", "hsto")

                    it.registerDateTime = formatTime(it.registerDateTime).split(' ')
                        .run { "${this[0]} ${this[1]} ${this[2]}" }

                    if (it.birthday != null)
                        it.birthday = formatBirthdate(it.birthday!!)

                    it.lastActivityDateTime =
                        it.lastActivityDateTime?.let { it1 -> formatTime(it1) }
                }
                result
            }

            return result
        }

        fun get(path: String, args: Map<String, String>? = null): User? {
            val raw = getRaw(path, args)

            var result: User? = null

            raw?.let {
                val followedHubs = HubsListController.get(
                    "users/${raw.alias}/subscriptions/hubs",
                    mapOf("perPage" to "50")
                )?.list
                val followedCompanies =
                    CompaniesListController.get("users/${raw.alias}/subscriptions/companies")?.list
                result = User(
                    alias = it.alias,
                    fullname = if (it.fullname?.isEmpty() == true) null else it.fullname,
                    avatarUrl = it.avatarUrl,
                    speciality = if (it.speciality?.isEmpty() == true) null else it.speciality,
                    rating = it.rating,
                    ratingPosition = it.ratingPos,
                    score = it.scoreStats.score,
                    followersCount = it.followStats.followersCount,
                    followsCount = it.followStats.followCount,
                    isReadonly = it.isReadonly,
                    registrationDate = it.registerDateTime,
                    lastActivityDate = it.lastActivityDateTime,
                    birthday = it.birthday,
                    canBeInvited = it.canBeInvited,
                    location = it.location?.let {
                        val buffer = StringBuilder()
                        it.city?.title?.let {
                            buffer.append("$it, ")
                        }
                        it.region?.title?.let {
                            //buffer.append("$it, ")
                        }
                        it.country?.title?.let{
                            buffer.append(it)
                        }

                        buffer.toString()
                    },
                    workPlaces = listOf(),
                    whoIs = null,
                    occupation = null,
                    note = null,
                    articlesCount = it.counterStats.postCount,
                    commentsCount = it.counterStats.commentCount,
                    favoritesCount = it.counterStats.favoriteCount,
                    followHubs = followedHubs,
                    followCompanies = followedCompanies,
                    relatedData = it.relatedData?.let{ User.RelatedData(it.isSubscribed)}
                )
            }

            return result
        }

        /**
         * Subscribe/unsubscribe to user.
         * @return subscription status
         * @throws UnsupportedOperationException TODO: check subscription to user itself
         */
        fun subscription(alias: String): Boolean {
            val response = HabrApi.post("users/$alias/following/toggle")

            response.body?.string()?.let {
                return Json.parseToJsonElement(it).jsonObject["isSubscribed"]?.jsonPrimitive!!.boolean
            }
            throw UnsupportedOperationException("User is not authorized")

        }
    }

    @Serializable
    data class WhoIs(
        val alias: String,
        val badgets: List<Badget>,
        val aboutHtml: String,
        val contacts: List<Contact>,
        val invitedBy: InvitedBy? = null
    )


    @Serializable
    data class Badget(
        val id: String,
        val title: String,
        val description: String,
        val url: String? = null,
        val isRemovable: Boolean
    )

    @Serializable
    data class Contact(
        val title: String,
        val url: String,
        val value: String?,
        val siteTitle: String? = null,
        val favicon: String? = null
    )

    @Serializable
    data class InvitedBy(
        val issuerLogin: String?,
        val timeCreated: String
    )

}

@Serializable
data class UserProfileData(
    var alias: String,
    var fullname: String?,
    var avatarUrl: String? = null,
    var speciality: String?,
    var gender: String,
    var rating: Float,
    var ratingPos: Int? = null,
    var scoreStats: ScoreStats,
    var relatedData: RelatedData? = null,
    var followStats: FollowStats,
    var lastActivityDateTime: String?,
    var registerDateTime: String,
    var birthday: String? = null,
    var location: Location?,
    var workplace: List<WorkPlace>,
    var counterStats: CounterStats,
    var isReadonly: Boolean,
    var canBeInvited: Boolean
) {
    @Serializable
    data class RelatedData(var isSubscribed: Boolean)
}

@Serializable
data class CounterStats(
    var postCount: Int,
    var commentCount: Int,
    var favoriteCount: Int
)

@Serializable
data class FollowStats(
    val followCount: Int,
    val followersCount: Int
)

@Serializable
data class Location(
    val city: LocationItem?,
    val region: LocationItem?,
    val country: LocationItem?
)

@Serializable
data class LocationItem(
    val id: String,
    val title: String
)

@Serializable
data class ScoreStats(
    val score: Int,
    val votesCount: Int
)

@Serializable
data class WorkPlace(
    val title: String,
    val alias: String
)

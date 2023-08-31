package com.garnegsoft.hubs.api.user

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import com.garnegsoft.hubs.api.utils.formatBirthdate
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.jsoup.Jsoup

class UserController {

    companion object {
        private fun _get(path: String, args: Map<String, String>? = null): UserProfileData? {
            val response = HabrApi.get(path, args)

            var result: UserProfileData? = null

            if (response?.code != 200)
                return null

            response.body?.let {
                var customJson = Json { ignoreUnknownKeys = true }

                result =
                    customJson.decodeFromJsonElement(customJson.parseToJsonElement(it.string()))

                result?.let {
                    if (it.avatarUrl != null) {
                        it.avatarUrl = "https:" + it.avatarUrl!!.replace("habrastorage", "hsto")
                    }
                    else {
                        it.avatarUrl = placeholderAvatarUrl(it.alias)
                    }
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

        private fun _whoIs(path: String, args: Map<String, String>? = null): WhoIs? {
            val response = HabrApi.get(path, args)
            val responseBody = response?.body?.string()
            if (response?.code != 200 || responseBody == null){
                return null
            }
            responseBody.let {
                val result = HabrDataParser.parseJson<WhoIs>(it)
                result.invitedBy?.let {
                    result.invitedBy!!.timeCreated = formatTime(it.timeCreated)
                }

                return result
            }
            return null
        }

        private fun _note(path: String, args: Map<String, String>? = null): Note? {
            val response = HabrApi.get(path, args)

            if (response?.code != 200)
                return null

            response.body?.string()?.let {
                val result = HabrDataParser.parseJson<Note>(it)
                result.text?.let {
                    result.text = Jsoup.parse(it).text()
                }
                return result
            }
            return null
        }

        fun get(
            alias: String,
            args: Map<String, String>? = null,
        ): User? {
            val raw = _get("users/$alias/card", args)

            var result: User? = null

            raw?.let {

                result = User(
                    alias = it.alias,
                    fullname = if (it.fullname?.isEmpty() == true) null else it.fullname,
                    avatarUrl = it.avatarUrl,
                    speciality = if (it.speciality?.isEmpty() == true) null else it.speciality,
                    rating = it.rating,
                    ratingPosition = it.ratingPos,
                    score = it.scoreStats.score,
                    followersCount = it.followStats.followersCount,
                    subscriptionsCount = it.followStats.followCount,
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
                    articlesCount = it.counterStats.postCount,
                    commentsCount = it.counterStats.commentCount,
                    bookmarksCount = it.counterStats.favoriteCount,
                    workPlaces = it.workplace.map { User.WorkPlace(it.title, it.alias) },
                    relatedData = it.relatedData?.let{ User.RelatedData(it.isSubscribed)}
                )
            }

            return result
        }

        fun whoIs(
            alias: String,
            args: Map<String, String>? = null,
        ): User.WhoIs? {
            val raw = _whoIs("users/$alias/whois", args)

            raw?.let {
                return User.WhoIs(
                    aboutHtml = it.aboutHtml,
                    badges = it.badgets.map { User.WhoIs.Badge(it.title, it.description) },
                    invite = it.invitedBy?.let { User.WhoIs.Invite(it.issuerLogin, it.timeCreated) },
                    contacts = it.contacts.map { User.WhoIs.Contact(it.title, it.url, it.favicon) }
                )
            }
            return null
        }

        fun note(
            alias: String,
            args: Map<String, String>? = null
        ): User.Note? {
            val raw = _note("users/$alias/note", args)

            raw?.let {
                return User.Note(it.text)
            }
            return null
        }

        /**
         * Subscribe/unsubscribe to user.
         * @return subscription status
         * @throws UnsupportedOperationException
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
    data class Note(
        var text: String?
    )

    @Serializable
    data class WhoIs(
        var alias: String,
        var badgets: List<Badget>,
        var aboutHtml: String,
        var contacts: List<Contact>,
        var invitedBy: InvitedBy? = null
    )


    @Serializable
    data class Badget(
        var title: String,
        var description: String,
        var url: String? = null,
        var isRemovable: Boolean
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
        var timeCreated: String
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

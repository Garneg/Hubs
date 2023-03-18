package com.garnegsoft.hubs.api.user

import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.utils.formatBirthdate
import com.garnegsoft.hubs.api.utils.formatTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class UserController {
    companion object{
        private fun getRaw(path: String, args: Map<String, String>? = null): UserProfileData?{
            var response = HabrApi.get(path, args)

            var result: UserProfileData? = null

            response.body?.let {
                var customJson = Json { ignoreUnknownKeys = true }


                result = customJson.decodeFromJsonElement(customJson.parseToJsonElement(it.string()))

                result?.let {
                    if (it.avatarUrl != null)
                        it.avatarUrl = "https:" + it.avatarUrl!!.replace("habrastorage", "hsto")

                    it.registerDateTime = formatTime(it.registerDateTime).split(' ').run { "${this[0]} ${this[1]} ${this[2]}" }

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
                result = User(
                    alias = it.alias,
                    fullname = if(it.fullname?.isEmpty() == true) null else it.fullname,
                    avatarUrl = it.avatarUrl,
                    speciality = if(it.speciality?.isEmpty() == true) null else it.speciality,
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
                    location = "Not implemented yet",
                    workPlaces = listOf(),
                    whoIs = null,
                    occupation = null,
                    note = null,
                    articlesCount = it.counterStats.postCount,
                    commentsCount = it.counterStats.commentCount,
                    favoritesCount = it.counterStats.favoriteCount
                )
            }
            
            return result
        }
    }
}
@Serializable
data class UserProfileData (
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
){
    @Serializable
    data class RelatedData(var isSubscribed: Boolean)
}

@Serializable
data class CounterStats (
    var postCount: Int,
    var commentCount: Int,
    var favoriteCount: Int
)

@Serializable
data class FollowStats (
    val followCount: Int,
    val followersCount: Int
)

@Serializable
data class Location (
    val city: LocationItem?,
    val region: LocationItem?,
    val country: LocationItem?
)

@Serializable
data class LocationItem (
    val id: String,
    val title: String
)

@Serializable
data class ScoreStats (
    val score: Int,
    val votesCount: Int
)

@Serializable
data class WorkPlace(
    val title: String,
    val alias: String
)

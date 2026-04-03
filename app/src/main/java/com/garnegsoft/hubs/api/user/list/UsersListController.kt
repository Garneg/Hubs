package com.garnegsoft.hubs.api.user.list

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.utils.processUserAvatar
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class UsersListController {
    companion object {

        private fun getOriginalData(path: String, args: Map<String, String>? = null): AuthorsList? {
            var response = HabrApi.get(path, args)

            if (response?.code != 200)
                return null

            var authorsList: AuthorsList? = null

            if (response.body != null) {
                var bodyAsString = response.body!!.string()
                    .replace("userIds", "authorIds")
                    .replace("userRefs", "authorRefs")
                var element = Json.parseToJsonElement(bodyAsString)

                var customJson = Json { ignoreUnknownKeys = true }
                authorsList = customJson.decodeFromJsonElement<AuthorsList>(element)

                authorsList.authorRefs.entries.forEach {
                    it.value.avatarUrl = processUserAvatar(it.value.avatarUrl, it.value.alias)
                }
            }
            return authorsList
        }

        fun get(path: String, args: Map<String, String>? = null): HabrList<UserSnippet>? {

            var originalUsersList = getOriginalData(path, args)
            if (originalUsersList == null)
                return null
            var usersSnippetsArray = ArrayList<UserSnippet>().apply {
                originalUsersList.authorIds.forEach {
                    var rawUserData = originalUsersList.authorRefs.get(it)!!
                    add(
                        UserSnippet(
                            id = rawUserData.id.toInt(),
                            alias = rawUserData.alias,
                            fullname = rawUserData.fullname,
                            avatarUrl = rawUserData.avatarUrl,
                            speciality = rawUserData.speciality,
                            score = rawUserData.scoreStats.score,
                            rating = rawUserData.rating,
                            investment = rawUserData.invest
                        )
                    )
                }

            }

            return HabrList(usersSnippetsArray, originalUsersList.pagesCount)
        }

        /**
         * Return list of users that were added to blocklist.
         * Will work only if user authenticated
         */
        fun getBlockedByUser(page: Int = 1, perPage: Int = 20): BlockedUsersList? {

            val response = HabrApi.get("me/blacklist", args = mapOf("page_num" to page.toString(), "per_page" to perPage.toString())) // It seems like newcomer had written this endpoint since naming of the parameters changed to snake_case instead of camelCase used earlier by other ones

            return response?.let {
                it.body?.string()?.let {
                    HabrDataParser.parseJson<BlockedUsersList>(it).apply {
                        authorRefs.forEach {
                            it.value.avatarUrl = processUserAvatar(it.value.avatarUrl, it.value.alias)
                        }
                    }
                } ?: throw UnsupportedOperationException("User is not authenticated!")

            }

        }

        fun getListOfBlockedByUser(page: Int = 1, perPage: Int = 20): List<BlockedUser>? {
            val response = getBlockedByUser(page, perPage)


            return response?.let { list ->
                val mutableList = mutableListOf<BlockedUser>()
                list.authorIds.forEach { id ->
                    mutableList.add(list.authorRefs[id]!!)
                }
                mutableList
            }
        }
    }


    @Serializable
    data class AuthorsList(
        var authorIds: ArrayList<String>,
        var authorRefs: Map<String, AuthorsListEntity>,
        var pagesCount: Int
    )

    @Serializable
    data class AuthorsListEntity(
        var id: String,
        var alias: String,
        var fullname: String? = null,

        var avatarUrl: String? = null,

        var speciality: String? = null,
        var scoreStats: AuthorsListScoreStats,
        var rating: Float,
        val invest: Float? = null,
    )

    @Serializable
    data class AuthorsListScoreStats(
        val score: Int,
        val votesCount: Int
    )



    @Serializable
    data class BlockedUsersList(
        var authorIds: ArrayList<String>,
        var authorRefs: Map<String, BlockedUser>,
        var pagesCount: Int
    )

    @Serializable
    data class BlockedUser(
        @SerialName("login")
        var alias: String,
        var avatarUrl: String?
    )

}




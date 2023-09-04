package com.garnegsoft.hubs.api.me

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.serialization.Serializable


class MeController {
    companion object {
        private fun get(): Me? {
            val response = HabrApi.get("me")
            if (response?.code != 200)
                return null
            response.body?.string()?.let {
                if (it == "null") return null
                val me = HabrDataParser.parseJson<Me>(it)
                me.avatarUrl = me.avatarUrl?.let {
                    it.replace("//habrastorage", "https://hsto")
                } ?: placeholderAvatarUrl(me.alias)
                return me
            }
            return null
        }

        fun getMe(): com.garnegsoft.hubs.api.me.Me? {
            val raw = get()

            raw?.let {
                return Me(
                    alias = it.alias,
                    avatarUrl = it.avatarUrl
                )
            }

            return null
        }

    }

    // I comment those fields just in case habr api will change. I don't actually need them for now, but
    // may be in future they will be useful, so i can't just delete them
    @Serializable
    private data class Me(
        var id: String,
        var alias: String,
//        var fullname: String?,
        var avatarUrl: String?,
//        var groups: List<String>,
//        var settings: Settings,
//        var crc32: String,
//        var gaUid: String,
//        var availableInvitesCount: Int,
//        var email: String,
//        var scoreStats: ScoreStats,
//        var unreadConversationCount: Int,
//        var notificationUnreadCounters: NotificationUnreadCounters,
    )

    @Serializable
    private data class NotificationUnreadCounters(
        var publicationComments: Int,
        var publicationCommentsByAuthor: Int,
        var total: Int,
        var subscribers: Int,
        var mentions: Int,
        var system: Int,
        var applications: Int
    )

    @Serializable
    private data class ScoreStats(
        val score: Int,
        val votesCount: Int
    )

    @Serializable
    private data class Settings(
        var miscSettings: MiscSettings,
        var langSettings: LangSettings,
        var chargeSettings: ChargeSettings,
        var permissionSettings: PermissionSettings?
    )

    @Serializable
    private data class ChargeSettings(
        var postVoteCount: Int,
        var commentVoteCount: Int
    )

    @Serializable
    private data class LangSettings(
        var hl: String,
        var fl: String
    )

    @Serializable
    private data class MiscSettings(
        var viewCommentsRefresh: Boolean,
        var enableShortcuts: Boolean,
        var hideAdv: Boolean,
        var digestSubscription: String?,
        var useMarkdown: Boolean,
        var useMarkdownInComments: Boolean,
        var useMarkdownInArticles: Boolean,
        var useMarkdownInPosts: Boolean
    )

    @Serializable
    private data class PermissionSettings(
        var canAddComplaints: Boolean,
        var canCreateVoices: Boolean
    )
}



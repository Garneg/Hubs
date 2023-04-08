package com.garnegsoft.hubs.api.me

import android.util.Log
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import kotlinx.serialization.Serializable


class MeController {
    companion object {
        private fun get(): Me? {
            val response = HabrApi.get("me")
            if (response.code != 200)
                return null
            response.body?.string()?.let {
                if (it == "null") return null
                val me = HabrDataParser.parseJson<Me>(it)
                me.avatarUrl?.let {
                    me.avatarUrl = it.replace("//habrastorage", "https://hsto")
                }
                return me
            }
            return null
        }

        fun getMe(): com.garnegsoft.hubs.api.me.Me? {
            val raw = get()

            raw?.let {
                return com.garnegsoft.hubs.api.me.Me(
                    alias = it.alias,
                    avatarUrl = it.avatarUrl
                )
            }

            return null
        }

    }

    @Serializable
    private data class Me(
        var id: String,
        var alias: String,
        var fullname: String?,
        var avatarUrl: String?,
        var groups: List<String>,
        var settings: Settings,
        var crc32: String,
        var gaUid: String,
        var availableInvitesCount: Long,
        var email: String,
        var scoreStats: ScoreStats,
        var unreadConversationCount: Long,
        var notificationUnreadCounters: NotificationUnreadCounters,
    )

    @Serializable
    private data class NotificationUnreadCounters(
        var posts_and_comments: Long,
        var subscribers: Long,
        var mentions: Long,
        var system: Long,
        var applications: Long
    )

    @Serializable
    private data class ScoreStats(
        val score: Long,
        val votesCount: Long
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
        var postVoteCount: Long,
        var commentVoteCount: Long
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



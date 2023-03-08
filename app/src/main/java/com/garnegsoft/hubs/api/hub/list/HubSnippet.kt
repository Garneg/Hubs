package com.garnegsoft.hubs.api.hub.list

import com.garnegsoft.hubs.api.HabrSnippet


data class HubSnippet(

    override val id: Int,

    val alias: String,

    val title: String,

    val description: String,

    val avatarUrl: String?,

    val tags: List<String>?,

    val statistics: Statistics,

    val isProfiled: Boolean,

    val isOfftop: Boolean

) : HabrSnippet {
    data class Statistics(
        val subscribersCount: Int,
        val rating: Float,
        val authorsCount: Int,
        val postsCount: Int
    )
}
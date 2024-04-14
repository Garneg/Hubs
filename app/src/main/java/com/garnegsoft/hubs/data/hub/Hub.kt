package com.garnegsoft.hubs.data.hub

class Hub(
    val alias: String,
    val title: String,
    val description: String,
    val avatarUrl: String,
    val statistics: Statistics,
    val isProfiled: Boolean,
    val relatedData: RelatedData?
){
    data class Statistics(
        val subscribersCount: Int,
        val rating: Float,
        val authorsCount: Int,
        val postsCount: Int
    )

    data class RelatedData(
        val isSubscribed: Boolean
    )
}
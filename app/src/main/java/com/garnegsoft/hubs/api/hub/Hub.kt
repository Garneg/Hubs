package com.garnegsoft.hubs.api.hub

class Hub (
    val alias: String,
    val title: String,
    val description: String,
    val fullDescription: String,
    val avatarUrl: String,
    val statistics: Statistics,
    val isProfiled: Boolean
){
    data class Statistics(
        val subscribersCount: Int,
        val rating: Float,
        val authorsCount: Int,
        val postsCount: Int
    )

}
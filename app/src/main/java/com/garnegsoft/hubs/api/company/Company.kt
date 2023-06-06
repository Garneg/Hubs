package com.garnegsoft.hubs.api.company


class Company(
    val alias: String,
    val avatarUrl: String?,
    val title: String,
    val description: String?,
    val registrationDate: String,
    val foundationDate: String?,
    val siteUrl: String?,
    val staffNumber: String,
    val habrCareerAlias: String?,
    val location: String?,
    val statistics: Statistics,
    val branding: Branding?,
    val relatedData: RelatedData?
) {
    class Statistics(
        val subscribersCount: Int,
        val rating: Float,
        val postsCount: Int,
        val newsCount: Int,
        val employees: Int,
    )

    class RelatedData(val isSubscribed: Boolean)

    class Branding(
        val bannerImageUrl: String?,
        val bannerLinkUrl: String?,
    )

    data class WhoIs(
        val aboutHtml: String?,
        val sectors: List<Sector>?,

    ){
        data class Sector(
            val title: String,
            val alias: String,
        )
    }
}

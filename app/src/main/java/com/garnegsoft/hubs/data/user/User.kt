package com.garnegsoft.hubs.data.user


class User(
	val alias: String,
	val fullname: String?,
	val avatarUrl: String?,
	val speciality: String?,
	val rating: Float,
	val ratingPosition: Int?,
	val score: Int,
	val articlesCount: Int,
	val commentsCount: Int,
	val bookmarksCount: Int,
	val followersCount: Int,
	val subscriptionsCount: Int,
	val isReadonly: Boolean,
	val registrationDate: String,
	val lastActivityDate: String?,
	val birthday: String?,
	val canBeInvited: Boolean,
	val location: String?,
	val workPlaces: List<WorkPlace>,
	
	val relatedData: RelatedData?
){

    class WhoIs(
        val aboutHtml: String?,
        val badges: List<Badge>,
        val invite: Invite?,
        val contacts: List<Contact>
    ) {
        data class Badge(
            val title: String,
            val description: String
        )
        data class Invite(
            /**
             * Alias of user, that invited. If null, user was invited by UFO
             */
            val inviterAlias: String?,

            val inviteDate: String
        )
        data class Contact(
            val title: String,
            val url: String,
            val faviconUrl: String?
        )
    }

    class Occupation(
        val salary: Int?,
        val currencySign: String?,
        val qualification: String?,
        val specializations: List<String>,
        val skills: List<String>
    )

    /**
     * Represents company where users works
     */
    class WorkPlace(
        val title: String,
        val alias: String
    )

    data class Note(
        val text: String?
    )

    class RelatedData(val isSubscribed: Boolean)
}

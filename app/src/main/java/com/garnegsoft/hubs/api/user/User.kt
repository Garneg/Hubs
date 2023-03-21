package com.garnegsoft.hubs.api.user

import androidx.compose.runtime.Immutable
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet


@Immutable
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
    val favoritesCount: Int,
    val followersCount: Int,
    val followsCount: Int,
    val isReadonly: Boolean,
    val registrationDate: String,
    val lastActivityDate: String?,
    val birthday: String?,
    val canBeInvited: Boolean,
    val location: String?,

    /**
     * Companies where user works
     */
    val workPlaces: List<WorkPlace>,

    /**
     * Some additional info about user
     */
    val whoIs: WhoIs?,

    /**
     * User's job info
     */
    val occupation: Occupation?,

    /**
     * Hubs that user follows
     */
    val followHubs: List<HubSnippet>?,

    /**
     * Companies that user follows
     */
    val followCompanies: List<CompanySnippet>?,

    /**
     * Note about user, get only if app user is authorized, otherwise should be null
     */
    val note: String?,

    val relatedData: RelatedData?
){

    class WhoIs(
        val badges: List<String>,
        val invite: Invite?,
        val contacts: List<Contact>
    ) {
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

    class RelatedData(val isSubscribed: Boolean)
}

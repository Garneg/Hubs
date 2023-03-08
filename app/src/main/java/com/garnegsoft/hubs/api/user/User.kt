package com.garnegsoft.hubs.api.user


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
    val whoIs: WhoIs?,
    val occupation: Occupation?,

    /**
     * Note about user, get only if app user is authorized, else should be null
     */
    val note: String?
){
    class WhoIs(
        val badges: List<String>?

    )

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
}

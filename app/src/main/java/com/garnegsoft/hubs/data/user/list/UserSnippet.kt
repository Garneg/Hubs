package com.garnegsoft.hubs.data.user.list

import com.garnegsoft.hubs.data.HabrSnippet

/**
 * Snippet of user profile, should be used in users(authors) list.
 */
data class UserSnippet(

    override val id: Int,

    val alias: String,

    val fullname: String?,

    val avatarUrl: String?,

    val speciality: String?,

    val score: Int,

    val rating: Float,

    val investment: Float?
) : HabrSnippet
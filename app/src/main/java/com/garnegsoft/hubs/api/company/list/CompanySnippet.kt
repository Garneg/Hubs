package com.garnegsoft.hubs.api.company.list

import com.garnegsoft.hubs.api.HabrSnippet

data class CompanySnippet(

    override val id: Int,

    val alias: String,

    val title: String,

    val avatarUrl: String?,

    val description: String?,

    val statistics: Statistics,
    
    val relatedData: RelatedData?

) : HabrSnippet {
    data class Statistics(
        val rating: Float,

        val investment: Float?,

        val subscribersCount: Int
    )
    
    data class RelatedData(
        val isSubscribed: Boolean
    )
}
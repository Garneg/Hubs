package com.garnegsoft.hubs.api.company

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import com.garnegsoft.hubs.api.user.Location
import com.garnegsoft.hubs.api.utils.formatFoundationDate
import com.garnegsoft.hubs.api.utils.formatTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup

class CompanyController {
    companion object{
        private fun getPrivate(path: String, args: Map<String, String>? = null): CompanyProfile? {
            val response = HabrApi.get(path, args)

            if (response?.code != 200)
                return null

            response.body?.string()?.let {
                val result = HabrDataParser.parseJson<CompanyProfile>(it)
                result.imageUrl?.let {
                    result.imageUrl = "https:$it"
                }
                result.registrationDate = formatTime(result.registrationDate).split(' ').run { "${this[0]} ${this[1]} ${this[2]}" }
                result.descriptionHtml?.let { it1 -> result.descriptionHtml = Jsoup.parse(it1).text() }
                return result
            }
            return null
        }

        fun get(alias: String): Company? {
            val raw = getPrivate("companies/$alias/card")

            var result: Company? = null

            raw?.let {
                result = Company(
                    alias = it.alias,
                    avatarUrl = it.imageUrl,
                    title = it.titleHtml,
                    description = it.descriptionHtml,
                    registrationDate = it.registrationDate,
                    foundationDate = it.foundationDate?.run { formatFoundationDate(day, month, year) },
                    siteUrl = it.siteUrl,
                    staffNumber = it.staffNumber,
                    habrCareerAlias = it.careerAlias,
                    location = it.location?.let { it.country?.title!! },
                    statistics = Company.Statistics(
                        subscribersCount = it.statistics.subscribersCount,
                        rating = it.statistics.rating,
                        postsCount = it.statistics.postsCount,
                        newsCount = it.statistics.newsCount,
                        employees = it.statistics.employeesCount
                    ),
                    relatedData = it.relatedData?.let { Company.RelatedData(it.isSubscribed) }
                )
            }
            return result
        }

        /**
         * Subscribe / unsubscribe to company.
         * @param alias alias of the company
         * @return subscription status (subscribed/unsubscribed)
         * @throws UnsupportedOperationException if request was not succeed (e.g. because user isn't authorized)
         */
        fun subscription(alias: String): Boolean {
            val response = HabrApi.post("companies/$alias/subscription")
            response.body?.string()?.let {
                return Json.parseToJsonElement(it).jsonObject["isSubscribed"]?.jsonPrimitive!!.boolean
            }
            throw UnsupportedOperationException("User is not authorized")

        }
    }
    @Serializable
    private data class CompanyProfile (
        var alias: String,
        var imageUrl: String?,
        var titleHtml: String,
        var descriptionHtml: String?,
        var relatedData: RelatedData? = null,
        var statistics: CompanyStatistics,
        var foundationDate: FoundationDate?,
        var location: Location?,
        var siteUrl: String?,
        var staffNumber: String,
        var registrationDate: String,
        var contacts: List<Contact>,
        var settings: Settings,
        var metadata: Metadata,
//  var al aDeskSettings: Any? = null,
        var careerAlias: String?
    ) {

        @Serializable
        data class RelatedData(var isSubscribed: Boolean)

        @Serializable
        data class Contact(
            var title: String,
            var url: String,
            var siteTitle: String? = null,
            var favicon: String? = null
        )
        @Serializable
        data class FoundationDate(
            val year: String?,
            val month: String?,
            val day: String?
        )


        @Serializable
        data class Metadata(
            var titleHtml: String,
            var title: String,
            var keywords: List<String>,
            var descriptionHtml: String?,
            var description: String?
        )

        @Serializable
        data class RepresentativeUser(
            val alias: String,
            val fullname: String? = null
        )

        @Serializable
        data class Settings(
            val branding: Branding?,
            val status: String,
            val isStartup: Boolean
        )

        @Serializable
        data class Branding(
            var imageUrl: String?,
            var linkUrl: String?,
        )

        @Serializable
        data class CompanyStatistics(
            var subscribersCount: Int,
            var rating: Float,
            var invest: Float? = null,
            var postsCount: Int,
            var newsCount: Int,
            var vacanciesCount: Int,
            var employeesCount: Int,
            var careerRating: Float? = null
        )
    }
}
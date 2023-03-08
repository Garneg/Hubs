package com.garnegsoft.hubs.api.company.list

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jsoup.Jsoup


class CompaniesListController {
    companion object {
        private fun getInternal(path: String, args: Map<String, String>? = null): CompaniesList? {
            val response = HabrApi.get(path, args)

            var result: CompaniesList? = null

            response.body?.let {
                var customJson = Json { ignoreUnknownKeys = true }
                result = customJson.decodeFromJsonElement<CompaniesList>(customJson.parseToJsonElement(it.string()))
                result?.companyRefs?.forEach{
                    it.value.imageUrl?.let { _ ->
                        it.value.imageUrl = "https://" + it.value.imageUrl
                    }
                    it.value.descriptionHtml =
                        it.value.descriptionHtml?.let { it1 -> Jsoup.parse(it1).text() }
                    it.value.titleHtml = Jsoup.parse(it.value.titleHtml).text()
                }
            }

            return result
        }

        fun get(path: String, args: Map<String, String>? = null): HabrList<CompanySnippet>? {
            var originalList = getInternal(path, args)

            if (originalList == null)
                return null

            var companiesList = ArrayList<CompanySnippet>()
            originalList.companyIds.forEach {
                originalList.companyRefs.get(it)?.let {
                    companiesList.add(
                        CompanySnippet(
                            id = it.id.toInt(),
                            alias = it.alias,
                            title = it.titleHtml,
                            avatarUrl = it.imageUrl,
                            description = it.descriptionHtml,
                            statistics = CompanySnippet.Statistics(
                                rating = it.statistics.rating,
                                investment = it.statistics.invest,
                                subscribersCount = it.statistics.subscribersCount
                            )
                        )
                    )
                }
            }

            return HabrList(companiesList, originalList.pagesCount)

        }
    }

}

@Serializable
data class CompaniesList (
    var pagesCount: Int,

    var companyIds: List<String>,

    var companyRefs: Map<String, CompanyRef>
)

@Serializable
data class CompanyRef (
    var id: String,
    var alias: String,
    var titleHtml: String,
    var descriptionHtml: String? = null,
    var imageUrl: String?,
//    ral relatedData: Any? = null,
    var statistics: CompaniesStatistics,
    var commonHubs: List<CommonHub>
)

@Serializable
data class CommonHub (
    var id: String,
    var alias: String,
    var type: String,
    var title: String,
    var titleHtml: String,
    var isProfiled: Boolean
)

@Serializable
data class CompaniesStatistics (
    var subscribersCount: Int,
    var rating: Float,
    var invest: Float? = null
)

package com.garnegsoft.hubs.api.utils

// list of patterns paired with group index of id
// TODO: Rewrite with regex non-including groups to remove indexes
class SearchUrlHandler {
    companion object {

        private val articlePatterns = listOf(
            //https://habr.com/ru/news/886998/
            """https://habr.com/([^/]+)/([^/]+)/(\d+)(?:/|${'$'})""" to 3,

            //https://habr.com/p/845748
            """https://habr.com/([^/]+)/(\d+)(?:/|${'$'})""" to 2,

            //https://habr.com/company/ruvds/blog/42069
            """https://habr.com/([^/]+)/([^/]+)/([^/]+)/(\d+)(?:/|${'$'})""" to 4,

            //https://habr.com/ru/companies/{company}/{type}/{id}
            """https://habr.com/([^/]+)/([^/]+)/([^/]+)/([^/]+)/(\d+)(?:/|${'$'})""" to 5
        )

        /**
         * Method to retrieve id from url that supposed to contain it
         * @return Id that url contains or 0 if path does not match not a single supported pattern
         */
        fun getArticleIdFromUrl(url: String): Int? =
            findMatches(url, articlePatterns)?.toInt()


        private val userPatterns = listOf(
            //https://habr.com/ru/users/BincomAD/  <-- example
            "https://habr.com/([^/]+)/users/([^/]+)(?:/|${'$'})" to 2,

            //https://habr.com/users/Garneg/  <-- example
            "https://habr.com/users/([^/]+)(?:/|${'$'})" to 1,
        )

        fun getUserAliasFromUrl(url: String): String? =
            findMatches(url, userPatterns)


        private val companyPatterns = listOf(
            //https://habr.com/ru/companies/yadro/profile/  <-- example
            "https://habr.com/([^/]+)/companies/([^/]+)/([^/]+)(?:/|${'$'})" to 2,

            "https://habr.com/companies/([^/]+)/([^/]+)(?:/|${'$'})" to 1,
        )

        fun getCompanyAliasFromUrl(url: String): String? =
            findMatches(url, companyPatterns)


        private val hubPatterns = listOf(
            "https://habr.com/([^/]+)/(?:hubs?)/([^/]+)(?:/|${'$'})" to 2,

            "https://habr.com/(?:hubs?)/([^/]+)(?:/|${'$'})" to 2,
        )


        private fun findMatches(url: String, patterns: List<Pair<String, Int>>): String? {
            patterns.forEach { pair ->
                val regex = Regex(pair.first)
                regex.find(url)?.let { result ->
                    return result.groupValues[pair.second]
                }
            }
            return null
        }

        // TODO: Make it more efficient
        fun recongnizeUrlDataTypeAndIdentifier(url: String): Pair<UrlDataType, String?> {
            val patternsList = listOf(
                articlePatterns to UrlDataType.Article,
                userPatterns to UrlDataType.User,
                companyPatterns to UrlDataType.Company,
                hubPatterns to UrlDataType.Hub
            )


            patternsList.forEach { patterns ->
                findMatches(url, patterns.first)?.let {
                    return patterns.second to it
                }
            }
            return UrlDataType.Unknown to null
        }

    }

    /**
     * Represents type of content that url refers at
     */
    enum class UrlDataType {
        Unknown,
        Article,
        User,
        Hub,
        Company,
        // todo: Comment
    }
}



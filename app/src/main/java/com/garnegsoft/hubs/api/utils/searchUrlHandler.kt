package com.garnegsoft.hubs.api.utils

// list of patterns paired with group index of id

/**
 *
 */
val patterns = listOf(
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
fun getArticleIdFromUrl(url: String): Int {
    patterns.forEach { pair ->
        val regex = Regex(pair.first)
        regex.find(url)?.let { result ->
            return result.groupValues[pair.second].toInt()
        }
    }
    return 0
}


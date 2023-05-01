package com.garnegsoft.hubs

import androidx.navigation.NavDeepLink


val UserScreenNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/{lang}/users/{alias}"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/{page}"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/{page}/"),
    NavDeepLink("https://habr.com/users/{alias}"),
    NavDeepLink("https://habr.com/users/{alias}/"),
    )

val HubScreenNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/{lang}/hub/{alias}"),
    NavDeepLink("https://habr.com/{lang}/hub/{alias}/"),
    NavDeepLink("https://habr.com/{lang}/hub/{alias}/{page}"),
    NavDeepLink("https://habr.com/{lang}/hub/{alias}/{page}/"),
    )

val CompanyScreenNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/{lang}/companies/{alias}"),
    NavDeepLink("https://habr.com/{lang}/companies/{alias}/"),
    NavDeepLink("https://habr.com/{lang}/companies/{alias}/{page}"),
    NavDeepLink("https://habr.com/{lang}/companies/{alias}/{page}/"),
    )

val CommentsScreenNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/ru/articles/{postId}/#comment_{commentId}"),
    NavDeepLink("https://habr.com/ru/articles/{postId}/#comment_{commentId}/"),
    NavDeepLink("https://habr.com/ru/companies/{company}/articles/{postId}/comments/#comment_{commentId}"),
    NavDeepLink("https://habr.com/ru/companies/{company}/articles/{postId}/comments/#comment_{commentId}/"),
    NavDeepLink("https://habr.com/ru/companies/{company}/articles/{postId}/comments/"),
    NavDeepLink("https://habr.com/ru/articles/{postId}/"),
)

val ArticleNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/p/{id}"),
    NavDeepLink("https://habr.com/p/{id}/"),
    NavDeepLink("https://habr.com/{lang}/post/{id}"),
    NavDeepLink("https://habr.com/{lang}/post/{id}/"),
    NavDeepLink("https://{domain}/article/{id}"),
    NavDeepLink("https://{domain}/article/{id}/"),
    NavDeepLink("https://{domain}/company/{company}/blog/{id}"),
    NavDeepLink("https://{domain}/company/{company}/blog/{id}/"),
    NavDeepLink("https://habr.com/{lang}/news/t/{id}"),
    NavDeepLink("https://habr.com/{lang}/news/t/{id}/"),
    NavDeepLink("https://habr.com/{lang}/news/{id}"),
    NavDeepLink("https://habr.com/{lang}/news/{id}/"),
    NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}"),
    NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}/"),
    NavDeepLink("https://habr.com/{lang}/article/{id}"),
    NavDeepLink("https://habr.com/{lang}/article/{id}/"),
    NavDeepLink("https://habr.com/{lang}/articles/{id}"),
    NavDeepLink("https://habr.com/{lang}/articles/{id}/"),
    NavDeepLink("https://habr.com/{lang}/amp/publications/{id}"),
    NavDeepLink("https://habr.com/{lang}/amp/publications/{id}/")

)


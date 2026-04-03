package com.garnegsoft.hubs

import androidx.navigation.navDeepLink

val domain = "https://habr.com"

val UserScreenNavDeepLinks = listOf(
    navDeepLink { uriPattern = "https://habr.com/{lang}/users/{alias}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/users/{alias}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/users/{alias}/{page}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/users/{alias}/{page}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/users/{alias}/{page}/{subPage}" },
    navDeepLink { uriPattern = "https://habr.com/users/{alias}" },
    navDeepLink { uriPattern = "https://habr.com/users/{alias}/" },
    navDeepLink { uriPattern = "https://habr.com/users/{alias}/{page}" },
    navDeepLink { uriPattern = "https://habr.com/users/{alias}/{page}/" },
    navDeepLink { uriPattern = "http://habrahabr.ru/users/{alias}" },
    navDeepLink { uriPattern = "http://habrahabr.ru/users/{alias}/" },
    )

val HubScreenNavDeepLinks = listOf(
    navDeepLink { uriPattern = "https://habr.com/{lang}/hub/{alias}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/hub/{alias}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/hub/{alias}/{page}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/hub/{alias}/{page}/" },
)

val CompanyScreenNavDeepLinks = listOf(
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{alias}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{alias}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{alias}/{page}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{alias}/{page}/" },
)

val CommentsScreenNavDeepLinks = listOf(
    navDeepLink { uriPattern = "https://habr.com/{lang}/articles/{postId}/comments/#comment_{commentId}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/articles/{postId}/comments/#comment_{commentId}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/#comment_{commentId}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/#comment_{commentId}/" },
//        NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/#comment_{commentId}"),
//        NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/#comment_{commentId}/"),
    navDeepLink { uriPattern = "https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/" },
    navDeepLink { uriPattern = "https://habr.com/p/{postId}/comments/#comment_{commentId}" },
    navDeepLink { uriPattern = "https://habr.com/p/{postId}/comments/#comment_{commentId}/" },
    navDeepLink { uriPattern = "http://habrahabr.ru/post/{postId}/#comment_{commentId}" },
    navDeepLink { uriPattern = "http://habrahabr.ru/post/{postId}/#comment_{commentId}/" },
)
//https://habr.com/ru/news/753128/comments/#comment_25835578

val ArticleNavDeepLinks = listOf(
    navDeepLink { uriPattern = "https://habr.com/p/{id}" },
    navDeepLink { uriPattern = "https://habr.com/p/{id}/" },
    navDeepLink { uriPattern = "http://habrahabr.ru/post/{id}" },
    navDeepLink { uriPattern = "http://habrahabr.ru/post/{id}/" },
    navDeepLink { uriPattern = "http://habrahabr.ru/company/{company}/blog/{id}" },
    navDeepLink { uriPattern = "http://habrahabr.ru/company/{company}/blog/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/post/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/post/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/article/{id}" },
    navDeepLink { uriPattern = "https://habr.com/article/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/company/{company}/blog/{id}" },
    navDeepLink { uriPattern = "https://habr.com/company/{company}/blog/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/news/t/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/news/t/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/news/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/news/{id}/" },
    //NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}"),
    navDeepLink { uriPattern = "https://habr.com/ru/companies/{company}/{type}/{id}" },
    navDeepLink { uriPattern = "https://habr.com/ru/companies/{company}/{type}/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/article/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/article/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/articles/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/articles/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/amp/publications/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/amp/publications/{id}/" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/specials/{id}" },
    navDeepLink { uriPattern = "https://habr.com/{lang}/specials/{id}/" },
    navDeepLink { uriPattern = "hubs://article/{id}" }
)




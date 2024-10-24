package com.garnegsoft.hubs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState

val domain = "https://habr.com"

val UserScreenNavDeepLinks = listOf(
    NavDeepLink("https://habr.com/{lang}/users/{alias}"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/{deepLinkPage}"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/{deepLinkPage}/"),
    NavDeepLink("https://habr.com/{lang}/users/{alias}/{deepLinkPage}/{subPage}"),
    NavDeepLink("https://habr.com/users/{alias}"),
    NavDeepLink("https://habr.com/users/{alias}/"),
    NavDeepLink("https://habr.com/users/{alias}/{deepLinkPage}"),
    NavDeepLink("https://habr.com/users/{alias}/{deepLinkPage}/"),
    navDeepLink { uriPattern = "http://habrahabr.ru/users/{alias}" },
    navDeepLink { uriPattern = "http://habrahabr.ru/users/{alias}/" },
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
    NavDeepLink("https://habr.com/{lang}/articles/{postId}/comments/#comment_{commentId}"),
    NavDeepLink("https://habr.com/{lang}/articles/{postId}/comments/#comment_{commentId}/"),
    NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/#comment_{commentId}"),
    NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/#comment_{commentId}/"),
//        NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/#comment_{commentId}"),
//        NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/#comment_{commentId}/"),
    NavDeepLink("https://habr.com/{lang}/companies/{company}/articles/{postId}/comments/"),
    NavDeepLink("https://habr.com/p/{postId}/comments/#comment_{commentId}"),
    NavDeepLink("https://habr.com/p/{postId}/comments/#comment_{commentId}/"),
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
    NavDeepLink("https://{domain}/{lang}/post/{id}"),
    NavDeepLink("https://{domain}/{lang}/post/{id}/"),
    NavDeepLink("https://{domain}/article/{id}"),
    NavDeepLink("https://{domain}/article/{id}/"),
    NavDeepLink("https://{domain}/company/{company}/blog/{id}"),
    NavDeepLink("https://{domain}/company/{company}/blog/{id}/"),
    NavDeepLink("https://habr.com/{lang}/news/t/{id}"),
    NavDeepLink("https://habr.com/{lang}/news/t/{id}/"),
    NavDeepLink("https://habr.com/{lang}/news/{id}"),
    NavDeepLink("https://habr.com/{lang}/news/{id}/"),
    //NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}"),
    NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}/"),
    NavDeepLink("https://habr.com/{lang}/article/{id}"),
    NavDeepLink("https://habr.com/{lang}/article/{id}/"),
    NavDeepLink("https://habr.com/{lang}/articles/{id}"),
    NavDeepLink("https://habr.com/{lang}/articles/{id}/"),
    NavDeepLink("https://habr.com/{lang}/amp/publications/{id}"),
    NavDeepLink("https://habr.com/{lang}/amp/publications/{id}/"),
    NavDeepLink("https://habr.com/{lang}/specials/{id}"),
    NavDeepLink("https://habr.com/{lang}/specials/{id}/"),
)




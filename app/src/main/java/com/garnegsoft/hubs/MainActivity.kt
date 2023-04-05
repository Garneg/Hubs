package com.garnegsoft.hubs

import ArticleController
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.garnegsoft.hubs.api.DataStoreKeys
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.NoConnectionInterceptor
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
import com.garnegsoft.hubs.ui.screens.article.ArticleScreenViewModel
import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
import com.garnegsoft.hubs.ui.screens.hub.HubScreen
import com.garnegsoft.hubs.ui.screens.main.ArticlesScreen
import com.garnegsoft.hubs.ui.screens.search.SearchScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreen
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import android.webkit.CookieManager
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import androidx.navigation.NavDeepLink
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu

var cookies: String = ""
var authorized: Boolean = false

val Context.authDataStore by preferencesDataStore("auth")
val Context.settingsDataStore by preferencesDataStore("settings")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().removeAllCookies(null)
        val cookiesFlow = authDataStore.data.map { it.get(DataStoreKeys.Auth.Cookies) ?: "" }
        val isAuthorizedFlow = authDataStore.data.map { it[DataStoreKeys.Auth.Authorized] ?: false }


        runBlocking {
            cookies = cookiesFlow.first()
            authorized = isAuthorizedFlow.first()

        }

        val authActivityLauncher =
            registerForActivityResult(AuthActivityResultContract()) { result ->
                lifecycle.coroutineScope.launch {
                    authDataStore.edit {
                        it[DataStoreKeys.Auth.Cookies] = result ?: ""
                        it[DataStoreKeys.Auth.Authorized] = true
                        authorized = true
                        cookies = result ?: ""
                    }
                }

            }


        intent.dataString?.let { Log.e("intentData", it) }
        HabrApi.HttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor {
                val req = it.request()
                    .newBuilder()
                    .addHeader("Cookie", cookies)
                    .build()
                it.proceed(req)
            })
            .addInterceptor(NoConnectionInterceptor(this))
            .build()

        setContent {
            HubsTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "articles",
                    builder = {
                        composable("articles") {
                            ArticlesScreen(
                                viewModelStoreOwner = it,
                                onSearchClicked = { navController.navigate("search") },
                                onArticleClicked = {
                                    if (!navController.currentBackStackEntry!!.destination.route!!.contains(
                                            "article/"
                                        )
                                    )
                                        navController.navigate("article/$it")
                                },
                                onCommentsClicked = {
                                    navController.navigate("comments/$it")
                                },
                                onUserClicked = {
                                    navController.navigate("user/$it")
                                },
                                onCompanyClicked = {
                                    navController.navigate("company/$it")
                                },
                                onHubClicked = {
                                    navController.navigate("hub/$it")
                                },
                                menu = { AuthorizedMenu() }
                            )
                        }
                        composable(
                            route = "article/{id}",
                            deepLinks = listOf(
                                NavDeepLink("https://habr.com/{lang}/post/{id}"),
                                NavDeepLink("https://habr.com/{lang}/post/{id}/"),
                                NavDeepLink("https://habrahabr.ru/article/{id}"),
                                NavDeepLink("https://habrahabr.ru/article/{id}/"),
                                NavDeepLink("https://habrahabr.ru/company/{company}/blog/{id}"),
                                NavDeepLink("https://habrahabr.ru/company/{company}/blog/{id}/"),
                                NavDeepLink("https://habr.com/{lang}/news/t/{id}"),
                                NavDeepLink("https://habr.com/{lang}/news/t/{id}/"),
                                NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}"),
                                NavDeepLink("https://habr.com/{lang}/{companies}/{company}/{type}/{id}/"),
                                NavDeepLink("https://habr.com/p/{id}"),
                                NavDeepLink("https://habr.com/p/{id}/")
                            )
                        ) {

                            var articleViewModel = viewModel<ArticleScreenViewModel>(it)
                            val article by articleViewModel.article.observeAsState()
                            if (article != null) {

                                ArticleScreen(
                                    article = article!!,
                                    onBackButtonClicked = { navController.navigateUp() },
                                    onCommentsClicked = {
                                        navController.navigate("comments/${article!!.id}")
                                    },
                                    onAuthorClicked = {
                                        navController.navigate("user/${article!!.author?.alias}")
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    launch(Dispatchers.IO) {
                                        ArticleController.get(
                                            "articles/${
                                                it.arguments!!.getString("id")
                                            }"
                                        )?.let {
                                            launch(Dispatchers.Main) {
                                                articleViewModel.article.value = it
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        composable(route = "search") {
                            SearchScreen(
                                viewModelStoreOwner = it,
                                onArticleClicked = { navController.navigate("article/$it") },
                                onUserClicked = { navController.navigate("user/$it") },
                                onHubClicked = { navController.navigate("hub/$it") },
                                onCompanyClicked = { navController.navigate("company/$it") },
                                onCommentsClicked = { navController.navigate("comments/$it") },
                                onBackClicked = { navController.navigateUp() }
                            )
                        }

                        composable("comments/{postId}") {
                            val postId = it.arguments!!.getString("postId")!!
                            CommentsScreen(
                                viewModelStoreOwner = it,
                                parentPostId = postId.toInt(),
                                onBackClicked = { navController.navigateUp() },
                                onArticleClicked = { navController.navigate("article/$postId") },
                                onUserClicked = { navController.navigate("user/$it") }
                            )

                        }

                        composable(
                            "user/{alias}",
//                            deepLinks = listOf(
//                                NavDeepLink("https://habr.com/{lang}/users/{alias}"),
//                                NavDeepLink("https://habr.com/{lang}/users/{alias}/"),
//                                NavDeepLink("https://habr.com/{lang}/users/{alias}/{page}"),
//                                NavDeepLink("https://habr.com/{lang}/users/{alias}/{page}/"),
//
//                                )
                        ) {
                            var user: User? by remember { mutableStateOf(null) }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    user =
                                        UserController.get("users/${it.arguments!!.getString("alias")}/card")

                                }
                            })

                            if (user != null) {
                                UserScreen(
                                    user = user!!,
                                    onBack = { navController.popBackStack() },
                                    onArticleClicked = { navController.navigate("article/$it") },
                                    onUserClicked = { navController.navigate("user/$it") },
                                    onCommentsClicked = { navController.navigate("comments/$it") },
                                    onCommentClicked = { a, e -> },
                                    viewModelStoreOwner = it
                                )
                            }

                        }

                        composable(
                            "hub/{alias}",
                            deepLinks = listOf(
                                NavDeepLink("https://habr.com/{lang}/hub/{alias}"),
                                NavDeepLink("https://habr.com/{lang}/hub/{alias}/"),
                                NavDeepLink("https://habr.com/{lang}/hub/{alias}/{page}"),
                                NavDeepLink("https://habr.com/{lang}/hub/{alias}/{page}/"),

                                )
                        ) {
                            val alias = it.arguments?.getString("alias")
                            HubScreen(alias = alias!!, viewModelStoreOwner = it,
                                onBackClick = { navController.popBackStack() },
                                onArticleClick = { navController.navigate("article/$it") },
                                onCompanyClick = { navController.navigate("company/$it") },
                                onUserClick = { navController.navigate("user/$it") },
                                onCommentsClick = { navController.navigate("comments/$it") }
                            )
                        }
                        composable(
                            "company/{alias}",
                            deepLinks = listOf(
                                NavDeepLink("https://habr.com/{lang}/companies/{alias}"),
                                NavDeepLink("https://habr.com/{lang}/companies/{alias}/"),
                                NavDeepLink("https://habr.com/{lang}/companies/{alias}/{page}"),
                                NavDeepLink("https://habr.com/{lang}/companies/{alias}/{page}/"),

                            )
                        ) {
                            val alias = it.arguments?.getString("alias")!!
                            CompanyScreen(
                                viewModelStoreOwner = it,
                                alias = alias,
                                onBack = { navController.popBackStack() },
                                onArticleClick = { navController.navigate("article/$it") },
                                onCommentsClick = { navController.navigate("comments/$it") },
                                onUserClick = { navController.navigate("user/$it") }
                            )
                        }

                    })
            }
        }

        Log.e("ExternalLink", intent.data.toString())

    }

}



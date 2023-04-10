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
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.navigation
import com.garnegsoft.hubs.api.me.Me
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages

var cookies: String = ""
var authorized: Boolean = false

val Context.authDataStore by preferencesDataStore("auth")
val Context.settingsDataStore by preferencesDataStore("settings")

fun <T> Context.authDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
    return authDataStore.data.map { it[key] }
}


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        CookieManager.getInstance().removeAllCookies(null)
        val cookiesFlow = authDataStore.data.map { it.get(DataStoreKeys.Auth.Cookies) ?: "" }
        val isAuthorizedFlow = authDataStore.data.map { it[DataStoreKeys.Auth.Authorized] ?: false }
        lifecycle.coroutineScope.launch {
            cookiesFlow
                .collect {
                    cookies = it
                }

        }


        runBlocking {
            authorized = isAuthorizedFlow.first()

        }

        val authActivityLauncher =
            registerForActivityResult(AuthActivityResultContract()) { result ->
                CookieManager.getInstance().removeAllCookies(null)
                lifecycle.coroutineScope.launch {
                    result?.let {
                        authDataStore.edit {
                            it[DataStoreKeys.Auth.Cookies] = result
                            it[DataStoreKeys.Auth.Authorized] = true
                            authorized = true
                            //cookies = result
                        }
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
            var userInfo: Me? by remember { mutableStateOf(null) }
            val userInfoUpdateBlock = remember {
                {
                    userInfo = MeController.getMe()
                    Log.e("userInfoUpdateBlock", userInfo.toString())
                }
            }
            LaunchedEffect(
                key1 = isAuthorizedFlow.collectAsState(initial = false).value,
                block = {
                    launch(Dispatchers.IO, block = { userInfoUpdateBlock() })
                })
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
                                menu = {
                                    val authorizedMenu by isAuthorizedFlow.collectAsState(initial = false)
                                    if (authorizedMenu && userInfo != null) {
                                        AuthorizedMenu(
                                            userAlias = userInfo!!.alias,
                                            avatarUrl = userInfo!!.avatarUrl,
                                            onProfileClick = { navController.navigate("user/${userInfo!!.alias}") },
                                            onArticlesClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Articles}") },
                                            onCommentsClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Comments}") },
                                            onBookmarksClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Bookmarks}") },
                                            onAboutClick = { navController.navigate("about") }
                                        )
                                    } else {

                                        UnauthorizedMenu(
                                            onLoginClick = {
                                                authActivityLauncher.launch(Unit)
                                                lifecycle.coroutineScope.launch(Dispatchers.IO) { userInfoUpdateBlock() }
                                            },
                                            onAboutClick = { navController.navigate("about") }
                                        )
                                    }
                                }
                            )
                        }
                        composable(
                            route = "article/{id}",
                            deepLinks = ArticleNavDeepLinks
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
                                    },
                                    onHubClicked = {
                                        navController.navigate("hub/$it")
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
                            "user/{alias}?page={page}",
                            deepLinks = UserScreenNavDeepLinks
                        ) {
                            val page =
                                it.arguments?.getString("page")?.let { UserScreenPages.valueOf(it) }
                                    ?: UserScreenPages.Profile

                            var user: User? by remember { mutableStateOf(null) }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    user =
                                        it.arguments!!.getString("alias")
                                            ?.let { it1 -> UserController.get(it1) }

                                }
                            })
                            val logoutCoroutineScope = rememberCoroutineScope()
                            if (user != null) {
                                UserScreen(
                                    isAppUser = user!!.alias == userInfo?.alias,
                                    initialPage = page,
                                    user = user!!,
                                    onBack = { navController.popBackStack() },
                                    onArticleClicked = { navController.navigate("article/$it") },
                                    onUserClicked = { navController.navigate("user/$it") },
                                    onCommentsClicked = { navController.navigate("comments/$it") },
                                    onCommentClicked = { postId, commentId ->
                                        navController.navigate(
                                            "comments/$postId"
                                        )
                                    },
                                    viewModelStoreOwner = it,
                                    onLogout = {
                                        logoutCoroutineScope.launch {
                                            authDataStore.edit {
                                                it[DataStoreKeys.Auth.Authorized] = false
                                                it[DataStoreKeys.Auth.Cookies] = ""
                                            }
                                            //cookies = ""
                                            authorized = false
                                            navController.popBackStack(
                                                "articles",
                                                inclusive = false
                                            )
                                        }
                                    },
                                    onHubClicked = {
                                        navController.navigate("hub/$it")
                                    }
                                )
                            }

                        }

                        composable(
                            "hub/{alias}",
                            deepLinks = HubScreenNavDeepLinks
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
                            deepLinks = CompanyScreenNavDeepLinks,
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

                        composable("about") {
                            AboutScreen {
                                navController.popBackStack()
                            }
                        }

                    })
            }
        }

        Log.e("ExternalLink", intent.data.toString())

    }

}



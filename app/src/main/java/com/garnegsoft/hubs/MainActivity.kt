package com.garnegsoft.hubs


import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.coroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.NoConnectionInterceptor
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.Preferences
import com.garnegsoft.hubs.api.me.Me
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages

var cookies: String = ""
var authorized: Boolean = false

val Context.authDataStore by preferencesDataStore(HubsDataStore.Auth.DataStoreName)
val Context.settingsDataStore by preferencesDataStore(HubsDataStore.Settings.DataStoreName)
val Context.lastReadDataStore by preferencesDataStore(HubsDataStore.LastRead.DataStoreName)

fun <T> Context.lastReadDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
    return lastReadDataStore.data.map { it[key] }
}

fun <T> Context.authDataStoreFlow(key: Preferences.Key<T>): Flow<T?> {
    return authDataStore.data.map { it[key] }
}


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        CookieManager.getInstance().removeAllCookies(null)
        val cookiesFlow = authDataStore.data.map { it.get(HubsDataStore.Auth.Keys.Cookies) ?: "" }
        val isAuthorizedFlow =
            authDataStore.data.map { it[HubsDataStore.Auth.Keys.Authorized] ?: false }
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
                            it[HubsDataStore.Auth.Keys.Cookies] = result
                            it[HubsDataStore.Auth.Keys.Authorized] = true
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
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding(),
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
                                    val authorizedMenu by isAuthorizedFlow.collectAsState(
                                        initial = false
                                    )
                                    if (authorizedMenu && userInfo != null) {
                                        AuthorizedMenu(
                                            userAlias = userInfo!!.alias,
                                            avatarUrl = userInfo!!.avatarUrl,
                                            onProfileClick = { navController.navigate("user/${userInfo!!.alias}") },
                                            onArticlesClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Articles}") },
                                            onCommentsClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Comments}") },
                                            onBookmarksClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Bookmarks}") },
                                            onSavedArticlesClick = { navController.navigate("savedArticles")},
                                            onAboutClick = { navController.navigate("about") }
                                        )
                                    } else {

                                        UnauthorizedMenu(
                                            onLoginClick = {
                                                authActivityLauncher.launch(Unit)
                                                lifecycle.coroutineScope.launch(Dispatchers.IO) { userInfoUpdateBlock() }
                                            },
                                            onSavedArticlesClick = { navController.navigate("savedArticles")},
                                            onAboutClick = { navController.navigate("about") }
                                        )
                                    }
                                }
                            )
                        }

                        composable(
                            route = "article/{id}?offline={offline}",
                            deepLinks = ArticleNavDeepLinks
                        ) {
                            val id = it.arguments?.getString("id")?.toIntOrNull()
                            val offline = it.arguments?.getString("offline")?.toBooleanStrict()

                            val clearLastArticle = remember {
                                {
                                    lifecycle.coroutineScope.launch(Dispatchers.IO) {
                                        lastReadDataStore.edit {
                                            it[HubsDataStore.LastRead.Keys.LastArticleRead] = 0
                                            it[HubsDataStore.LastRead.Keys.LastArticleReadPosition] =
                                                0
                                        }
                                    }
                                }
                            }

                            BackHandler(enabled = true) {
                                clearLastArticle()
                                navController.navigateUp()
                            }
                            ArticleScreen(
                                articleId = id!!,
                                isOffline = offline ?: false,
                                onBackButtonClicked = {
                                    clearLastArticle()
                                    navController.navigateUp()
                                },
                                onCommentsClicked = {
                                    clearLastArticle()
                                    navController.navigate("comments/${id}")
                                },
                                onAuthorClicked = {
                                    clearLastArticle()
                                    navController.navigate("user/${it}")
                                },
                                onHubClicked = {
                                    clearLastArticle()
                                    navController.navigate("hub/$it")
                                },
                                onCompanyClick = {
                                    clearLastArticle()
                                    navController.navigate("company/$it")
                                }
                            )

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

                        composable(
                            route = "comments/{postId}?commentId={commentId}",
                            deepLinks = CommentsScreenNavDeepLinks
                        ) {
                            val postId = it.arguments!!.getString("postId")!!
                            val commentId = it.arguments?.getString("commentId")
                            CommentsScreen(
                                viewModelStoreOwner = it,
                                parentPostId = postId.toInt(),
                                commentId = commentId?.toInt(),
                                onBackClicked = { navController.navigateUp() },
                                onArticleClicked = { navController.navigate("article/$postId") },
                                onUserClicked = { navController.navigate("user/$it") }
                            )

                        }

                        composable(
                            "user/{alias}?page={page}",
                            deepLinks = UserScreenNavDeepLinks
                        ) {
                            navController.popBackStack("", false)
                            val page =
                                it.arguments?.getString("page")?.let { UserScreenPages.valueOf(it) }
                                    ?: UserScreenPages.Profile

                            val alias = it.arguments!!.getString("alias")!!

                            val logoutCoroutineScope = rememberCoroutineScope()
                            UserScreen(
                                isAppUser = alias == userInfo?.alias,
                                initialPage = page,
                                alias = alias,
                                onBack = { navController.popBackStack() },
                                onArticleClicked = { navController.navigate("article/$it") },
                                onUserClicked = { navController.navigate("user/$it") },
                                onCommentsClicked = { navController.navigate("comments/$it") },
                                onCommentClicked = { postId, commentId ->
                                    navController.navigate(
                                        "comments/$postId?commentId=$commentId"
                                    )
                                },
                                viewModelStoreOwner = it,
                                onLogout = {
                                    logoutCoroutineScope.launch {
                                        authDataStore.edit {
                                            it[HubsDataStore.Auth.Keys.Authorized] = false
                                            it[HubsDataStore.Auth.Keys.Cookies] = ""
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

                        composable("savedArticles"){
                            OfflineArticlesScreen(
                                onBack = { navController.popBackStack() },
                                onArticleClick = { navController.navigate("article/$it?offline=true")}
                            )
                        }

                    })
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    window.statusBarColor = Color.parseColor("#FF313131")
                } else {
                    window.statusBarColor = resources.getColor(R.color.habrTopColor)
                }
            }
        }

        Log.e("ExternalLink", intent.data.toString())

    }

}

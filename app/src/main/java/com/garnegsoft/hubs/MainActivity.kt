package com.garnegsoft.hubs

import ArticleController
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        CookieManager.getInstance().removeAllCookies(null)
//        registerForActivityResult(LoginActivityResultContract()) {
//            if (it != null) {
//                Log.i("LoginEnded", it)
//            }
//        }
//            .launch(Unit)
        intent.dataString?.let { Log.e("intentData", it) }

        HabrApi.HttpClient = OkHttpClient.Builder()
            .addInterceptor(NoConnectionInterceptor(this))
            .build()

        setContent {
            HubsTheme() {
                var navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "articles",
                    builder = {
                        composable("articles") {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {

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
                                    }
                                )
                            }

                        }
                        composable("article/{id}") {

                            var articleViewModel = viewModel<ArticleScreenViewModel>(it)
                            val article by articleViewModel.article.observeAsState()
                            if (article != null) {

                                ArticleScreen(
                                    article = article!!,
                                    onBackButtonClicked = { navController.popBackStack() },
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

                        composable("user/{alias}") {
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

                        composable("hub/{alias}") {
                            val alias = it.arguments?.getString("alias")
                            HubScreen(alias = alias!!, viewModelStoreOwner = it,
                                onBackClick = { navController.popBackStack() },
                                onArticleClick = { navController.navigate("article/$it") },
                                onCompanyClick = { navController.navigate("company/$it") },
                                onUserClick = { navController.navigate("user/$it") },
                                onCommentsClick = { navController.navigate("comments/$it") }
                            )
                        }
                        composable("company/{alias}") {
                            val alias = it.arguments?.getString("alias")!!
                            CompanyScreen(
                                viewModelStoreOwner = it,
                                alias = alias,
                                onBack = { navController.popBackStack() },
                                onArticleClick = { navController.navigate("article/$it")},
                                onCommentsClick = { navController.navigate("comments/$it")},
                                onUserClick = { navController.navigate("user/$it")}
                            )
                        }

                    })
            }
        }

        Log.e("ExternalLink", intent.data.toString())
        var context = this
        onBackPressedDispatcher.addCallback {
            Toast.makeText(
                context,
                "Want to escape?",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}

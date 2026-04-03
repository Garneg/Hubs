package com.garnegsoft.hubs.ui.navigation

import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.*
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
import com.garnegsoft.hubs.ui.screens.history.HistoryScreen
import com.garnegsoft.hubs.ui.screens.hub.HubScreen
import com.garnegsoft.hubs.ui.screens.imageViewer.ImageViewerScreenOverlay
import com.garnegsoft.hubs.ui.screens.imageViewer.rememberImageViewerState
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.MainScreen
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticleScreen
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesListScreen
import com.garnegsoft.hubs.ui.screens.search.SearchScreen
import com.garnegsoft.hubs.ui.screens.settings.ArticleScreenSettingsScreen
import com.garnegsoft.hubs.ui.screens.settings.FeedSettingsScreen
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreen
import com.garnegsoft.hubs.ui.screens.subscriptions.SubscriptionManagementScreen
import com.garnegsoft.hubs.ui.screens.user.LogoutConfirmDialog
import com.garnegsoft.hubs.ui.screens.user.UserScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun MainNavigationGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "main"
) {
    val rootCoroutineScope = rememberCoroutineScope()
    val imageViewerState =
        rememberImageViewerState(offlineResourcesRootPath = navController.context.filesDir.absolutePath + "/offline_resources/")

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(tween(250)) { it }
        },
        exitTransition = {
            if (this.targetState.destination.route?.startsWith("article/") == true){
                slideOutVertically(tween(250)) { -it / 5 } + fadeOut(targetAlpha = 0.5f)
            } else {
                slideOutHorizontally(tween(250)) { -it / 2 }
                //+ fadeOut(tween(250), targetAlpha = 0.9f)
            }
        },
        popEnterTransition = {
            if (this.initialState.destination.route?.startsWith("article/") == true){
                slideInVertically(tween(250)) { -it / 5 }
            } else {
                slideInHorizontally(tween(250)) { -it / 2 }
            }

        },
        popExitTransition = {
            slideOutHorizontally(tween(250)) { it }
//            +
//                    fadeOut(tween(250), targetAlpha = 0.9f)
        },
        builder = {
            
            hubsComposable(
                route = "main",
            ) {

                MainScreen(
                    viewModelStoreOwner = it,
                    onSearchClicked = {
                        navController.navigate("search")
                    },
                    onArticleClicked = {
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
                    onSavedArticles = {
                        navController.navigate("savedArticles")
                    },
                    onSubscriptionsClicked = {
                        navController.navigate("subscriptionManagement")
                    },
                    menu = {
                        val context = LocalContext.current
                        val isAuthorizedFlow = remember {
                            HubsDataStore.Auth.getValueFlow(
                                context,
                                HubsDataStore.Auth.Authorized
                            )
                        }
                        val showAuthorizedMenu by isAuthorizedFlow.collectAsState(
                            initial = false
                        )
                        val userAlias by HubsDataStore.Auth.getValueFlow(
                            LocalContext.current,
                            HubsDataStore.Auth.Alias
                        ).collectAsState(initial = "initial")
                        if (showAuthorizedMenu) {
                            AuthorizedMenu(
                                userAlias = userAlias,
                                onProfileClick = { navController.navigate("user/${userAlias}") },
                                onArticlesClick = { navController.navigate("user/${userAlias}?page=${UserScreenPages.Articles}") },
                                onCommentsClick = { navController.navigate("user/${userAlias}?page=${UserScreenPages.Comments}") },
                                onBookmarksClick = {
                                    navController.navigate(
                                        "user/${userAlias}?page=${UserScreenPages.Bookmarks}"
                                    )
                                },
                                onSubscriptionsClick = { navController.navigate("subscriptionManagement") },
                                onSavedArticlesClick = {
                                    navController.navigate(
                                        "savedArticles"
                                    )
                                },
                                onHistoryClick = { navController.navigate("history") },
                                onSettingsClick = { navController.navigate("settings") },
                                onAboutClick = { navController.navigate("about") }
                            )
                        } else {

                            val authActivityLauncher =
                                rememberLauncherForActivityResult(contract = AuthActivityResultContract()) {
                                    CookieManager.getInstance().removeAllCookies(null)
                                    rootCoroutineScope.launch {
                                        it?.let { result ->
                                            HubsDataStore.Auth.edit(
                                                context = navController.context,
                                                pref = HubsDataStore.Auth.Cookies,
                                                value = result.split("; ")
                                                    .find { it.startsWith("connect_sid") }!!
                                            )

                                            HubsDataStore.Auth.edit(
                                                context = navController.context,
                                                pref = HubsDataStore.Auth.Authorized,
                                                value = true
                                            )
                                            HabrApi.initializeWithCookies(navController.context, result)
                                            launch(Dispatchers.IO) {
                                                MeController.getMe()?.let {
                                                    val shortcut = ShortcutInfoCompat.Builder(
                                                        navController.context,
                                                        "bookmarks_shortcut"
                                                    )
                                                        .setIntent(
                                                            Intent(Intent.ACTION_VIEW).apply {
                                                                `package` =
                                                                    BuildConfig.APPLICATION_ID
                                                                data =
                                                                    Uri.parse("https://habr.com/users/${it.getOrNull()?.alias}/bookmarks")
                                                            }
                                                        )
                                                        .setIcon(
                                                            IconCompat.createWithResource(
                                                                navController.context,
                                                                R.drawable.bookmarks_shortcut_icon
                                                            )
                                                        )
                                                        .setShortLabel("Закладки")
                                                        .setLongLabel("Закладки")
                                                        .build()
                                                    ShortcutManagerCompat.pushDynamicShortcut(
                                                        navController.context,
                                                        shortcut
                                                    )

                                                }
                                                val updateMeDataRequest =
                                                    OneTimeWorkRequestBuilder<MeDataUpdateWorker>()
                                                        .setConstraints(
                                                            Constraints(
                                                                requiredNetworkType = NetworkType.CONNECTED
                                                            )
                                                        )
                                                        .build()
                                                WorkManager.getInstance(navController.context)
                                                    .enqueue(updateMeDataRequest)
                                            }
                                        }
                                    }
                                }

                            UnauthorizedMenu(
                                onLoginClick = {
                                    authActivityLauncher.launch(Unit)
                                },
                                onSavedArticlesClick = {
                                    navController.navigate(
                                        "savedArticles"
                                    )
                                },
                                onHistoryClick = { navController.navigate("history") },
                                onSettingsClick = { navController.navigate("settings") },
                                onAboutClick = { navController.navigate("about") }
                            )
                        }
                    }
                )
            }

            hubsComposable(
                route = "article/{id}?offline={offline}",
                deepLinks = ArticleNavDeepLinks,
                enterTransition = {
                    slideInVertically(
                        animationSpec = tween(durationMillis = 250, easing = EaseOutExpo),
                        initialOffsetY = { it / 1 }
                    ) +
                            scaleIn(
                                tween(250, easing = EaseOutExpo),
                                0.8f
                            ) +
                            fadeIn(
                                tween(durationMillis = 240, easing = EaseOutExpo)
                            )
                },
                popEnterTransition = {
                    if (navController.currentBackStackEntry?.destination?.route?.startsWith("article/") == true) {
                        slideInVertically(tween(250)) { -it / 5 }
                    } else
                        slideInHorizontally(tween(250)) { -it / 2 }
                },
//                exitTransition = {
//                    scaleOut(
//                        tween(250, easing = EaseIn),
//                        0.8f
//                    ) + fadeOut(
//                        tween(240, easing = EaseOut)
//                    )
//
//                },
                popExitTransition = {
                    scaleOut(
                        tween(250, easing = EaseInSine),
                        0.8f
                    ) +
                            slideOutVertically(
                                tween(250, easing = EaseInSine),
                                targetOffsetY = { it }
                            ) +
                            fadeOut(
                        tween(200, easing = EaseInQuart),

                    )

                },
            ) {
                val id = it.arguments?.getString("id")?.toIntOrNull()

                val clearLastArticle = remember {
                    {
                        rootCoroutineScope.launch(Dispatchers.IO) {
                            LastReadArticleController.clearLastArticle(navController.context)
                        }
                    }
                }


                DisposableEffect(key1 = id) {
                    onDispose {
                        clearLastArticle()
//                        if (navController.context.intent.data != null && navController.previousBackStackEntry == null) {
//                            navController.context.finish()
//                        }
                    }
                }

                // Old solution that blocks predictive back gesture handling by navigation
                // But i feel that i need to leave it too in case DisposableEffect will be improper solution

//                BackHandler(enabled = !imageViewerState.show) {
//                    clearLastArticle()
//                    if (navController.context.intent.data != null && navController.previousBackStackEntry == null) {
//                        navController.context.finish()
//                    } else {
//                        navController.popBackStack()
//                    }
//                }



                ArticleScreen(
                    articleId = id!!,
                    onBackButtonClicked = {
                        clearLastArticle()
                        navController.navigateBack()
                    },
                    onCommentsClick = {
                        clearLastArticle()
                        navController.navigate("comments/${id}")
                    },
                    onAuthorClick = {
                        clearLastArticle()
                        navController.navigate("user/${it}")
                    },
                    onHubClick = {
                        clearLastArticle()
                        navController.navigate("hub/$it")
                    },
                    onCompanyClick = {
                        clearLastArticle()
                        navController.navigate("company/$it")
                    },
                    onViewImageRequest = {
                        imageViewerState.showImage(it)
                    },
                    onArticleClick = {
                        navController.navigate("article/$it")
                    },
                    navigationTransition = transition,
                    viewModelStoreOwner = it
                )


            }

            hubsComposable(
                route = "offlineArticle/{articleId}",
                enterTransition = {
                    scaleIn(
                        tween(150, easing = EaseInOut),
                        0.9f
                    ) + fadeIn(
                        tween(durationMillis = 150, easing = EaseIn)
                    ) + slideInVertically(
                        tween(durationMillis = 150, easing = EaseIn),
                        initialOffsetY = { it / 9 }
                    )
                },
                popEnterTransition = {
                    fadeIn(
                        tween(durationMillis = 50, easing = EaseIn)
                    )
                },
                exitTransition = {
                    scaleOut(
                        tween(150, easing = EaseIn),
                        0.9f
                    ) + fadeOut(
                        tween(150, easing = EaseOut)
                    )

                },
                popExitTransition = {
                    scaleOut(
                        tween(150, easing = EaseOut),
                        0.9f
                    ) + fadeOut(
                        tween(150, easing = EaseOut)
                    )

                }
            ) {
                val articleId = it.arguments!!.getString("articleId")!!.toInt()

                OfflineArticleScreen(
                    articleId = articleId,
                    onSwitchToNormalMode = { navController.navigate("article/$articleId") },
                    onViewImageRequest = { imageViewerState.showImageOfflineMode(it) },
                    onBack = { navController.navigateBack() },
                    onDelete = {
                        OfflineArticlesController.deleteArticle(articleId, navController.context)
                        navController.navigateBack()
                    }
                )
            }

            hubsComposable(
                route = "search",
            ) {
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

            hubsComposable(
                route = "settings",
            ) {
                SettingsScreen(
                    onBack = {
                        navController.navigateBack()
                    },
                    onArticleScreenSettings = {
                        navController.navigate("article_settings")
                    },
                    onFeedSettings = {
                        navController.navigate("feed_settings")
                    }
                )
            }

            hubsComposable(
                route = "article_settings"
            ) {
                ArticleScreenSettingsScreen(
                    onBack = { navController.navigateBack() }
                )
            }

            hubsComposable(
                route = "feed_settings"
            ) {
                FeedSettingsScreen(
                    onBack = { navController.navigateBack() }
                )
            }

            hubsComposable(
                route = "comments/{postId}?commentId={commentId}",
                deepLinks = CommentsScreenNavDeepLinks
            ) {
                val postId = it.arguments!!.getString("postId")!!
                val commentId = it.arguments?.getString("commentId")
                var showFullContent by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(transition.isRunning) {
                    if (!transition.isRunning){
                        showFullContent = true
                    }
                }
                CommentsScreen(
                    viewModelStoreOwner = it,
                    parentPostId = postId.toInt(),
                    highlightedCommentId = commentId?.toInt(),
                    allowDisplayFullContent = showFullContent,
                    onBackClicked = {
                        navController.navigateBack()
                    },
                    onArticleClicked = { navController.navigate("article/$postId") },
                    onUserClicked = { navController.navigate("user/$it") },
                    onImageClick = { imageViewerState.showImage(it) },
                )

            }


            hubsComposable(
                route = "user/{alias}?page={page}",
                deepLinks = UserScreenNavDeepLinks,
            ) {
                val pageNavArgument = it.arguments?.getString("page")
                val page = if (pageNavArgument.isNullOrEmpty())
                    UserScreenPages.Profile
                else
                    UserScreenPages.valueOf(pageNavArgument)

                val deepLinkPage =
                    it.arguments?.getString("deepLinkPage")?.let {
                        when (it) {
                            "posts" -> UserScreenPages.Articles
                            "comments" -> UserScreenPages.Comments
                            "bookmarks" -> UserScreenPages.Bookmarks

                            else -> null
                        }
                    }
                val alias = it.arguments!!.getString("alias")!!

                val logoutCoroutineScope = rememberCoroutineScope()
                var showLogoutConfirmationDialog by remember {
                    mutableStateOf(
                        false
                    )
                }
                val context = LocalContext.current
                LogoutConfirmDialog(
                    show = showLogoutConfirmationDialog,
                    onDismiss = { showLogoutConfirmationDialog = false },
                    onProceed = {
                        logoutCoroutineScope.launch {
                            val shortcuts =
                                ShortcutManagerCompat.getDynamicShortcuts(
                                    navController.context
                                ).map { it.id }
                            ShortcutManagerCompat.disableShortcuts(
                                navController.context,
                                shortcuts,
                                "Вы вышли из аккаунта в приложении!"
                            )

                            AuthDataController.clearAuthData(context)
                            HabrApi.initializeWithCookies(context, "")

                            val updateMeDataRequest =
                                OneTimeWorkRequestBuilder<MeDataUpdateWorker>()
                                    .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                                    .build()
                            WorkManager.getInstance(context).enqueue(updateMeDataRequest)

                            navController.popBackStack(
                                "main",
                                inclusive = false
                            )
                            showLogoutConfirmationDialog = false
                        }
                    })
                val userAlias by HubsDataStore.Auth.getValueFlow(
                    context,
                    HubsDataStore.Auth.Alias
                ).collectAsState(initial = "")

                UserScreen(
                    isAppUser = alias == userAlias,
                    initialPage = deepLinkPage ?: page,
                    alias = alias,
                    onBack = {
                        navController.navigateBack()
                    },
                    onArticleClicked = { navController.navigate("article/$it") },
                    onUserClicked = { navController.navigate("user/$it") },
                    onCommentsClicked = { navController.navigate("comments/$it") },
                    onCommentClicked = { postId, commentId ->
                        navController.navigate(
                            "comments/$postId?commentId=$commentId"
                        )
                    },
                    onCompanyClick = { navController.navigate("company/$it") },
                    viewModelStoreOwner = it,
                    onLogout = {
                        showLogoutConfirmationDialog = true
                    },
                    onHubClicked = {
                        navController.navigate("hub/$it")
                    }
                )

            }

            hubsComposable(
                "hub/{alias}",
                deepLinks = HubScreenNavDeepLinks
            ) {
                val alias = it.arguments?.getString("alias")
                HubScreen(alias = alias!!, viewModelStoreOwner = it,
                    onBackClick = { navController.navigateBack() },
                    onArticleClick = { navController.navigate("article/$it") },
                    onCompanyClick = { navController.navigate("company/$it") },
                    onUserClick = { navController.navigate("user/$it") },
                    onCommentsClick = { navController.navigate("comments/$it") }
                )

            }
            hubsComposable(
                "company/{alias}"
            ) {
                val alias = it.arguments?.getString("alias")!!
                CompanyScreen(
                    viewModelStoreOwner = it,
                    alias = alias,
                    onBack = {
//                        if (navController.context.intent.data != null && navController.previousBackStackEntry == null) {
//                            navController.context.finish()
//                        } else {
//                            navController.navigateBack()
//                        }
                        navController.navigateBack()
                    },
                    onArticleClick = { navController.navigate("article/$it") },
                    onCommentsClick = { navController.navigate("comments/$it") },
                    onUserClick = { navController.navigate("user/$it") }
                )


            }

            hubsComposable(
                "about"
            ) {
                AboutScreen {
                    navController.navigateBack()
                }
            }

            hubsComposable(
                route = "savedArticles",
                deepLinks = listOf(navDeepLink { uriPattern = "hubs://saved-articles" }),
            ) {
                OfflineArticlesListScreen(
                    onBack = { navController.navigateBack() },
                    onArticleClick = { navController.navigate("offlineArticle/$it") }
                )
            }

            hubsComposable(
                "history"
            ) {
                HistoryScreen(
                    onBack = { navController.navigateBack() },
                    onArticleClick = { navController.navigate("article/$it") },
                    onUserClick = { navController.navigate("user/$it") },
                    onHubClick = { navController.navigate("hub/$it") },
                    onCompanyClick = { navController.navigate("company/$it") }
                )
            }

            hubsComposable(
                route = "subscriptionManagement"
            ) {
                SubscriptionManagementScreen(
                    onBack = { navController.navigateBack() },
                    onHubClick = { navController.navigate("hub/$it") },
                    onUserClick = { navController.navigate("user/$it") },
                    onCompanyClick = { navController.navigate("company/$it") }
                )
            }
        })

    ImageViewerScreenOverlay(
        state = imageViewerState
    )

}


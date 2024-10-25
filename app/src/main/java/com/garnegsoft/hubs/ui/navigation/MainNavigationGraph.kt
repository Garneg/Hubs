package com.garnegsoft.hubs.ui.navigation

import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.ArticleNavDeepLinks
import com.garnegsoft.hubs.AuthActivityResultContract
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.CommentsScreenNavDeepLinks
import com.garnegsoft.hubs.CompanyScreenNavDeepLinks
import com.garnegsoft.hubs.HubScreenNavDeepLinks
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.UserScreenNavDeepLinks
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.imageViewer.ImageViewerScreenOverlay
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
import com.garnegsoft.hubs.ui.screens.history.HistoryScreen
import com.garnegsoft.hubs.ui.screens.hub.HubScreen
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
import com.garnegsoft.hubs.ui.screens.user.LogoutConfirmDialog
import com.garnegsoft.hubs.ui.screens.user.UserScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainNavigationGraph(
    navController: NavHostController = rememberNavController(),
    parentActivity: ComponentActivity
) {
    val imageViewerState =
        rememberImageViewerState(offlineResourcesRootPath = parentActivity.filesDir.absolutePath + "/offline_resources/")
    NavHost(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        navController = navController,
        startDestination = "articles",
        builder = {

            composable(
                route = "articles",
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.EmptyTransitions.enterTransition
            ) {

                MainScreen(
                    viewModelStoreOwner = it,
                    onSearchClicked = { navController.navigate("search") },
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
                                    parentActivity.lifecycle.coroutineScope.launch {
                                        it?.let { result ->
                                            HubsDataStore.Auth.edit(
                                                context = parentActivity,
                                                pref = HubsDataStore.Auth.Cookies,
                                                value = result.split("; ")
                                                    .find { it.startsWith("connect_sid") }!!
                                            )

                                            HubsDataStore.Auth.edit(
                                                context = parentActivity,
                                                pref = HubsDataStore.Auth.Authorized,
                                                value = true
                                            )
                                            HabrApi.initializeWithCookies(parentActivity, result)
                                            launch(Dispatchers.IO) {
                                                MeController.getMe()?.let {
                                                    val shortcut = ShortcutInfoCompat.Builder(
                                                        parentActivity,
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
                                                                parentActivity,
                                                                R.drawable.bookmarks_shortcut_icon
                                                            )
                                                        )
                                                        .setShortLabel("Закладки")
                                                        .setLongLabel("Закладки")
                                                        .build()
                                                    ShortcutManagerCompat.pushDynamicShortcut(
                                                        parentActivity,
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
                                                WorkManager.getInstance(parentActivity)
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

            composable(
                route = "article/{id}?offline={offline}",
                deepLinks = ArticleNavDeepLinks,
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

                },
            ) {
                val id = it.arguments?.getString("id")?.toIntOrNull()

                val clearLastArticle = remember {
                    {
                        parentActivity.lifecycle.coroutineScope.launch(Dispatchers.IO) {
                            LastReadArticleController.clearLastArticle(parentActivity)
                        }
                    }
                }

                BackHandler(enabled = true) {
                    clearLastArticle()
                    if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                        parentActivity.finish()
                    } else {
                        navController.popBackStack()
                    }
                }

                ArticleScreen(
                    articleId = id!!,
                    onBackButtonClicked = {
                        clearLastArticle()
                        if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                            parentActivity.finish()
                        } else {
                            navController.popBackStack()
                        }
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
                    },
                    onViewImageRequest = {
                        imageViewerState.showImage(it)
                    },
                    onArticleClick = {
                        navController.navigate("article/$it")
                    },
                    viewModelStoreOwner = it
                )


            }

            composable(
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
                    onBack = { navController.popBackStack() },
                    onDelete = {
                        OfflineArticlesController.deleteArticle(articleId, parentActivity)
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "search",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
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

            composable(
                route = "settings",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onArticleScreenSettings = {
                        navController.navigate("article_settings")
                    },
                    onFeedSettings = {
                        navController.navigate("feed_settings")
                    }
                )
            }

            composable(
                route = "article_settings",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                ArticleScreenSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "feed_settings",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                FeedSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "comments/{postId}?commentId={commentId}",
                deepLinks = CommentsScreenNavDeepLinks,
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                val postId = it.arguments!!.getString("postId")!!
                val commentId = it.arguments?.getString("commentId")
                CommentsScreen(
                    viewModelStoreOwner = it,
                    parentPostId = postId.toInt(),
                    commentId = commentId?.toInt(),
                    onBackClicked = {
                        if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                            parentActivity.finish()
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onArticleClicked = { navController.navigate("article/$postId") },
                    onUserClicked = { navController.navigate("user/$it") },
                    onImageClick = { imageViewerState.showImage(it) },
                )

            }



            composable(
                route = "user/{alias}?page={page}",
                deepLinks = UserScreenNavDeepLinks,
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {

                val page =
                    it.arguments?.getString("page")
                        ?.let { UserScreenPages.valueOf(it) }
                        ?: UserScreenPages.Profile
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
                                    parentActivity
                                ).map { it.id }
                            ShortcutManagerCompat.disableShortcuts(
                                parentActivity,
                                shortcuts,
                                "Вы вышли из приложения!"
                            )

                            AuthDataController.clearAuthData(context)
                            HabrApi.initializeWithCookies(context, "")

                            val updateMeDataRequest =
                                OneTimeWorkRequestBuilder<MeDataUpdateWorker>()
                                    .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                                    .build()
                            WorkManager.getInstance(context).enqueue(updateMeDataRequest)

                            navController.popBackStack(
                                "articles",
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

                        if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                            parentActivity.finish()
                        } else {
                            navController.popBackStack()
                        }
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
                if (this.transition.isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(this.transition.isRunning) {})
                }
            }

            composable(
                "hub/{alias}",
                deepLinks = HubScreenNavDeepLinks,
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                val alias = it.arguments?.getString("alias")
                HubScreen(alias = alias!!, viewModelStoreOwner = it,
                    onBackClick = {
                        if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                            parentActivity.finish()
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onArticleClick = { navController.navigate("article/$it") },
                    onCompanyClick = { navController.navigate("company/$it") },
                    onUserClick = { navController.navigate("user/$it") },
                    onCommentsClick = { navController.navigate("comments/$it") }
                )

                if (this.transition.isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(this.transition.isRunning) {})
                }
            }
            composable(
                "company/{alias}",
                deepLinks = CompanyScreenNavDeepLinks,
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                val alias = it.arguments?.getString("alias")!!
                CompanyScreen(
                    viewModelStoreOwner = it,
                    alias = alias,
                    onBack = {
                        if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
                            parentActivity.finish()
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onArticleClick = { navController.navigate("article/$it") },
                    onCommentsClick = { navController.navigate("comments/$it") },
                    onUserClick = { navController.navigate("user/$it") }
                )

                if (this.transition.isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(this.transition.isRunning) {})
                }
            }

            composable(
                "about",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                AboutScreen {
                    navController.popBackStack()
                }
            }

            composable(
                route = "savedArticles",
                deepLinks = listOf(navDeepLink { uriPattern = "hubs://saved-articles" }),
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                OfflineArticlesListScreen(
                    onBack = { navController.popBackStack() },
                    onArticleClick = { navController.navigate("offlineArticle/$it") }
                )
            }

            composable(
                "history",
                enterTransition = Transitions.GenericTransitions.enterTransition,
                exitTransition = Transitions.GenericTransitions.exitTransition,
                popEnterTransition = Transitions.GenericTransitions.popEnterTransition,
                popExitTransition = Transitions.GenericTransitions.popExitTransition
            ) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    onArticleClick = { navController.navigate("article/$it") },
                    onUserClick = { navController.navigate("user/$it") },
                    onHubClick = { navController.navigate("hub/$it") },
                    onCompanyClick = { navController.navigate("company/$it") }
                )
            }
        })
    Box(
        Modifier
            .height(Dp(WindowInsets.statusBars.getTop(LocalDensity.current) / LocalDensity.current.density))
            .fillMaxWidth()
            .background(MaterialTheme.colors.let {
                if (it.isLight)
                    it.primary
                else
                    it.surface
            })
    )
    ImageViewerScreenOverlay(
        state = imageViewerState
    )


}
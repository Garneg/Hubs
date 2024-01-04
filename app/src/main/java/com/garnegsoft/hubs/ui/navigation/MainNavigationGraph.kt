package com.garnegsoft.hubs.ui.navigation
//
//import android.app.Activity
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.BackHandler
//import androidx.compose.animation.EnterTransition
//import androidx.compose.animation.ExperimentalAnimationApi
//import androidx.compose.animation.core.EaseIn
//import androidx.compose.animation.core.EaseInOut
//import androidx.compose.animation.core.EaseOut
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.scaleIn
//import androidx.compose.animation.scaleOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.content.pm.ShortcutManagerCompat
//import androidx.lifecycle.coroutineScope
//import androidx.navigation.NavController
//import androidx.navigation.NavDeepLink
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.garnegsoft.hubs.ArticleNavDeepLinks
//import com.garnegsoft.hubs.CommentsScreenNavDeepLinks
//import com.garnegsoft.hubs.CompanyScreenNavDeepLinks
//import com.garnegsoft.hubs.HubScreenNavDeepLinks
//import com.garnegsoft.hubs.UserScreenNavDeepLinks
//import com.garnegsoft.hubs.api.dataStore.AuthDataController
//import com.garnegsoft.hubs.api.dataStore.HubsDataStore
//import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
//import com.garnegsoft.hubs.ui.screens.AboutScreen
//import com.garnegsoft.hubs.ui.screens.ImageViewScreen
//import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
//import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
//import com.garnegsoft.hubs.ui.screens.comments.CommentsThreadScreen
//import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
//import com.garnegsoft.hubs.ui.screens.history.HistoryScreen
//import com.garnegsoft.hubs.ui.screens.hub.HubScreen
//import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
//import com.garnegsoft.hubs.ui.screens.main.MainScreen
//import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
//import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesListScreen
//import com.garnegsoft.hubs.ui.screens.search.SearchScreen
//import com.garnegsoft.hubs.ui.screens.settings.ArticleScreenSettingsScreen
//import com.garnegsoft.hubs.ui.screens.settings.FeedSettingsScreen
//import com.garnegsoft.hubs.ui.screens.settings.SettingsScreen
//import com.garnegsoft.hubs.ui.screens.user.LogoutConfirmDialog
//import com.garnegsoft.hubs.ui.screens.user.UserScreen
//import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//
//@OptIn(ExperimentalAnimationApi::class)
//@Composable
//fun MainNavigationGraph(
//	navController: NavHostController = rememberNavController(),
//	parentActivity: ComponentActivity
//) {
//
//	NavHost(
//		navController = navController,
//		startDestination = ""
//	) {
//		composable(
//			"articles",
//
//			popEnterTransition = { EnterTransition.None }
//		) {
//
//			MainScreen(
//				viewModelStoreOwner = it,
//				onSearchClicked = { navController.navigate("search") },
//				onArticleClicked = {
//					navController.navigate("article/$it")
//				},
//				onCommentsClicked = {
//					navController.navigate("comments/$it")
//				},
//				onUserClicked = {
//					navController.navigate("user/$it")
//				},
//				onCompanyClicked = {
//					navController.navigate("company/$it")
//				},
//				onHubClicked = {
//					navController.navigate("hub/$it")
//				},
//				menu = {
//					val authorizedMenu by HubsDataStore.Auth.getValueFlow(LocalContext.current, HubsDataStore.Auth.Authorized)
//						.collectAsState(
//						initial = false
//					)
//					if (authorizedMenu && userInfo != null) {
//						AuthorizedMenu(
//							userAlias = userInfo!!.alias,
//							avatarUrl = userInfo!!.avatarUrl,
//							onProfileClick = { navController.navigate("user/${userInfo!!.alias}") },
//							onArticlesClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Articles}") },
//							onCommentsClick = { navController.navigate("user/${userInfo!!.alias}?page=${UserScreenPages.Comments}") },
//							onBookmarksClick = {
//								navController.navigate(
//									"user/${userInfo!!.alias}?page=${UserScreenPages.Bookmarks}"
//								)
//							},
//							onSavedArticlesClick = {
//								navController.navigate(
//									"savedArticles"
//								)
//							},
//							onHistoryClick = { navController.navigate("history")},
//							onSettingsClick = { navController.navigate("settings") },
//							onAboutClick = { navController.navigate("about") }
//						)
//					} else {
//						UnauthorizedMenu(
//							onLoginClick = {
//								// TODO: resolve this
////								authActivityLauncher.launch(Unit)
////								lifecycle.coroutineScope.launch(
////									Dispatchers.IO
////								) { userInfoUpdateBlock() }
//							},
//							onSavedArticlesClick = {
//								navController.navigate(
//									"savedArticles"
//								)
//							},
//							onHistoryClick = { navController.navigate("history")},
//							onSettingsClick = { navController.navigate("settings") },
//							onAboutClick = { navController.navigate("about") }
//						)
//					}
//				}
//			)
//		}
//
//		composable(
//			route = "article/{id}?offline={offline}",
//			deepLinks = ArticleNavDeepLinks,
//			enterTransition = {
//				scaleIn(
//					tween(150, easing = EaseInOut),
//					0.9f
//				) + fadeIn(
//					tween(durationMillis = 150, easing = EaseIn)
//				) + slideInVertically(
//					tween(durationMillis = 150, easing = EaseIn),
//					initialOffsetY = { it / 9 }
//				)
//			},
//			popEnterTransition = {
//				fadeIn(
//					tween(durationMillis = 50, easing = EaseIn)
//				)
//			},
//			exitTransition = {
//				scaleOut(
//					tween(150, easing = EaseIn),
//					0.9f
//				) + fadeOut(
//					tween(150, easing = EaseOut)
//				)
//
//			},
//			popExitTransition = {
//				scaleOut(
//					tween(150, easing = EaseOut),
//					0.9f
//				) + fadeOut(
//					tween(150, easing = EaseOut)
//				)
//
//			},
//		) {
//
//			val id = it.arguments?.getString("id")?.toIntOrNull()
//			val offline =
//				it.arguments?.getString("offline")?.toBooleanStrict()
//
//			val clearLastArticle = remember {
//				{
//					parentActivity.lifecycle.coroutineScope.launch(Dispatchers.IO) {
//						LastReadArticleController.clearLastArticle(parentActivity)
//					}
//				}
//			}
//
//			BackHandler(enabled = true) {
//				clearLastArticle()
//				if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//					parentActivity.finish()
//				} else {
//					navController.popBackStack()
//				}
//			}
//
//			ArticleScreen(
//				articleId = id!!,
//				isOffline = offline ?: false,
//				onBackButtonClicked = {
//					clearLastArticle()
//					if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//						parentActivity.finish()
//					} else {
//						navController.popBackStack()
//					}
//				},
//				onCommentsClicked = {
//					clearLastArticle()
//					navController.navigate("comments/${id}")
//				},
//				onAuthorClicked = {
//					clearLastArticle()
//					navController.navigate("user/${it}")
//				},
//				onHubClicked = {
//					clearLastArticle()
//					navController.navigate("hub/$it")
//				},
//				onCompanyClick = {
//					clearLastArticle()
//					navController.navigate("company/$it")
//				},
//				onViewImageRequest = {
//					navController.navigate(route = "imageViewer?imageUrl=$it")
//				},
//				onArticleClick = {
//					navController.navigate("article/$it")
//				},
//				viewModelStoreOwner = it
//			)
//
//
//		}
//
//		composable(route = "search") {
//			SearchScreen(
//				viewModelStoreOwner = it,
//				onArticleClicked = { navController.navigate("article/$it") },
//				onUserClicked = { navController.navigate("user/$it") },
//				onHubClicked = { navController.navigate("hub/$it") },
//				onCompanyClicked = { navController.navigate("company/$it") },
//				onCommentsClicked = { navController.navigate("comments/$it") },
//				onBackClicked = { navController.navigateUp() }
//			)
//		}
//
//		composable(route = "settings") {
//			SettingsScreen(
//				onBack = {
//					navController.popBackStack()
//				},
//				onArticleScreenSettings = {
//					navController.navigate("article_settings")
//				},
//				onFeedSettings = {
//					navController.navigate("feed_settings")
//				}
//			)
//		}
//
//		composable(route = "article_settings") {
//			ArticleScreenSettingsScreen(
//				onBack = { navController.popBackStack() }
//			)
//		}
//
//		composable(route = "feed_settings") {
//			FeedSettingsScreen(
//				onBack = { navController.popBackStack()}
//			)
//		}
//
//		composable(
//			route = "comments/{postId}?commentId={commentId}",
//			deepLinks = CommentsScreenNavDeepLinks
//		) {
//			val postId = it.arguments!!.getString("postId")!!
//			val commentId = it.arguments?.getString("commentId")
//			CommentsScreen(
//				viewModelStoreOwner = it,
//				parentPostId = postId.toInt(),
//				commentId = commentId?.toInt(),
//				onBackClicked = {
//					if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//						parentActivity.finish()
//					} else {
//						navController.popBackStack()
//					}
//				},
//				onArticleClicked = { navController.navigate("article/$postId") },
//				onUserClicked = { navController.navigate("user/$it") },
//				onImageClick = { navController.navigate(route = "imageViewer?imageUrl=$it") },
//				onThreadClick = { navController.navigate("thread/$postId/$it") }
//			)
//
//		}
//
//		composable("thread/{articleId}/{threadId}") {
//			val articleId =
//				it.arguments!!.getString("articleId")?.toInt()
//			val threadId = it.arguments!!.getString("threadId")?.toInt()
//
//			CommentsThreadScreen(
//				articleId = articleId!!,
//				threadId = threadId!!,
//				onAuthor = { navController.navigate("user/$it") },
//				onImageClick = { navController.navigate("image/$it") },
//				onBack = { navController.popBackStack() }
//			)
//		}
//
//		composable(
//			route = "user/{alias}?page={page}",
//			deepLinks = UserScreenNavDeepLinks,
//			popExitTransition = { fadeOut(tween(50)) },
//
//			) {
//
//			val page =
//				it.arguments?.getString("page")
//					?.let { UserScreenPages.valueOf(it) }
//					?: UserScreenPages.Profile
//			val deepLinkPage =
//				it.arguments?.getString("deepLinkPage")?.let {
//					when (it) {
//						"posts" -> UserScreenPages.Articles
//						"comments" -> UserScreenPages.Comments
//						"bookmarks" -> UserScreenPages.Bookmarks
//
//						else -> null
//					}
//				}
//			val alias = it.arguments!!.getString("alias")!!
//
//			val logoutCoroutineScope = rememberCoroutineScope()
//			var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
//			LogoutConfirmDialog(
//				show = showLogoutConfirmationDialog,
//				onDismiss = { showLogoutConfirmationDialog = false },
//				onProceed = {
//					logoutCoroutineScope.launch {
//						val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(parentActivity).map { it.id }
//						ShortcutManagerCompat.disableShortcuts(parentActivity, shortcuts, "Вы вышли из приложения!")
//
//						AuthDataController.clearAuthData(parentActivity)
//// TODO: check logout
//						navController.popBackStack(
//							"articles",
//							inclusive = false
//						)
//						showLogoutConfirmationDialog = false
//					}
//				})
//			UserScreen(
//				isAppUser = alias == userInfo?.alias,
//				initialPage = deepLinkPage ?: page,
//				alias = alias,
//				onBack = {
//					if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//						parentActivity.finish()
//					} else {
//						navController.popBackStack()
//					}
//				},
//				onArticleClicked = { navController.navigate("article/$it") },
//				onUserClicked = { navController.navigate("user/$it") },
//				onCommentsClicked = { navController.navigate("comments/$it") },
//				onCommentClicked = { postId, commentId ->
//					navController.navigate(
//						"comments/$postId?commentId=$commentId"
//					)
//				},
//				onCompanyClick = { navController.navigate("company/$it") },
//				viewModelStoreOwner = it,
//				onLogout = {
//					showLogoutConfirmationDialog = true
//				},
//				onHubClicked = {
//					navController.navigate("hub/$it")
//				}
//			)
//			if (this.transition.isRunning) {
//				Box(
//					modifier = Modifier
//						.fillMaxSize()
//						.pointerInput(this.transition.isRunning) {})
//			}
//		}
//
//		composable(
//			"hub/{alias}",
//			deepLinks = HubScreenNavDeepLinks,
//			popExitTransition = { fadeOut(tween(50)) }
//		) {
//			val alias = it.arguments?.getString("alias")
//			HubScreen(alias = alias!!, viewModelStoreOwner = it,
//				onBackClick = {
//					if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//						parentActivity.finish()
//					} else {
//						navController.popBackStack()
//					}
//				},
//				onArticleClick = { navController.navigate("article/$it") },
//				onCompanyClick = { navController.navigate("company/$it") },
//				onUserClick = { navController.navigate("user/$it") },
//				onCommentsClick = { navController.navigate("comments/$it") }
//			)
//
//			if (this.transition.isRunning) {
//				Box(
//					modifier = Modifier
//						.fillMaxSize()
//						.pointerInput(this.transition.isRunning) {})
//			}
//		}
//		composable(
//			"company/{alias}",
//			deepLinks = CompanyScreenNavDeepLinks,
//			popExitTransition = { fadeOut(tween(50)) }
//		) {
//			val alias = it.arguments?.getString("alias")!!
//			CompanyScreen(
//				viewModelStoreOwner = it,
//				alias = alias,
//				onBack = {
//					if (parentActivity.intent.data != null && navController.previousBackStackEntry == null) {
//						parentActivity.finish()
//					} else {
//						navController.popBackStack()
//					}
//				},
//				onArticleClick = { navController.navigate("article/$it") },
//				onCommentsClick = { navController.navigate("comments/$it") },
//				onUserClick = { navController.navigate("user/$it") }
//			)
//
//			if (this.transition.isRunning) {
//				Box(
//					modifier = Modifier
//						.fillMaxSize()
//						.pointerInput(this.transition.isRunning) {})
//			}
//		}
//
//		composable("about") {
//			AboutScreen {
//				navController.popBackStack()
//			}
//		}
//
//		composable(
//			route = "savedArticles",
//			deepLinks = listOf(NavDeepLink("hubs://saved-articles"))
//		) {
//
//			OfflineArticlesListScreen(
//				onBack = { navController.popBackStack() },
//				onArticleClick = { navController.navigate("article/$it?offline=true") }
//			)
//		}
//
//		composable("imageViewer?imageUrl={imageUrl}") {
//			val url = it.arguments?.getString("imageUrl")
//			ImageViewScreen(
//				model = url!!,
//				onBack = { navController.popBackStack() })
//		}
//
//		composable("history"){
//			HistoryScreen(
//				onBack = { navController.popBackStack() },
//				onArticleClick = { navController.navigate("article/$it")},
//				onUserClick = { navController.navigate("user/$it")},
//				onHubClick = { navController.navigate("hub/$it")},
//				onCompanyClick = { navController.navigate("company/$it")}
//			)
//		}
//	}
//}
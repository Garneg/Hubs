package com.garnegsoft.hubs


import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.me.Me
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.ImageViewScreen
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsThreadScreen
import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
import com.garnegsoft.hubs.ui.screens.hub.HubScreen
import com.garnegsoft.hubs.ui.screens.main.MainScreen
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesScreen
import com.garnegsoft.hubs.ui.screens.search.SearchScreen
import com.garnegsoft.hubs.ui.screens.settings.ArticleScreenSettingsScreen
import com.garnegsoft.hubs.ui.screens.settings.FeedSettingsScreen
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreen
import com.garnegsoft.hubs.ui.screens.user.LogoutConfirmDialog
import com.garnegsoft.hubs.ui.screens.user.UserScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient


// TODO: shouldn't be singleton
var cookies: String by mutableStateOf("")
var authorized: Boolean = false


class MainActivity : ComponentActivity() {
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		
		var authStatus: Boolean? by mutableStateOf(null)
		
		val cookiesFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Cookies)
		val isAuthorizedFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Authorized)
		
		lifecycle.coroutineScope.launch {
			cookiesFlow.collect {
				cookies = it
			}
		}
		
		runBlocking {
			authStatus = isAuthorizedFlow.firstOrNull()
			authorized = isAuthorizedFlow.firstOrNull() ?: false
			cookies = cookiesFlow.firstOrNull() ?: ""
		}
		
		val authActivityLauncher =
			registerForActivityResult(AuthActivityResultContract()) { it ->
				CookieManager.getInstance().removeAllCookies(null)
				lifecycle.coroutineScope.launch {
					it?.let { result ->
						HubsDataStore.Auth.edit(
							context = this@MainActivity,
							pref = HubsDataStore.Auth.Cookies,
							value = result.split("; ").find { it.startsWith("connect_sid") }!!
						)
						
						HubsDataStore.Auth.edit(
							context = this@MainActivity,
							pref = HubsDataStore.Auth.Authorized,
							value = true
						)
						authorized = true
						
						launch(Dispatchers.IO) {
							MeController.getMe()?.let {
								val shortcut = ShortcutInfoCompat.Builder(this@MainActivity, "bookmarks_shortcut")
									.setIntent(
										Intent(Intent.ACTION_VIEW).apply {
											`package` = BuildConfig.APPLICATION_ID
											data = Uri.parse("https://habr.com/users/${it.alias}/bookmarks")
										}
									)
									.setIcon(IconCompat.createWithResource(this@MainActivity, R.drawable.bookmarks_shortcut_icon))
									.setShortLabel("Закладки")
									.setLongLabel("Закладки")
									.build()
								ShortcutManagerCompat.pushDynamicShortcut(this@MainActivity, shortcut)
								
							}
							
						}
					}
				}
			}
		
		intent.dataString?.let { Log.e("intentData", it) }
		HabrApi.initialize(this)
		
		
		
		setContent {
			
			key(cookies) {
				val themeMode by HubsDataStore.Settings
					.getValueFlow(this, HubsDataStore.Settings.Theme.ColorSchemeMode)
					.run { HubsDataStore.Settings.Theme.ColorSchemeMode.mapValues(this) }
					.collectAsState(initial = null)
				
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
				if (themeMode != null && authStatus != null) {
					HubsTheme(
						darkTheme = when (themeMode) {
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined -> isSystemInDarkTheme()
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Undetermined -> isSystemInDarkTheme()
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark -> true
							else -> false
						}
					) {
						val navController = rememberNavController()
						
						NavHost(
							modifier = Modifier
								.statusBarsPadding()
								.navigationBarsPadding(),
							navController = navController,
							startDestination = "articles",
							builder = {
								
								composable(
									"articles",
									
									popEnterTransition = { EnterTransition.None }
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
													onBookmarksClick = {
														navController.navigate(
															"user/${userInfo!!.alias}?page=${UserScreenPages.Bookmarks}"
														)
													},
													onSavedArticlesClick = {
														navController.navigate(
															"savedArticles"
														)
													},
													onSettingsClick = { navController.navigate("settings") },
													onAboutClick = { navController.navigate("about") }
												)
											} else {
												UnauthorizedMenu(
													onLoginClick = {
														authActivityLauncher.launch(Unit)
														lifecycle.coroutineScope.launch(
															Dispatchers.IO
														) { userInfoUpdateBlock() }
													},
													onSavedArticlesClick = {
														navController.navigate(
															"savedArticles"
														)
													},
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
									val offline =
										it.arguments?.getString("offline")?.toBooleanStrict()
									
									val clearLastArticle = remember {
										{
											lifecycle.coroutineScope.launch(Dispatchers.IO) {
												LastReadArticleController.clearLastArticle(this@MainActivity)
											}
										}
									}
									
									BackHandler(enabled = true) {
										clearLastArticle()
										if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
											this@MainActivity.finish()
										} else {
											navController.popBackStack()
										}
									}
									
									ArticleScreen(
										articleId = id!!,
										isOffline = offline ?: false,
										onBackButtonClicked = {
											clearLastArticle()
											if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
												this@MainActivity.finish()
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
											navController.navigate(route = "imageViewer?imageUrl=$it")
										},
										onArticleClick = {
											navController.navigate("article/$it")
										},
										viewModelStoreOwner = it
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
								
								composable(route = "settings") {
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
								
								composable(route = "article_settings") {
									ArticleScreenSettingsScreen(
										onBack = { navController.popBackStack() }
									)
								}
								
								composable(route = "feed_settings") {
									FeedSettingsScreen(
										onBack = { navController.popBackStack()}
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
										onBackClicked = {
											if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
												this@MainActivity.finish()
											} else {
												navController.popBackStack()
											}
										},
										onArticleClicked = { navController.navigate("article/$postId") },
										onUserClicked = { navController.navigate("user/$it") },
										onImageClick = { navController.navigate(route = "imageViewer?imageUrl=$it") },
										onThreadClick = { navController.navigate("thread/$postId/$it") }
									)
									
								}
								
								composable("thread/{articleId}/{threadId}") {
									val articleId =
										it.arguments!!.getString("articleId")?.toInt()
									val threadId = it.arguments!!.getString("threadId")?.toInt()
									
									CommentsThreadScreen(
										articleId = articleId!!,
										threadId = threadId!!,
										onAuthor = { navController.navigate("user/$it") },
										onImageClick = { navController.navigate("image/$it") },
										onBack = { navController.popBackStack() }
									)
								}
								
								composable(
									route = "user/{alias}?page={page}",
									deepLinks = UserScreenNavDeepLinks,
									popExitTransition = { fadeOut(tween(50)) },
									
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
									var showLogoutConfirmationDialog by remember { mutableStateOf(false) }
									LogoutConfirmDialog(
										show = showLogoutConfirmationDialog,
										onDismiss = { showLogoutConfirmationDialog = false },
										onProceed = {
											logoutCoroutineScope.launch {
												val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(this@MainActivity).map { it.id }
												ShortcutManagerCompat.disableShortcuts(this@MainActivity, shortcuts, "Вы вышли из приложения!")
												
												AuthDataController.clearAuthData(this@MainActivity)
												
												authorized = false
												navController.popBackStack(
													"articles",
													inclusive = false
												)
												showLogoutConfirmationDialog = false
											}
										})
									UserScreen(
										isAppUser = alias == userInfo?.alias,
										initialPage = deepLinkPage ?: page,
										alias = alias,
										onBack = {
											if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
												this@MainActivity.finish()
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
									deepLinks = HubScreenNavDeepLinks
								) {
									val alias = it.arguments?.getString("alias")
									HubScreen(alias = alias!!, viewModelStoreOwner = it,
										onBackClick = {
											if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
												this@MainActivity.finish()
											} else {
												navController.popBackStack()
											}
										},
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
										onBack = {
											if (this@MainActivity.intent.data != null && navController.previousBackStackEntry == null) {
												this@MainActivity.finish()
											} else {
												navController.popBackStack()
											}
										},
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
								
								composable(
									route = "savedArticles",
									deepLinks = listOf(NavDeepLink("hubs://saved-articles"))
								) {
									OfflineArticlesScreen(
										onBack = { navController.popBackStack() },
										onArticleClick = { navController.navigate("article/$it?offline=true") }
									)
								}
								
								composable("imageViewer?imageUrl={imageUrl}") {
									val url = it.arguments?.getString("imageUrl")
									ImageViewScreen(
										model = url!!,
										onBack = { navController.popBackStack() })
								}
							})
						
					}
				}
			}
		}
		
		Log.e("ExternalLink", intent.data.toString())
		
	}
	
}

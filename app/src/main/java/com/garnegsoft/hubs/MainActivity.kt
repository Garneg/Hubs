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
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import androidx.room.Database
import com.garnegsoft.hubs.api.dataStore.settingsDataStoreFlowWithDefault
import com.garnegsoft.hubs.api.me.Me
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.ImageViewScreen
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesScreen
import com.garnegsoft.hubs.ui.screens.settings.ArticleScreenSettingsScreen
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreen
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
import okhttp3.internal.filterList
import java.net.URLEncoder


// TODO: shouldn't be singleton
var cookies: String = ""
var authorized: Boolean = false

val Context.authDataStore by preferencesDataStore(HubsDataStore.Auth.DataStoreName)
val Context.settingsDataStore by preferencesDataStore(HubsDataStore.Settings.DataStoreName)
val Context.lastReadDataStore by preferencesDataStore(HubsDataStore.LastRead.DataStoreName)




class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		
		var authStatus: Boolean? by mutableStateOf(null)
		
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
			authStatus = isAuthorizedFlow.firstOrNull()
			authorized = isAuthorizedFlow.firstOrNull() ?: false
			cookies = cookiesFlow.firstOrNull() ?: ""
//			val themeInt = settingsDataStoreFlow(HubsDataStore.Settings.Keys.Theme).first()
//				?: HubsDataStore.Settings.Keys.ThemeMode.Undetermined.ordinal
		}
		
		val authActivityLauncher =
			registerForActivityResult(AuthActivityResultContract()) { result ->
				CookieManager.getInstance().removeAllCookies(null)
				lifecycle.coroutineScope.launch {
					result?.let { res ->
						authDataStore.edit {
							it[HubsDataStore.Auth.Keys.Cookies] = res.split("; ").find { it.startsWith("connect_sid") }!! + "; hl=ru; fl=ru"
							it[HubsDataStore.Auth.Keys.Authorized] = true
							authorized = true
							//cookies = result
						}
					}
				}
			}
		
		
		intent.dataString?.let { Log.e("intentData", it) }
		HabrApi.initialize(this)
		
		setContent {
			val themeMode by settingsDataStoreFlowWithDefault(
				HubsDataStore.Settings.Keys.Theme,
				HubsDataStore.Settings.Keys.ThemeMode.Undetermined.ordinal
			).collectAsState(initial = null)
			
			val theme by remember(themeMode) {
				mutableStateOf(
					themeMode?.let {
						HubsDataStore.Settings.Keys.ThemeMode.values()[it]
					} ?: HubsDataStore.Settings.Keys.ThemeMode.Undetermined
				
				)
			}
			
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
					darkTheme = when (theme) {
						HubsDataStore.Settings.Keys.ThemeMode.Undetermined -> isSystemInDarkTheme()
						HubsDataStore.Settings.Keys.ThemeMode.SystemDefined -> isSystemInDarkTheme()
						HubsDataStore.Settings.Keys.ThemeMode.Dark -> true
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
								exitTransition = { ExitTransition.None },
								popEnterTransition = { EnterTransition.None }
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
												onSavedArticlesClick = { navController.navigate("savedArticles") },
												onSettingsClick = { navController.navigate("settings") },
												onAboutClick = { navController.navigate("about") }
											)
										} else {
											UnauthorizedMenu(
												onLoginClick = {
													authActivityLauncher.launch(Unit)
													lifecycle.coroutineScope.launch(Dispatchers.IO) { userInfoUpdateBlock() }
												},
												onSavedArticlesClick = { navController.navigate("savedArticles") },
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
									
									scaleIn(tween(200, easing = EaseOut), 0.9f) + fadeIn(tween(270, easing = EaseOut))
								},
								exitTransition = {
									ExitTransition.None
								},
								popExitTransition = {
									scaleOut(tween(200, easing = EaseOut), 0.9f) + fadeOut(tween(200, easing = EaseOut), )
									
								},
								popEnterTransition = {
									EnterTransition.None
								}
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
									}
								)
							}
							
							composable(route = "article_settings") {
								ArticleScreenSettingsScreen(
									onBack = { navController.popBackStack() }
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
									onImageClick = { navController.navigate(route = "imageViewer?imageUrl=$it") }
								)
								
							}
							
							composable(
								"user/{alias}?page={page}",
								deepLinks = UserScreenNavDeepLinks
							) {
								navController.popBackStack("", false)
								val page =
									it.arguments?.getString("page")
										?.let { UserScreenPages.valueOf(it) }
										?: UserScreenPages.Profile
								val deepLinkPage = it.arguments?.getString("deepLinkPage")?.let {
									when (it) {
										"posts" -> UserScreenPages.Articles
										"comments" -> UserScreenPages.Comments
										"bookmarks" -> UserScreenPages.Bookmarks
										
										else -> null
									}
								}
								
								
								val alias = it.arguments!!.getString("alias")!!
								
								val logoutCoroutineScope = rememberCoroutineScope()
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
							
							composable("savedArticles") {
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
		
		Log.e("ExternalLink", intent.data.toString())
		
	}
	
}

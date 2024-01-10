package com.garnegsoft.hubs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.coroutineScope
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.me.MeController
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
import com.garnegsoft.hubs.ui.navigation.MainNavigationGraph
import com.garnegsoft.hubs.ui.screens.AboutScreen
import com.garnegsoft.hubs.ui.screens.ImageViewScreen
import com.garnegsoft.hubs.ui.screens.article.ArticleScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsScreen
import com.garnegsoft.hubs.ui.screens.comments.CommentsThreadScreen
import com.garnegsoft.hubs.ui.screens.company.CompanyScreen
import com.garnegsoft.hubs.ui.screens.history.HistoryScreen
import com.garnegsoft.hubs.ui.screens.hub.HubScreen
import com.garnegsoft.hubs.ui.screens.main.AuthorizedMenu
import com.garnegsoft.hubs.ui.screens.main.MainScreen
import com.garnegsoft.hubs.ui.screens.main.UnauthorizedMenu
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesListScreen
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


class MainActivity : ComponentActivity() {
	
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

//		(this.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
//			.clearApplicationUserData()
		
		var authStatus: Boolean? by mutableStateOf(null)
		
		val cookiesFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Cookies)
		val isAuthorizedFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Authorized)
		
		runBlocking {
			authStatus = isAuthorizedFlow.firstOrNull()
			HabrApi.initialize(this@MainActivity, cookiesFlow.firstOrNull() ?: "")
		}
		
		
		
		val authActivityLauncher =
			registerForActivityResult(AuthActivityResultContract()) { it ->
				
			}
		
		
		val updateMeData = OneTimeWorkRequestBuilder<MeDataUpdateWorker>()
			.setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
			.build()
		WorkManager.getInstance(this).enqueue(updateMeData)
		
		intent.dataString?.let { Log.e("intentData", it) }
		
		
		
			setContent {
				val cookies by cookiesFlow.collectAsState(initial = "")
				key(cookies) {
					val themeMode by HubsDataStore.Settings
						.getValueFlow(this, HubsDataStore.Settings.Theme.ColorSchemeMode)
						.run { HubsDataStore.Settings.Theme.ColorSchemeMode.mapValues(this) }
						.collectAsState(initial = null)
					
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
							
							MainNavigationGraph(parentActivity = this, navController = navController)
							
						}
					}
				}
			}
			
			Log.e("ExternalLink", intent.data.toString())
			
		
	}
}

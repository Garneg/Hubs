package com.garnegsoft.hubs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.data.FcmDispatcher
import com.garnegsoft.hubs.data.HabrApi
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.data.me.MeDataUpdateWorker
import com.garnegsoft.hubs.ui.navigation.MainNavigationGraph
import com.garnegsoft.hubs.ui.theme.HubsTheme
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		
		// Disable crashlytics if it's debug version
		FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
		
		intent.extras?.let {
			FcmDispatcher.dispatchExtras(
				handleUrl = {
					startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
						this.data = Uri.parse(it)
					}, null))
				},
				extras = it)
		}
		
//		FirebaseMessaging.getInstance().token.addOnCompleteListener {
//			Log.e("fcm-token", it.result)
//		}
		
		val workRequest = PeriodicWorkRequestBuilder<MostReadingWidgetUpdateWorker>(
			6, TimeUnit.HOURS
		)
			.setConstraints(
				Constraints.Builder()
					.setRequiredNetworkType(NetworkType.UNMETERED)
					.setRequiresBatteryNotLow(true)
					.build())
			.build()
		
		WorkManager.getInstance(this).enqueueUniquePeriodicWork(
			"update_news_work",
			ExistingPeriodicWorkPolicy.KEEP,
			workRequest
		)
		
		var authStatus: Boolean? by mutableStateOf(null)
		
		val cookiesFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Cookies)
		val isAuthorizedFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Authorized)
		
		runBlocking {
			authStatus = isAuthorizedFlow.firstOrNull()
			Firebase.crashlytics.setCustomKey("authorized", authStatus ?: false)
			HabrApi.initializeWithCookies(this@MainActivity, cookiesFlow.firstOrNull() ?: "")
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
						Scaffold(
							bottomBar = {
								val bsEntry by navController.currentBackStackEntryFlow.collectAsState(
									initial = null
								)
								if (bsEntry?.destination?.route?.startsWith("article/") == false) {
									
									BottomNavigation(
										modifier = Modifier.background(MaterialTheme.colors.background)
											.padding(WindowInsets.Companion.safeGestures.asPaddingValues(LocalDensity.current).calculateBottomPadding())
											.border(width = 0.5.dp, color = MaterialTheme.colors.onSurface.copy(0.1f), shape = RoundedCornerShape(25))
											.shadow(2.dp, shape = RoundedCornerShape(25))
											.clip(RoundedCornerShape(25)),
										elevation = 0.dp
									) {
										
										BottomNavigationItem(
											selected = bsEntry?.destination?.route?.startsWith("articles") == true,
											onClick = {
												navController.popBackStack(
													"articles",
													false
												)
											},
											icon = {
												Icon(
													imageVector = Icons.Default.Home,
													contentDescription = null
												)
											})
										BottomNavigationItem(
											selected = navController.currentDestination?.route?.startsWith(
												"search"
											) == true,
											onClick = {
													  navController.navigate("search") {
														  this.launchSingleTop = true
														  
													  }
												
											},
											icon = {
												Icon(
													imageVector = Icons.Default.Search,
													contentDescription = null
												)
											})
										BottomNavigationItem(
											selected = navController.currentDestination?.route?.startsWith(
												"bookmarks"
											) == true,
											onClick = {
												navController.navigate("bookmarks") {
													this.launchSingleTop = true
												}
											},
											icon = {
												Icon(
													painter = painterResource(id = R.drawable.bookmark),
													contentDescription = null
												)
											})
										BottomNavigationItem(
											selected = false,
											onClick = { /*TODO*/ },
											icon = {
												Icon(
													imageVector = Icons.Default.AccountCircle,
													contentDescription = null
												)
											})
									}
									Divider(modifier = Modifier.alpha(0.5f))
								}
								
							}
						) {
							Box(modifier = Modifier.padding(it)) {
								MainNavigationGraph(
									parentActivity = this@MainActivity,
									navController = navController
								)
							}
						}
					}
				}
			}
		}
		
		Log.e("ExternalLink", intent.data.toString())
		
	}
}

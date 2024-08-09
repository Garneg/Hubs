package com.garnegsoft.hubs

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.api.FcmDispatcher
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
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
		
		val workRequest = PeriodicWorkRequestBuilder<NewsWidgetUpdateWorker>(
			15, TimeUnit.MINUTES
		).build()
		
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
			HabrApi.initialize(this@MainActivity, cookiesFlow.firstOrNull() ?: "")
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
						
						MainNavigationGraph(
							parentActivity = this@MainActivity,
							navController = navController
						)
					}
				}
			}
		}
		
		Log.e("ExternalLink", intent.data.toString())
		
	}
}

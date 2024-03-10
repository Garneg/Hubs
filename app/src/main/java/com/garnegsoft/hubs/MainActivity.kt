package com.garnegsoft.hubs

import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
import com.garnegsoft.hubs.ui.navigation.MainNavigationGraph
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {
	
	
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

//		(this.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
//			.clearApplicationUserData()
		val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
		
		
		var authStatus: Boolean? by mutableStateOf(null)
		
		val cookiesFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Cookies)
		val isAuthorizedFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Authorized)
		
		runBlocking {
			authStatus = isAuthorizedFlow.firstOrNull()
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
						Scaffold(
							bottomBar = {
								val bsEntry by navController.currentBackStackEntryFlow.collectAsState(
									initial = null
								)
								if (bsEntry?.destination?.route?.startsWith("article/") == false) {
									BottomNavigation(
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
											onClick = { navController.navigate("search") },
											icon = {
												Icon(
													imageVector = Icons.Default.Search,
													contentDescription = null
												)
											})
										BottomNavigationItem(
											selected = false,
											onClick = { /*TODO*/ },
											icon = {
												Icon(
													imageVector = Icons.Default.Star,
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
								}
								Divider(modifier = Modifier.alpha(0.5f))
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

//	override fun onResume() {
//		super.onResume()
//		GlobalScope.launch{
//			Looper.prepare()
//			val connectivityManager =
//				this@MainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//			if (connectivityManager.activeNetwork == null) {
//				Toast.makeText(this@MainActivity, "Connection failed!", Toast.LENGTH_SHORT).show()
//			} else {
//				withContext(Dispatchers.IO) {
//					try {
//						Socket().use { socket ->
//							socket.connect(InetSocketAddress("habr.com", 80), 2000)
//						}
//						Toast.makeText(this@MainActivity, "Connected!", Toast.LENGTH_SHORT).show()
//					} catch (e: IOException) {
//						Toast.makeText(this@MainActivity, "Connection failed!", Toast.LENGTH_SHORT).show()
//
//
//					}
//				}
//
//			}
//
//
//		}
//	}
	

}

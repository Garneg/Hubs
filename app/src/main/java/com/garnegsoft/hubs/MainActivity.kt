package com.garnegsoft.hubs

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.api.FcmDispatcher
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.me.MeDataUpdateWorker
import com.garnegsoft.hubs.api.tts.HubsTTSService
import com.garnegsoft.hubs.api.tts.LocalMediaController
import com.garnegsoft.hubs.ui.navigation.MainNavigationGraph
import com.garnegsoft.hubs.ui.theme.HubsTheme
import com.google.common.util.concurrent.MoreExecutors
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.garnegsoft.hubs.api.tts.setTTSSpeed
import com.google.common.base.Stopwatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val stopwatch = Stopwatch.createStarted()
        super.onCreate(savedInstanceState)
        Log.i("Runblocking", "after onCreate: " + stopwatch.elapsed(TimeUnit.MILLISECONDS).toString())

        enableEdgeToEdge()
        // Disable crashlytics if it's debug version
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        intent.extras?.let {
            FcmDispatcher.dispatchExtras(
                handleUrl = {
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).apply {
                        this.data = it.toUri()
                    }, null))
                },
                extras = it
            )
        }
        // Just to get fcm token if device runs debug version
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                Log.i("fcm-token", it.result)
            }
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }

        Log.i("Runblocking", "after firebase: " + stopwatch.elapsed(TimeUnit.MILLISECONDS).toString())



        var authStatus: Boolean? by mutableStateOf(null)

        val cookiesFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Cookies)
        val isAuthorizedFlow = HubsDataStore.Auth.getValueFlow(this, HubsDataStore.Auth.Authorized)
        val ttsSpeechRate = HubsDataStore.Settings.TextToSpeech.SpeechRate.getFlow(this)


        runBlocking(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                authStatus = isAuthorizedFlow.firstOrNull()
                Firebase.crashlytics.setCustomKey("authorized", authStatus ?: false)
            }
            launch(Dispatchers.IO) {
                HabrApi.initializeWithCookies(this@MainActivity, cookiesFlow.firstOrNull() ?: "")
            }
        }

        Log.i("Runblocking", "after launches: " + stopwatch.elapsed(TimeUnit.MILLISECONDS).toString())



        val updateMeData = OneTimeWorkRequestBuilder<MeDataUpdateWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .build()
        WorkManager.getInstance(this).enqueue(updateMeData)

        Log.i("Runblocking", "after workmanager: " + stopwatch.elapsed(TimeUnit.MILLISECONDS).toString())

        intent.dataString?.let { Log.e("intentData", it) }

        Log.i("Runblocking", "before media controller builder: " + stopwatch.elapsed(TimeUnit.MILLISECONDS).toString())


        val mediaController = mutableStateOf<MediaController?>(null)



        val elsapsed = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)

        Log.i("Runblocking", "elapsed time: $elsapsed")

        setContent {
            val cookies by cookiesFlow.collectAsState(initial = "")

            CompositionLocalProvider(
                LocalMediaController provides mediaController.value,
            ) {

                key(cookies) {
                    val themeMode by remember {
                        HubsDataStore.Settings
                            .getValueFlow(this, HubsDataStore.Settings.Theme.ColorSchemeMode)
                            .run { HubsDataStore.Settings.Theme.ColorSchemeMode.mapValues(this) }
                    }
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
                            this.savedStateRegistry.consumeRestoredStateForKey("")

                            MainNavigationGraph(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
        val sessionToken = SessionToken(this, ComponentName(this, HubsTTSService::class.java))
        val mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            mediaController.value = mediaControllerFuture.get()
            lifecycleScope.launch {
                mediaController.value?.setTTSSpeed(ttsSpeechRate.first())
            }
        }, ContextCompat.getMainExecutor(this))

        Log.e("ExternalLink", intent.data.toString())

    }
}

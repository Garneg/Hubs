package com.garnegsoft.hubs.api.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


/**
 * Handles url. If url refers to habr, opens this link via app
 */
fun handleUrl(context: Context, url: String) {
//    val darkThemeEnabled = false
//    runBlocking {
//        val theme = HubsDataStore.Settings.getValueFlow(context, HubsDataStore.Settings.Theme.ColorSchemeMode).first()
//
//    }
    Log.e("URL Clicked", url)
    if (url.startsWith("https://habr.com") || url.startsWith("http://habrahabr.ru")){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            this.`package` = BuildConfig.APPLICATION_ID
        }
        context.startActivity(intent)
    } else {
        val customTabIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder().setToolbarColor(0xFF303B44.toInt()).build()
            )
            .setShowTitle(true)
            .setUrlBarHidingEnabled(true)
            .build()

        customTabIntent.launchUrl(context, Uri.parse(url))
    }
}
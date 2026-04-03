package com.garnegsoft.hubs.api.widget

import android.content.Context
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import kotlinx.coroutines.flow.first


class WidgetPreferences {

    companion object {
        suspend fun setFontSize(fontSize: Int, context: Context) {
            HubsDataStore.Settings.Widget.ArticleTitleFontSize.edit(context, fontSize)
        }

        suspend fun getFontSize(context: Context): Int {
            return HubsDataStore.Settings.Widget.ArticleTitleFontSize.getFlow(context).first()
        }

        suspend fun setThemeMode(mode: Int, context: Context) {
            HubsDataStore.Settings.Widget.ThemeMode.edit(context, mode)
        }

        suspend fun getThemeMode(context: Context): Int {
            return HubsDataStore.Settings.Widget.ThemeMode.getFlow(context).first()
        }
    }
}
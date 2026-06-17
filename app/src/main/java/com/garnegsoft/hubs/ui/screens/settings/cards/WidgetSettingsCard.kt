package com.garnegsoft.hubs.ui.screens.settings.cards

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.garnegsoft.hubs.MostReadingWidget
import com.garnegsoft.hubs.MostReadingWidgetReceiver
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.ui.screens.settings.SettingsCardItemPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WidgetSettingsCard(modifier: Modifier = Modifier.Companion) {
    val context = LocalContext.current
    SettingsCard(
        title = "Виджет читают сейчас"
    ) {
        var showWidgetCard by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            showWidgetCard = GlanceAppWidgetManager(context).getGlanceIds(MostReadingWidget::class.java).size == 0
        }
        if (showWidgetCard) {
            SettingsCardItem(
                title = "Добавить виджет",
                onClick = {
                    val widgetManager = AppWidgetManager.getInstance(context)
                    val widgetProvider = ComponentName(context, MostReadingWidgetReceiver::class.java)
                    if (Build.VERSION.SDK_INT >= 26 && widgetManager.isRequestPinAppWidgetSupported) {
                        widgetManager.requestPinAppWidget(widgetProvider, null, null)
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                })
        } else {
            if (Build.VERSION.SDK_INT >= 31) {
                val themeMode by collectPreferenceAsState(HubsDataStore.Settings.Widget.ThemeMode)
                SettingsCardItemPicker(
                    title = "Тема:",
                    items = listOf("Адаптивная (Material You)", "Как в приложении"),
                    pickedItemIndex = themeMode ?: 0,
                    onItemPicked = {
                        coroutineScope.launch(Dispatchers.IO) {
                            HubsDataStore.Settings.Widget.ThemeMode.edit(context = context, it)
                            MostReadingWidgetReceiver().glanceAppWidget.updateAll(context)
                        }
                    }
                )
            }


        }


    }
}
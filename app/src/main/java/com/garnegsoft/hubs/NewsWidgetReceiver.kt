package com.garnegsoft.hubs

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NewsWidgetReceiver(
	override val glanceAppWidget: GlanceAppWidget = NewsWidget()
) : GlanceAppWidgetReceiver() {

}
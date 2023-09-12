package com.garnegsoft.hubs

import android.appwidget.AppWidgetHost
import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.garnegsoft.hubs.ui.widgets.NewsWidgetLayout

class NewsWidget : GlanceAppWidget() {
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		
		provideContent(content = { NewsWidgetLayout() })
		
	}
	
	
}
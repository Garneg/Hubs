package com.garnegsoft.hubs

import ArticlesListController
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.garnegsoft.hubs.ui.theme.HubsWidgetThemeM3
import com.garnegsoft.hubs.ui.widgets.MostReadingWidgetLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MostReadingWidget : GlanceAppWidget() {
	var articles: List<Pair<String, Int>> = emptyList()
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		Log.i("most_reading_widget", "Articles before update: ${articles.size}")
		withContext(Dispatchers.IO) {
			articles = ArticlesListController.getMostReading()?.list?.map { Pair(it.title, it.id) } ?: emptyList()
		}
		if (BuildConfig.DEBUG) {
			Toast.makeText(context, "Data received, widget updated!", Toast.LENGTH_SHORT).show()
		}
		provideContent(content = {
			HubsWidgetThemeM3 {
				MostReadingWidgetLayout(articles.toList())
			}
			
		})
	}
	
	
	
	
}


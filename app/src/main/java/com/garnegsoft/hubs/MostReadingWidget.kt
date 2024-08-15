package com.garnegsoft.hubs

import ArticlesListController
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.theme.HubsWidgetTheme
import com.garnegsoft.hubs.ui.widgets.MostReadingWidgetLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MostReadingWidget : GlanceAppWidget() {
	var articles: List<Pair<String, Int>> = emptyList()
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		
		withContext(Dispatchers.IO) {
			articles = ArticlesListController.getMostReading()?.list?.map { Pair(it.title, it.id) } ?: emptyList()
		}
		Log.e("articles", articles.isEmpty().toString())
		provideContent(content = {
			HubsWidgetTheme {
				MostReadingWidgetLayout(articles.toList())
			}
			
		})
	}
	
	
	
	
}

class MostReadingWidgetUpdateWorker(
	val context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		val widgetManager = GlanceAppWidgetManager(context)
		val widgets = widgetManager.getGlanceIds(MostReadingWidget::class.java)
		Log.i("most_reading_widget", "Count of widget to update: ${widgets.size}")
		MostReadingWidget().updateAll(context)
		if (BuildConfig.DEBUG) {
			Looper.prepare()
			Toast.makeText(context, "Data received, widget updated!", Toast.LENGTH_SHORT).show()
		}
		return Result.success()
	}
	
}
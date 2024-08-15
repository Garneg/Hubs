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
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.theme.HubsWidgetTheme
import com.garnegsoft.hubs.ui.widgets.MostReadingWidgetLayout

class MostReadingWidget : GlanceAppWidget() {
	var articles: List<ArticleSnippet> = emptyList()
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		
		val articles = context.getSharedPreferences("widget", Context.MODE_PRIVATE).getStringSet("titles", emptySet())!!
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
		val mostReadingWidget = MostReadingWidget()
		mostReadingWidget.updateAll(context)
		val prefs = context.getSharedPreferences("widget", Context.MODE_PRIVATE)
		val mostReading = ArticlesListController.getMostReading()
		prefs.edit {
			this.clear()
		}
		prefs.edit().putStringSet("titles", mostReading!!.list.map { it.title + "*&^" + it.id.toString() }.toSet()).apply()
		
		
		val widgetManager = GlanceAppWidgetManager(context)
		MostReadingWidget().update(context, widgetManager.getGlanceIds(MostReadingWidget::class.java).first())
		Looper.prepare()
		Toast.makeText(context, "Data received, widget updated!", Toast.LENGTH_SHORT).show()
		return Result.success()
	}
	
}
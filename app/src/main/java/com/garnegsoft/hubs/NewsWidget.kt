package com.garnegsoft.hubs

import ArticleController
import ArticlesListController
import android.app.PendingIntent
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.DiscretePathEffect
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.material.ColorProviders
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.ui.theme.HubsWidgetTheme
import com.garnegsoft.hubs.ui.widgets.NewsWidgetLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.logging.Handler

class NewsWidget : GlanceAppWidget() {
	var articles: List<ArticleSnippet> = emptyList()
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		
		val articles = context.getSharedPreferences("widget", Context.MODE_PRIVATE).getStringSet("titles", emptySet())!!
		Log.e("articles", articles.isEmpty().toString())
		provideContent(content = {
			HubsWidgetTheme {
				NewsWidgetLayout(articles.toList())
			}
			
		})
	}
	
	
}

class NewsWidgetUpdateWorker(
	val context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		val newsWidget = NewsWidget()
		newsWidget.updateAll(context)
		val prefs = context.getSharedPreferences("widget", Context.MODE_PRIVATE)
		val mostReading = ArticlesListController.getMostReading()
		prefs.edit().putStringSet("titles", mostReading!!.list.map { it.title + "*&^" + it.id.toString() }.toSet()).apply()
		
		
		val widgetManager = GlanceAppWidgetManager(context)
		NewsWidget().update(context, widgetManager.getGlanceIds(NewsWidget::class.java).first())
		Looper.prepare()
		Toast.makeText(context, "Data received, widget updated!", Toast.LENGTH_SHORT).show()
		return Result.success()
	}
	
}
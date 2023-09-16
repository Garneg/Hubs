package com.garnegsoft.hubs

import android.appwidget.AppWidgetHost
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
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
import com.garnegsoft.hubs.ui.theme.HubsWidgetTheme
import com.garnegsoft.hubs.ui.widgets.NewsWidgetLayout
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.logging.Handler

class NewsWidget : GlanceAppWidget() {
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		
		provideContent(content = {
			HubsWidgetTheme {
				NewsWidgetLayout()
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
		
		Looper.prepare()
		Toast.makeText(context, "Worker worked", Toast.LENGTH_LONG).show()
		return Result.success()
	}
	
}
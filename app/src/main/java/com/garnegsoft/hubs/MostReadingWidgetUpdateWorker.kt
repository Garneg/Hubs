package com.garnegsoft.hubs

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MostReadingWidgetUpdateWorker(
	val context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		val widgetManager = GlanceAppWidgetManager(context)
		val widgets = widgetManager.getGlanceIds(MostReadingWidget::class.java)
		Log.i("most_reading_widget", "Count of widget to update: ${widgets.size}")
		MostReadingWidget().updateAll(context)
		
		return Result.success()
	}
	
}
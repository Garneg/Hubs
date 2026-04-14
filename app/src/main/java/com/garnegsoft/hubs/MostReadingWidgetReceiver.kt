package com.garnegsoft.hubs

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MostReadingWidgetReceiver(
	override val glanceAppWidget: GlanceAppWidget = MostReadingWidget()
) : GlanceAppWidgetReceiver() {
	companion object {
		private val periodicalWorkTag = "most_reading_update_periodical"
	}
	
	override fun onEnabled(context: Context?) {
		Log.i("most_reading_widget", "widget added")
		val periodicalWidgetUpdateRequest =
			PeriodicWorkRequestBuilder<MostReadingWidgetUpdateWorker>(3, TimeUnit.HOURS)
				.addTag(periodicalWorkTag)
				.setInitialDelay(6, TimeUnit.HOURS)
				.build()
		
		if (context != null) {
			WorkManager.getInstance(context).enqueueUniquePeriodicWork(
				periodicalWorkTag,
				ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
				periodicalWidgetUpdateRequest
			)
		}
		
		super.onEnabled(context)
	}
	
	override fun onDisabled(context: Context?) {
		Log.i("most_reading_widget", "widget deleted")
		context?.let {
			val wm = WorkManager.getInstance(it)
			wm.cancelAllWorkByTag(periodicalWorkTag)

		}
		
		super.onDisabled(context)
	}
	
}
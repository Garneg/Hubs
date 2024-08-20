package com.garnegsoft.hubs

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NewsWidgetReceiver(
	override val glanceAppWidget: GlanceAppWidget = MostReadingWidget()
) : GlanceAppWidgetReceiver() {
	companion object {
		private val periodicalWorkTag = "most_reading_update_periodical"
	}
	
	override fun onEnabled(context: Context?) {
		Log.i("most_reading_widget", "widget added")
		val periodicalWidgetUpdateRequest =
			PeriodicWorkRequestBuilder<MostReadingWidgetUpdateWorker>(6, TimeUnit.HOURS)
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
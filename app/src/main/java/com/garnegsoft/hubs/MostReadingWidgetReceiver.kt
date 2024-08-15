package com.garnegsoft.hubs

import android.content.Context
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
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
		Toast.makeText(context, "Новый виджет добавлен!", Toast.LENGTH_LONG).show()

//		val periodicalWidgetUpdateRequest =
//			PeriodicWorkRequestBuilder<MostReadingWidgetUpdateWorker>(6, TimeUnit.HOURS)
//				.addTag(periodicalWorkTag)
//				.setInitialDelay(6, TimeUnit.HOURS)
//				.build()
//		if (context != null) {
//			WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//				periodicalWorkTag,
//				ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
//				periodicalWidgetUpdateRequest
//			)

//		}
		
		
		super.onEnabled(context)
	}
	
	override fun onDisabled(context: Context?) {
		Toast.makeText(context, "Все виджеты удалены", Toast.LENGTH_LONG).show()
//		context?.let {
//			val wm = WorkManager.getInstance(it)
//			wm.cancelAllWorkByTag(periodicalWorkTag)
//		}
		
		super.onDisabled(context)
	}
	
}
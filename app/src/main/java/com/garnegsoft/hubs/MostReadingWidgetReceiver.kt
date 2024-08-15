package com.garnegsoft.hubs

import android.content.Context
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class NewsWidgetReceiver(
	override val glanceAppWidget: GlanceAppWidget = MostReadingWidget()
) : GlanceAppWidgetReceiver() {
	override fun onEnabled(context: Context?) {
		Toast.makeText(context, "Новый виджет добавлен!", Toast.LENGTH_LONG).show()
		super.onEnabled(context)
	}
	
	override fun onDisabled(context: Context?) {
		Toast.makeText(context, "Все виджеты удалены", Toast.LENGTH_LONG).show()
		super.onDisabled(context)
	}
	
}
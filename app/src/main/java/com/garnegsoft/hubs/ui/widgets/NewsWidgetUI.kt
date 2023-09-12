package com.garnegsoft.hubs.ui.widgets

import android.content.Context
import android.net.Uri
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import com.garnegsoft.hubs.R

@GlanceComposable
@Composable
fun NewsWidgetLayout() {
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			//.background(ColorProvider(Color.LightGray))
			.background(androidx.glance.ImageProvider(R.drawable.news_widget_background_shape))
			.clickable {
			
			}
	) {
		
	}
}
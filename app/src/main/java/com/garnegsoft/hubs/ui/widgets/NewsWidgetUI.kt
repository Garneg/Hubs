package com.garnegsoft.hubs.ui.widgets

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.glance.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.MainActivity
import com.garnegsoft.hubs.NewsWidgetUpdateWorker
import com.garnegsoft.hubs.R

@OptIn(ExperimentalGlanceApi::class)
@GlanceComposable
@Composable
fun NewsWidgetLayout(articles: List<String>) {
//	var isDarkTheme = false
	val context = LocalContext.current
//	if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
//		isDarkTheme = true
//	}
	
	Log.e("offline articles:", articles.size.toString())
	
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(ImageProvider(R.drawable.widget_background))
	) {
//		val time = Calendar.getInstance().time
//		Text(text = "Latest update: ${SimpleDateFormat("HH:mm:ss", Locale.US).format(time)}")
		
		
		Box(
			modifier = GlanceModifier.padding(8.dp)
		) {
			
			
			LazyColumn(modifier = GlanceModifier.padding(top = 35.dp)) {
				item {
					Spacer(GlanceModifier.height(23.dp + 0.dp))
				}
				
				itemsIndexed(
					articles
				) { index, it ->
					Column {
						Spacer(modifier = GlanceModifier.height(4.dp))
						WidgetArticleCard(
							title = it.split("*&^").first(),
							onClick = {
								val intent = Intent(context, MainActivity::class.java).apply {
									data = Uri.parse("https://habr.com/p/${it.split("*&^")[1]}")
//								`package` = BuildConfig.APPLICATION_ID
//								addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
//								addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
								}
								val pendingIntent = PendingIntent.getActivity(
									context,
									0,
									intent,
									PendingIntent.FLAG_IMMUTABLE
								)
								pendingIntent.send()
								
								context.startActivity(intent)
//							actionStartActivity<MainActivity>()
							}
						)
					}
				}
			}
			
			WidgetBar()
		}
		Box(
			modifier = GlanceModifier.fillMaxSize()
				.background(ImageProvider(R.drawable.widget_corners_overlay))
		) {
		
		}
	}
}

@GlanceComposable
@Composable
fun WidgetArticleCard(
	modifier: GlanceModifier = GlanceModifier,
	title: String,
	onClick: () -> Unit
) {
	Box(
		modifier.fillMaxWidth()
			.background(
				ImageProvider(R.drawable.news_widget_background_shape_dark)
			)
			
			.clickable(action(block = onClick), rippleOverride = R.drawable.rounded_corners_ripple)
			.padding(12.dp)
	
	) {
		
		Text(
			text = title,
			style = TextStyle(
				color = GlanceTheme.colors.onSurface,
				fontSize = 14.sp,
				fontWeight = FontWeight.Medium
			)
		)
	}
}

@GlanceComposable
@Composable
fun WidgetBar(modifier: GlanceModifier = GlanceModifier) {
	val context = LocalContext.current
	Box(
		modifier = GlanceModifier.padding(8.dp)
			.background(ImageProvider(R.drawable.widget_bar_background))
			.clickable(onClick = action {}, rippleOverride = R.drawable.invisible_ripple)
	) {
		Row(
			modifier = modifier
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = GlanceModifier.padding(start = 8.dp)
					.clickable(onClick = actionStartActivity(MainActivity::class.java), rippleOverride = R.drawable.rounded_corners_ripple),
				text = "Хабы",
				style = TextStyle(
					color = ColorProvider(Color.White),
					fontWeight = FontWeight.Medium
				)
			)
			Text(
				text = " · читают сейчас",
				style = TextStyle(color = ColorProvider(Color.White.copy(0.5f)))
			)
			Box(
				modifier = GlanceModifier.fillMaxWidth(),
				contentAlignment = Alignment.CenterEnd
			) {
				CircleIconButton(
					modifier = GlanceModifier.size(38.dp),
					imageProvider = ImageProvider(R.drawable.refresh),
					contentDescription = null,
					onClick = {
						val updateRequest = OneTimeWorkRequestBuilder<NewsWidgetUpdateWorker>().build()
						WorkManager.getInstance(context).enqueue(updateRequest)
					})
			}
		}
		
		
	}
	
}
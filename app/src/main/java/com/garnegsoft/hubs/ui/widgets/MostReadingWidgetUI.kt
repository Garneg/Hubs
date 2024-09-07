package com.garnegsoft.hubs.ui.widgets

import android.app.PendingIntent
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.glance.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
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
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.MainActivity
import com.garnegsoft.hubs.MostReadingWidgetUpdateWorker
import com.garnegsoft.hubs.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalGlanceApi::class)
@GlanceComposable
@Composable
fun MostReadingWidgetLayout(articles: List<Pair<String, Int>>) {
	var isDarkTheme = false
	val context = LocalContext.current
	if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
		isDarkTheme = true
	}
	
	Log.i("most_reading", "articles count: ${articles.size.toString()}")
	
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(
				imageProvider = ImageProvider(R.drawable.widget_background),
				colorFilter = ColorFilter.tint(GlanceTheme.colors.background)
			)
	
	) {
		Box(
			modifier = GlanceModifier.padding(8.dp)
		) {
			if (articles.isNotEmpty()) {
					LazyColumn(modifier = GlanceModifier.padding(top = 62.dp)) {
//						item {
//							Spacer(GlanceModifier.height(32.dp + 0.dp))
//						}
						
						itemsIndexed(
							articles
						) { index, it ->
							Column {
								if (index > 0) {
									Spacer(modifier = GlanceModifier.height(4.dp))
								}
								val id = remember { it.second }
								WidgetArticleCard(
									title = it.first,
									clickableKey = it.second.toString(),
									onClick = {
										val intent =
											Intent(context, MainActivity::class.java).apply {
												data = Uri.parse("https://habr.com/p/${id}")
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
									}
								)
							}
						}
						if (BuildConfig.DEBUG) {
							item {
								val time = Calendar.getInstance().time
								Text(
									text = "Latest update: ${
										SimpleDateFormat(
											"HH:mm:ss",
											Locale.US
										).format(time)
									}"
								)
							}
						}
					}
					
				
			} else {
				Box(
					modifier = GlanceModifier.fillMaxSize().padding(top = 23.dp),
					contentAlignment = Alignment.Center
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						Text(
							text = "Нет данных",
							style = TextStyle(
								ColorProvider(Color.Black.copy(0.5f)),
								fontWeight = FontWeight.Medium
							)
						)
						if (BuildConfig.DEBUG) {
							val time = Calendar.getInstance().time
							Text(
								text = "Latest update: ${
									SimpleDateFormat(
										"HH:mm:ss",
										Locale.US
									).format(time)
								}"
							)
						}
					}
				}
			}
			WidgetBar()
			
		}
		Column {
			Spacer(modifier = GlanceModifier.height(62.dp))
			Box(
				modifier = GlanceModifier
					
					.fillMaxSize()
					.background(
						imageProvider = if (isDarkTheme) {
							ImageProvider(R.drawable.widget_corners_overlay_dark)
						} else {
							ImageProvider(R.drawable.widget_corners_overlay)
						},
					)
			) {
			
			}
		}
		
		
	}
}

@GlanceComposable
@Composable
fun WidgetArticleCard(
	modifier: GlanceModifier = GlanceModifier,
	title: String,
	clickableKey: String? = null,
	onClick: () -> Unit
) {
	var isDarkTheme = false
	val context = LocalContext.current
	if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
		isDarkTheme = true
	}
	Box(
		modifier.fillMaxWidth()
			.background(
				ImageProvider(R.drawable.widget_list_card_background_shape),
				colorFilter = ColorFilter.tint(
					ColorProvider(
						if (isDarkTheme) {
							Color(49, 49, 49, 255)
						} else {
							Color.White
						}
					)
				)
			)
			.clickable(action(key = clickableKey, block = onClick), rippleOverride = R.drawable.rounded_corners_ripple)
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
	var isDarkTheme = false
	if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
		isDarkTheme = true
	}
	Box(
		modifier = GlanceModifier.padding(8.dp)
			.background(
				ImageProvider(R.drawable.widget_bar_background),
				colorFilter = ColorFilter.tint(
					if (isDarkTheme)
						ColorProvider(Color(49, 49, 49, 255))
					else
						GlanceTheme.colors.primary
				
				)
			)
			.clickable(onClick = action {}, rippleOverride = R.drawable.invisible_ripple)
	) {
		Row(
			modifier = modifier
				.fillMaxWidth()
				.padding(start = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = GlanceModifier
					.clickable(
						onClick = actionStartActivity(MainActivity::class.java),
						rippleOverride = R.drawable.rounded_corners_ripple
					),
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
						val updateRequest =
							OneTimeWorkRequestBuilder<MostReadingWidgetUpdateWorker>().build()
						WorkManager.getInstance(context).enqueue(updateRequest)
					})
			}
		}
		
		
	}
	
}
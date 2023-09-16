package com.garnegsoft.hubs.ui.widgets

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.util.TimeUtils
import android.widget.RemoteViews
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.garnegsoft.hubs.NewsWidget
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

@OptIn(ExperimentalGlanceApi::class)
@GlanceComposable
@Composable
fun NewsWidgetLayout() {
//	var isDarkTheme = false
	val context = LocalContext.current
//	if (LocalContext.current.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
//		isDarkTheme = true
//	}
	
//	var articles: List<OfflineArticleSnippet>
//	runBlocking {
//		articles =
//			OfflineArticlesDatabase.getDb(context).articlesDao().getAllSnippetsSortedByIdDesc()
//				.first()
//	}
	
//	Log.e("offline articles:", articles.size.toString())
//
	Box(
		modifier = GlanceModifier
			.fillMaxSize()
			.background(
			ImageProvider(R.drawable.news_widget_background_shape_dark)
		)
	) {
		val time = Calendar.getInstance().time
		Text(text = "Latest update: ${time.hours}:${time.minutes}")
//		LazyColumn(
//		) {
//			items(
//				articles
//			) {
//				Box(
//					modifier = GlanceModifier.padding(bottom = 4.dp)
//				) {
//					WidgetArticleCard(
//						title = it.title, onClick = {
//
//						})
//				}
//
//			}
//		}
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
			.clickable(onClick)
			.padding(12.dp)
	
	) {
		
		Text(
			text = title,
			style = TextStyle(
				color = GlanceTheme.colors.onSurface,
				fontSize = 16.sp,
				fontWeight = FontWeight.Medium
			)
		)
	}
}
package com.garnegsoft.hubs.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.history.HistoryActionType
import com.garnegsoft.hubs.data.history.HistoryEntityListModel
import com.garnegsoft.hubs.data.history.getArticle
import com.garnegsoft.hubs.data.utils.formatFoundationDate
import com.garnegsoft.hubs.ui.common.BaseHubsLazyColumn
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import java.util.Calendar
import java.util.Date


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryLazyColumn(
	model: HistoryEntityListModel,
	onArticleClick: (Int) -> Unit
) {
	LaunchedEffect(key1 = Unit, block = {
		if (!model.data.isInitialized) {
			model.loadFirstPage()
		}
	})
	
	val list by model.data.observeAsState()
	
	val lastLoadedPage by model.lastLoadedPage.observeAsState()
	
	list?.let { data ->
		
		BaseHubsLazyColumn(
			data = data,
			onScrollEnd = {
				model.loadNextPage()
			},
			lazyList = { state ->
				ArticleCardStyle.defaultArticleCardStyle()?.let {
					LazyColumn(
						state = state,
						contentPadding = PaddingValues(8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						var lastDay = 0
						
						data.list.forEach { entity ->
							val calendar =
								Calendar.getInstance().apply { time = Date(entity.timestamp) }
							val dayOfEvent = calendar.get(Calendar.DAY_OF_YEAR)
							
							if (dayOfEvent != lastDay) {
								stickyHeader {
									Row(
										modifier = Modifier.fillMaxWidth(),
										horizontalArrangement = Arrangement.Center
									) {
										Box(
											modifier = Modifier
												.padding(vertical = 2.dp)
												.shadow(0.dp, CircleShape)
												.clip(CircleShape)
												.background(MaterialTheme.colors.surface)
												.border(
													1.dp,
													MaterialTheme.colors.onBackground.copy(0.1f),
													CircleShape
												)
												.padding(horizontal = 12.dp, vertical = 4.dp)
										) {
											Text(
												text = remember {
													formatFoundationDate(
														calendar.get(
															Calendar.DAY_OF_MONTH
														).toString(),
														(calendar.get(Calendar.MONTH) + 1).toString(),
														calendar.get(Calendar.YEAR).toString()
													)!!
												},
												fontWeight = FontWeight.W500,
												color = MaterialTheme.colors.onBackground.copy(0.5f)
											)
										}
									}
								}
							}
							item {
								if (entity.actionType == HistoryActionType.Article) {
									ArticleHistoryCard(
										entity = entity,
										articleData = remember { entity.getArticle() },
										onClick = { onArticleClick(entity.getArticle().articleId) },
										style = it
									)
								}
							}
							lastDay = dayOfEvent
						}
						
						
						if (lastLoadedPage != null && data.pagesCount > lastLoadedPage!! + 1) {
							item {
								Box(modifier = Modifier.fillMaxWidth()) {
									CircularProgressIndicator(
										modifier = Modifier.align(
											Alignment.Center
										)
									)
								}
							}
						}
						
					}
				}
			},
			lazyListState = rememberLazyListState()
		)
	}
	
}
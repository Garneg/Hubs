package com.garnegsoft.hubs.ui.screens.history

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.history.HistoryActionType
import com.garnegsoft.hubs.api.history.HistoryDatabase
import com.garnegsoft.hubs.api.history.HistoryEntity
import com.garnegsoft.hubs.api.history.getArticle
import com.garnegsoft.hubs.api.utils.formatFoundationDate
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class HistoryScreenViewModel : ViewModel() {
	val elements = MutableLiveData<List<HistoryEntity>>()
	fun getFirstElements(context: Context) {
		viewModelScope.launch(Dispatchers.IO) {
			val result = HistoryDatabase.getDb(context).dao().getEventsPaged(0)
			elements.postValue(result)
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
	onBack: () -> Unit,
	onArticleClick: (articleId: Int) -> Unit,
	onUserClick: (alias: String) -> Unit,
	onHubClick: (alias: String) -> Unit,
	onCompanyClick: (alias: String) -> Unit,
) {
	val context = LocalContext.current
	val viewModel = viewModel<HistoryScreenViewModel>()
	val elements by viewModel.elements.observeAsState()
	LaunchedEffect(key1 = Unit, block = {
		viewModel.getFirstElements(context)
	})
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = "История") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
					}
				}
			)
		}
	) {
		Box(modifier = Modifier.padding(it)) {
			ArticleCardStyle.defaultArticleCardStyle()?.let {
				LazyColumn(
					contentPadding = PaddingValues(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					var lastDay = 0
					
					elements?.forEach { entity ->
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
											.shadow(1.dp, CircleShape)
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
				}
			}
		}
	}
}
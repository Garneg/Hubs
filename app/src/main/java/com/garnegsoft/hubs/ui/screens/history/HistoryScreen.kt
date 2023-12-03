package com.garnegsoft.hubs.ui.screens.history

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.history.HistoryActionType
import com.garnegsoft.hubs.api.history.HistoryDatabase
import com.garnegsoft.hubs.api.history.HistoryEntity
import com.garnegsoft.hubs.api.history.getArticle
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
fun HistoryScreen() {
	val context = LocalContext.current
	val viewModel = viewModel<HistoryScreenViewModel>()
	val elements by viewModel.elements.observeAsState()
	LaunchedEffect(key1 = Unit, block = {
		viewModel.getFirstElements(context)
	})
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = "История")},
			)
		}
	) {
		Box(modifier = Modifier.padding(it)) {
			LazyColumn(
				contentPadding = PaddingValues(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				var lastDay = 0
				elements?.forEach {
					val calendar = Calendar.getInstance().apply { time = Date(it.timestamp) }
					val dayOfEvent = calendar.get(Calendar.DAY_OF_YEAR)
					if (dayOfEvent != lastDay){
						stickyHeader {
							Text(text = dayOfEvent.toString())
						}
					}
					item {
						if (it.actionType == HistoryActionType.Article) {
							ArticleHistoryCard(entity = it, articleData = remember { it.getArticle()})
						}
					}
					lastDay = dayOfEvent
				}
			}
			
		}
	}
}
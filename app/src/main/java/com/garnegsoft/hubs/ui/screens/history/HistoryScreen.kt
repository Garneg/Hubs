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
import com.garnegsoft.hubs.api.history.HistoryEntityListModel
import com.garnegsoft.hubs.api.history.getArticle
import com.garnegsoft.hubs.api.utils.formatFoundationDate
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date


class HistoryScreenViewModel(context: Context) : ViewModel() {
	val model = HistoryEntityListModel(
		coroutineScope = viewModelScope,
		dao = HistoryDatabase.getDb(context).dao()
	)
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
	val viewModel = viewModel<HistoryScreenViewModel> { HistoryScreenViewModel(context)}
	
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
			HistoryLazyColumn(model = viewModel.model, onArticleClick = onArticleClick)
		}
	}
}
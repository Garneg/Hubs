package com.garnegsoft.hubs.ui.screens.history

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.data.history.HistoryDatabase
import com.garnegsoft.hubs.data.history.HistoryEntityListModel


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
				elevation = 0.dp,
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
package com.garnegsoft.hubs.ui.screens.bookmarks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.data.article.ArticlesListModel
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.HubsCircularProgressIndicator
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.screens.offline.OfflineArticlesList
import com.garnegsoft.hubs.ui.screens.user.UserBookmarksFilter
import kotlinx.coroutines.launch


class BookmarksScreenViewModel(userAlias: String?) : ViewModel() {
	val bookmarksFilter =
		MutableLiveData(UserBookmarksFilter(UserBookmarksFilter.Bookmarks.Articles))
	val bookmarksModel = userAlias?.let {
		ArticlesListModel(
			path = "articles",
			coroutineScope = viewModelScope,
			baseArgs = arrayOf("user" to userAlias),
			initialFilter = bookmarksFilter.value
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(
	onArticleClick: (Int) -> Unit,
	onOfflineArticleClick: (Int) -> Unit
) {
	val context = LocalContext.current
	val isAuthorized by HubsDataStore.Auth.getValueFlow(context, HubsDataStore.Auth.Authorized).collectAsState(
		initial = null
	)
	val userAlias by HubsDataStore.Auth.getValueFlow(context, HubsDataStore.Auth.Alias).collectAsState(
		initial = null
	)
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = "Закладки") },
				elevation = 0.dp
			)
		}
	) {
		
		
		
		val pagerState = rememberPagerState {
			2
		}
		val coroutineScope = rememberCoroutineScope()
		Column(modifier = Modifier.padding(it)) {
			isAuthorized?.let {
				if (isAuthorized == true && userAlias != null) {
					val viewModel = viewModel { BookmarksScreenViewModel(if (userAlias!!.isEmpty()) null else userAlias) }
					TabRow(
						selectedTabIndex = pagerState.currentPage,
						contentColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
					) {
						Tab(selected = pagerState.currentPage == 0, onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(0)
							}
						}) {
							Text(
								modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
								fontWeight = FontWeight.W500,
								text = "Закладки"
							)
						}
						Tab(selected = pagerState.currentPage == 1, onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(1)
							}
						}) {
							Text(
								modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
								fontWeight = FontWeight.W500,
								text = "Скачанные"
							)
						}
					}
					HorizontalPager(
						modifier = Modifier.fillMaxSize(),
						state = pagerState
					) {
						when (it) {
							0 -> {
								
									ArticlesListPageWithFilter(
										listModel = viewModel.bookmarksModel!!,
										//lazyListState = articlesLazyListState,
//							collapsingContentState = articlesFilterContentState,
										onArticleSnippetClick = onArticleClick,
										onArticleAuthorClick = {},
										onArticleCommentsClick = {}
									) { defaultValues, onDismiss, onDone ->
										UserBookmarksFilter(
											defaultValues = defaultValues,
											onDismiss = onDismiss,
											onDone = {
												viewModel.bookmarksFilter.value = it
												onDone(it)
											}
										)
									}
								
							}
							
							1 -> {
								OfflineArticlesList(onArticleClick = onOfflineArticleClick)
							}
						}
					}
				} else {
					OfflineArticlesList(onArticleClick = {})
				}
		} ?: Box(modifier = Modifier.fillMaxSize()) {
				HubsCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			}
		}
	}
}
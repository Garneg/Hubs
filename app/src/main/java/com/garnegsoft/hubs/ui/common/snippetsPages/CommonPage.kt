package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.CollapsingContent
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrSnippet
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.article.HabrSnippetListModel
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.FilterElement
import com.garnegsoft.hubs.ui.common.HubsCircularProgressIndicator
import com.garnegsoft.hubs.ui.common.LazyHabrSnippetsColumn
import com.garnegsoft.hubs.ui.common.RefreshableContainer
import kotlinx.coroutines.launch

@Composable
fun <T : HabrSnippet> CommonPage(
	listModel: HabrSnippetListModel<T>,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	snippetCard: @Composable (T) -> Unit,
) {
	val refreshing by listModel.isRefreshing.observeAsState(initial = false)
	
	val data by listModel.data.observeAsState()
	var doScrollToTop by rememberSaveable() { mutableStateOf(false) }
	var lastFirstItemArticleId by rememberSaveable {
		mutableStateOf(0)
	}
	
	LaunchedEffect(key1 = data?.list?.firstOrNull(), block = {
		if (doScrollToTop || lastFirstItemArticleId != data?.list?.firstOrNull()?.id) {
			lazyListState.scrollToItem(0)
			data?.list?.firstOrNull()?.id?.let {
				lastFirstItemArticleId = it
			}
		}
		doScrollToTop = false
	})
	
	LaunchedEffect(key1 = Unit, block = {
		if (doInitialLoading && !listModel.data.isInitialized) {
			listModel.loadFirstPage()
		}
	})
	
	val isPullingInProgress = remember {
		mutableStateOf(true)
	}
	
	val lastLoadedPageNumber by listModel.lastLoadedPage.observeAsState(1)
	val isLoading by listModel.isLoading.observeAsState(false)
	val isLoadingNextPage by listModel.isLoadingNextPage.observeAsState(false)

	
	CollapsingContent(
		collapsingContent = { collapsingBar?.invoke() },
		doCollapse = !isPullingInProgress.value,
		state = collapsingContentState
	) {
		if (data != null && data!!.list.isNotEmpty() && (!isLoading || refreshing || isLoadingNextPage)) {
			
			RefreshableContainer(
				isPullingInProgress = isPullingInProgress,
				onRefresh = {
					doScrollToTop = true
					listModel.refresh()
				},
				refreshing = refreshing
			) {
				LazyHabrSnippetsColumn(
					modifier = Modifier.fillMaxSize(),
					lazyListState = lazyListState,
					contentPadding = PaddingValues(0.dp),
					verticalArrangement = Arrangement.spacedBy(0.dp),
					data = data!!,
					onScrollEnd = listModel::loadNextPage,
					snippet = snippetCard,
					nextPageLoadingIndicator = if (lastLoadedPageNumber < data!!.pagesCount) {
						{
							Box(modifier = Modifier.fillMaxWidth()) {
								HubsCircularProgressIndicator(
									modifier = Modifier.align(
										Alignment.Center
									)
								)
							}
						}
					} else null
				)
				
			}
		} else if (isLoading) {
			Box(modifier = Modifier.fillMaxSize()) {
				HubsCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			}
		} else {
			Box(modifier = Modifier.fillMaxSize())
		}
	}
}

@Composable
fun <T : HabrSnippet, F : Filter> CommonPageWithFilter(
	listModel: AbstractSnippetListModel<T>,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	filter: (@Composable (onClick: () -> Unit) -> Unit) = { onClick ->
		listModel.filter.observeAsState().value?.let {
			val corscop = rememberCoroutineScope()
			FilterElement(title = it.getTitle(), onClick = {
				corscop.launch {
					collapsingContentState.animateShow()
				}
				onClick()
			})
		}
	},
	doInitialLoading: Boolean = true,
	filterDialog: @Composable (defaultValues: F, onDismiss: () -> Unit, onDone: (F) -> Unit) -> Unit,
	snippetCard: @Composable (T) -> Unit,
) {
	var showDialog by rememberSaveable {
		mutableStateOf(false)
	}
	
	val filterValues by listModel.filter.observeAsState()
	
	if (showDialog) {
		filterDialog(
			defaultValues = filterValues!! as F,
			onDismiss = { showDialog = false },
			onDone = {
				listModel.editFilter(it)
				showDialog = false
			}
		)
	}
	
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = {
			filter {
				showDialog = true
			}
		},
		doInitialLoading = doInitialLoading,
		snippetCard = snippetCard,
		collapsingContentState = collapsingContentState
	)
}


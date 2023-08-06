package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.CollapsingContent
import com.garnegsoft.hubs.api.HabrSnippet
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.LazyHabrSnippetsColumn
import com.garnegsoft.hubs.ui.common.RefreshableContainer

@Composable
fun <T : HabrSnippet> CommonPage(
    listModel: AbstractSnippetListModel<T>,
    lazyListState: LazyListState = rememberLazyListState(),
    filter: (@Composable () -> Unit)? = null,
    doInitialLoading: Boolean = true,
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
        if (doInitialLoading && !listModel.data.isInitialized){
            listModel.loadFirstPage()
        }
    })

    val isPullingInProgress = remember {
        mutableStateOf(true)
    }

    val lastLoadedPageNumber by listModel.lastLoadedPage.observeAsState(1)

    if (data != null && data!!.list.isNotEmpty()) {
        CollapsingContent(
            collapsingContent = { filter?.invoke() },
            doCollapse = !isPullingInProgress.value
        ) {
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
                    data = data!!,
                    onScrollEnd = listModel::loadNextPage,
                    snippet = snippetCard,
                    nextPageLoadingIndicator = if (lastLoadedPageNumber < data!!.pagesCount) {
                        {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(
                                        Alignment.Center
                                    )
                                )
                            }
                        }
                    } else null
                )
            }
        }
    } else if (listModel.isLoading.observeAsState(false).value) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        Box(modifier = Modifier.fillMaxSize())
    }
}


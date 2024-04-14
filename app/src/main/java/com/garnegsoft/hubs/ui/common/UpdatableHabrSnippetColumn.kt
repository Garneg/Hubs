package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.HabrSnippet

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : HabrSnippet> PagedRefreshableHabrSnippetsColumn(
    modifier: Modifier = Modifier.fillMaxSize(),
    data: HabrList<T>,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onNextPageLoad: (pageNumber: Int) -> Unit,
    nextPageLoadingIndicator: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            HubsCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    },
    page: MutableState<Int> = rememberSaveable {
        mutableStateOf(1)
    },
    isInProgress: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    onRefresh: () -> Unit,
    refreshing: Boolean,
    readyToScrollUpAfterRefresh: MutableState<Boolean>,
    snippet: @Composable LazyItemScope.(T) -> Unit,
) {

    LaunchedEffect(key1 = readyToScrollUpAfterRefresh.value, block = {
        if (readyToScrollUpAfterRefresh.value) {
            lazyListState.animateScrollToItem(0)
            readyToScrollUpAfterRefresh.value = false
        }

    })

    val state = rememberPullRefreshState(
        refreshing = refreshing,
        refreshThreshold = 50.dp,
        onRefresh = onRefresh
    )

    isInProgress.value = remember { derivedStateOf { state.progress > 0f } }.value
    Box(modifier = Modifier.pullRefresh(state)) {
        PagedHabrSnippetsColumn(
            modifier = modifier,
            data = data,
            lazyListState = lazyListState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            onNextPageLoad = onNextPageLoad,
            nextPageLoadingIndicator = nextPageLoadingIndicator,
            page = page,
            snippet = snippet
        )
        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colors.secondary
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefreshableContainer(
    isPullingInProgress: MutableState<Boolean> = remember {
        mutableStateOf(false)
    },
    onRefresh: () -> Unit,
    refreshing: Boolean,
    content: @Composable () -> Unit
) {
    val state = rememberPullRefreshState(
        refreshing = refreshing,
        refreshThreshold = 50.dp,
        onRefresh = onRefresh
    )
    isPullingInProgress.value = remember { derivedStateOf { state.progress > 0f } }.value
    Box(modifier = Modifier.pullRefresh(state)) {
        content()
        PullRefreshIndicator(
            refreshing = refreshing,
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colors.secondary
        )
    }
}
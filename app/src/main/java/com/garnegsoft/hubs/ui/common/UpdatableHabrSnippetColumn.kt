package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.HabrSnippet
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : HabrSnippet> PagedRefreshableHabrSnippetsColumn(
    modifier: Modifier = Modifier.fillMaxSize(),
    data: HabrList<T>,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onNextPageLoad: suspend CoroutineScope.(pageNumber: Int) -> Unit,
    nextPageLoadingIndicator: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
    snippet: @Composable (T) -> Unit,
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
            contentColor = MaterialTheme.colors.primary
        )
    }

}
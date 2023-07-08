package com.garnegsoft.hubs.ui.common

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.HabrSnippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun <T : HabrSnippet> LazyHabrSnippetsColumn(
    data: HabrList<T>,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    onScrollEnd: () -> Unit = { },
    nextPageLoadingIndicator: (@Composable () -> Unit)? = {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    },
    snippet: @Composable (T) -> Unit,
) {
    val derivedItemsCount by remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount } }
    val isLastDerived by remember {
        derivedStateOf {
            if (data.list.size > 0 && lazyListState.layoutInfo.totalItemsCount > 0)
                lazyListState.layoutInfo.totalItemsCount - 1 == lazyListState.layoutInfo.visibleItemsInfo.last().index
            else
                false
        }
    }
    var doPageLoad by remember(data.list.firstOrNull()?.id) { mutableStateOf(true) }
    LaunchedEffect(key1 = isLastDerived, data.list.firstOrNull()?.id) {
        if (isLastDerived && doPageLoad) {
            doPageLoad = false
            onScrollEnd()
        }
    }
    LaunchedEffect(key1 = derivedItemsCount) {
        doPageLoad = true
    }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        items(
            items = data.list,
            key = { it.id },
        ) {
            snippet(it)
        }
        nextPageLoadingIndicator?.let {
            item {
                nextPageLoadingIndicator()
            }
        }
    }
}


@Composable
@SuppressLint("ModifierParameter")
fun <T : HabrSnippet> PagedHabrSnippetsColumn(
    data: HabrList<T>,
    modifier: Modifier = Modifier.fillMaxSize(),
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onNextPageLoad: (pageNumber: Int) -> Unit,
    nextPageLoadingIndicator: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    },
    page: MutableState<Int> = rememberSaveable {
        mutableStateOf(1)
    },
    snippet: @Composable (T) -> Unit,
) {
    val scrollEndCoroutineScope = rememberCoroutineScope()
    LazyHabrSnippetsColumn(
        data = data,
        modifier = modifier,
        lazyListState = lazyListState,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        snippet = snippet,
        onScrollEnd = {
            if (page.value < data.pagesCount) {
                page.value++
                onNextPageLoad(page.value)

            }
        },
        nextPageLoadingIndicator =
        if (page.value < data.pagesCount) nextPageLoadingIndicator
        else null

    )
}
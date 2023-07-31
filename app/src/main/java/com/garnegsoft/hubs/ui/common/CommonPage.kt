package com.garnegsoft.hubs.ui.common

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.CollapsingContent
import com.garnegsoft.hubs.api.HabrSnippet
import com.garnegsoft.hubs.api.article.AbstractHabrSnippetListModel
import com.garnegsoft.hubs.api.article.ArticlesListModel

@Composable
fun <T : HabrSnippet> CommonPage(
    listModel: AbstractHabrSnippetListModel<T>,
    lazyListState: LazyListState = rememberLazyListState(),
    filter: (@Composable () -> Unit)? = null,
    snippetCard: @Composable (T) -> Unit,
) {

    val refreshing by listModel.isRefreshing.observeAsState(initial = false)

    val data by listModel.data.observeAsState()
    var doScrollToTop by rememberSaveable() { mutableStateOf(false) }

    LaunchedEffect(key1 = data?.list?.firstOrNull(), block = {
        if (doScrollToTop)
            lazyListState.scrollToItem(0)
        doScrollToTop = false
    })

    val doCollapse = remember {
        mutableStateOf(true)
    }

    val lastLoadedPageNumber by listModel.lastLoadedPage.observeAsState(1)

    if (data != null && data!!.list.isNotEmpty()) {
        CollapsingContent(
            collapsingContent = { filter?.invoke() },
            doCollapse = doCollapse.value
        ) {
            RefreshableContainer(
                isPullingInProgress = doCollapse,
                onRefresh = {
                    doScrollToTop = true
                    listModel.refresh()
                },
                refreshing = refreshing
            ) {
                LazyHabrSnippetsColumn(
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

@Composable
fun DefaultFilter(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .clickable(onClick = onClick),
    ) {
        Text(
            modifier = Modifier.padding(12.dp),
            text = title,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colors.onSurface
        )
        Divider(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun <T : Filter> CommonFilter(
    defaultValue: T,
    onDone: (T) -> Unit,
    filterDialogContent: @Composable (defaultValue: T, ) -> Unit
) {
    BaseFilterDialog(onDismiss = { /*TODO*/ }, onDone = {  }) {
        filterDialogContent
    }
}

interface Filter {
    fun getTitle(): String

    fun getArgs(): Map<String, String>
}
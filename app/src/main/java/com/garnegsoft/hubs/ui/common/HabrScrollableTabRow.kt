package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabrScrollableTabRow(
    pagerState: PagerState,
    tabs: List<String>
) {
    val pagerStateCoroutineScope = rememberCoroutineScope()
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 8.dp,
        divider = {
            Divider()
        },
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface
    ) {
        tabs.forEachIndexed { index, s ->
            Tab(
                selected = index == pagerState.currentPage,
                onClick = {
                    pagerStateCoroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }) {
                Text(
                    s,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

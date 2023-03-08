package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
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
        contentColor = SecondaryColor
    ) {
        tabs.forEachIndexed { index, s ->
            Tab(
                selected = index == pagerState.currentPage,
                onClick = {
                    pagerStateCoroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }) {
                Text(s, modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), fontWeight = FontWeight.W500)
            }
        }
    }
}

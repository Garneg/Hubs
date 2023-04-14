package com.garnegsoft.hubs.ui.common

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


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
        indicator = {
                    TabRowDefaults.Indicator(modifier = Modifier.customTabIndicatorOffset(
                        currentTabPosition = it[pagerState.currentPage],
                        offset = pagerState.currentPageOffsetFraction,
                        nextTabPosition =
                        when {
                            pagerState.currentPageOffsetFraction < 0 -> it[pagerState.currentPage - 1]
                            pagerState.currentPageOffsetFraction > 0 -> it[pagerState.currentPage + 1]
                            else -> it[pagerState.currentPage]
                        }

                    ))
        },
        contentColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = index == pagerState.currentPage,
                onClick = {
                    pagerStateCoroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    fontWeight = FontWeight.W500,
                )
            }
        }
    }
}

fun Modifier.customTabIndicatorOffset(
    currentTabPosition: TabPosition,
    offset: Float,
    nextTabPosition: TabPosition,
): Modifier {
    val indicatorWidth = currentTabPosition.width + (nextTabPosition.width - currentTabPosition.width) * offset.absoluteValue
    val indicatorOffset = currentTabPosition.left + (nextTabPosition.left - currentTabPosition.left) * offset.absoluteValue
    Log.e("cti_width", indicatorWidth.value.toString())
    Log.e("cti_offset", indicatorOffset.value.toString())

    return fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = indicatorOffset)
        .width(indicatorWidth)
}

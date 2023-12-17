package com.garnegsoft.hubs.ui.common

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * @param onCurrentPositionTabClick - calls when page that tab is associated with click is already current pager page
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabrScrollableTabRow(
	pagerState: PagerState,
	tabs: List<String>,
	onCurrentPositionTabClick: suspend CoroutineScope.(index: Int, title: String) -> Unit = { i, t -> }
) {
	val pagerStateCoroutineScope = rememberCoroutineScope()
	
	ScrollableTabRow(
		modifier = Modifier.fillMaxWidth(),
		selectedTabIndex = pagerState.currentPage,
		edgePadding = 8.dp,
		divider = {
			Divider()
		},
		backgroundColor = MaterialTheme.colors.surface,
		indicator = {
			TabRowDefaults.Indicator(
				modifier = Modifier
					.customTabIndicatorOffset(
						currentTabPosition = it[pagerState.currentPage],
						offset = pagerState.currentPageOffsetFraction,
						nextTabPosition =
						when {
							pagerState.currentPageOffsetFraction < 0 -> it[pagerState.currentPage - 1]
							pagerState.currentPageOffsetFraction > 0 -> it[pagerState.currentPage + 1]
							else -> it[pagerState.currentPage]
						}
					
					)
					.padding(horizontal = 8.dp)
					.clip(CircleShape)
			)
		},
		contentColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
	) {
		tabs.forEachIndexed { index, title ->
			Tab(
				selected = index == pagerState.currentPage,
				onClick = {
					pagerStateCoroutineScope.launch {
						if (pagerState.currentPage == index)
							onCurrentPositionTabClick(this, index, title)
						else
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
	val indicatorWidth =
		currentTabPosition.width + (nextTabPosition.width - currentTabPosition.width) * offset.absoluteValue
	val indicatorOffset =
		currentTabPosition.left + (nextTabPosition.left - currentTabPosition.left) * offset.absoluteValue
	
	return fillMaxWidth()
		.wrapContentSize(Alignment.BottomStart)
		.offset(x = indicatorOffset)
		.width(indicatorWidth)
}

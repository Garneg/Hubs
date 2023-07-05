package com.garnegsoft.hubs.api

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Preview
@Composable
fun FilterShitExperimental() {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var filterHeight by rememberSaveable { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val filterHeightPx =
        rememberSaveable(filterHeight) { with(density) { filterHeight.roundToPx().toFloat() } }
    var filterOffsetPx by rememberSaveable { mutableStateOf(0f) }
    val nestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

            if (available.y > 0 && filterOffsetPx > 0f) {
                filterOffsetPx -= available.y
                filterOffsetPx = filterOffsetPx.coerceIn(0f, filterHeightPx)
                return available
            }
            if (available.y < 0 && filterOffsetPx < filterHeightPx) {
                filterOffsetPx -= available.y
                filterOffsetPx = filterOffsetPx.coerceIn(0f, filterHeightPx)
                return available
            }

            return Offset.Zero
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return super.onPostScroll(consumed, available, source)
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            Log.e("so pre fling_a", available.toString())

            return super.onPreFling(available)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            Log.e("so post fling_c", consumed.toString())
            Log.e("so post fling_a", available.toString())

            return super.onPostFling(consumed, available)
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .scrollable(
                rememberScrollableState(consumeScrollDelta = { it }),
                orientation = Orientation.Vertical
            )

    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = filterHeight - Dp(filterOffsetPx / density.density)),
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(50) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(modifier = Modifier.padding(50.dp), text = "Card number $it")
                }
            }
        }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(0, -filterOffsetPx.roundToInt())
                }
                .onGloballyPositioned {
                    filterHeight = (it.size.height.toFloat() / density.density).dp
                }
                .fillMaxWidth()
                .clickable { }
                .background(MaterialTheme.colors.surface)

        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .padding(16.dp), text = "Filter"
            )
            Divider(modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}


/**
 * Collapsing content, that hides when user scrolls down and shows if user scrolls up.
 * Note, that you need scrolling container in content
 * @param collapsingContent UI, that will be hided/shown
 * @param content UI, that collapsing content will be drawn on
 */
@Composable
fun CollapsingContent(
    collapsingContent: @Composable () -> Unit,
    doCollapse: Boolean = true,
    content: @Composable () -> Unit
) {
    var collapsingContentHeightDp by rememberSaveable { mutableStateOf(0f) }
    val density = LocalDensity.current
    val collapsingContentHeightPx = rememberSaveable(collapsingContentHeightDp) {
        with(density) {
            collapsingContentHeightDp.dp.roundToPx().toFloat()
        }
    }

    var collapsingContentOffsetPx by rememberSaveable { mutableStateOf(0f) }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (doCollapse) {
                if (available.y > 0 && collapsingContentOffsetPx > 0f) {
                    collapsingContentOffsetPx -= available.y
                    collapsingContentOffsetPx =
                        collapsingContentOffsetPx.coerceIn(0f, collapsingContentHeightPx)
                    return available
                }
                if (available.y < 0 && collapsingContentOffsetPx < collapsingContentHeightPx) {
                    collapsingContentOffsetPx -= available.y
                    collapsingContentOffsetPx =
                        collapsingContentOffsetPx.coerceIn(0f, collapsingContentHeightPx)
                    return available
                }
            }

            return Offset.Zero
        }

    }

    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        Box(Modifier.padding(top = collapsingContentHeightDp.dp - Dp(collapsingContentOffsetPx / density.density))) {
            content()
        }
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .offset {
                    IntOffset(0, -collapsingContentOffsetPx.roundToInt())
                }
                .onGloballyPositioned {
                    collapsingContentHeightDp = it.size.height.toFloat() / density.density
                }

        ) {
            collapsingContent()
        }
    }
}

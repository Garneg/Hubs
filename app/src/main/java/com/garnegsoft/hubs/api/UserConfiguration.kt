package com.garnegsoft.hubs.api

import android.util.Log
import androidx.compose.animation.core.animateDecay
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

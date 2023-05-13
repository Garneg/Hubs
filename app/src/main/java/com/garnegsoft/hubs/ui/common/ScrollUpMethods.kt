package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState

sealed interface ScrollUpMethods {
    companion object {
        suspend fun scrollLazyList(lazyListState: LazyListState){
            lazyListState.scrollToItem(
                0,
                lazyListState.firstVisibleItemScrollOffset
            )

            lazyListState.animateScrollToItem(0)
        }

        suspend fun scrollNormal(scrollState: ScrollState) {
            scrollState.scrollTo(0)
        }
    }
}
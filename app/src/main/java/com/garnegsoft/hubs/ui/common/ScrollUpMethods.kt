package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState


sealed interface ScrollUpMethods {
    companion object {
        suspend fun scrollLazyList(lazyListState: LazyListState){
            if (lazyListState.firstVisibleItemIndex > 3) {
                lazyListState.scrollToItem(
                    2,
                    lazyListState.firstVisibleItemScrollOffset
                )
            }
            lazyListState.animateScrollToItem(0)
        }

        suspend fun scrollNormalList(scrollState: ScrollState) {
            scrollState.animateScrollTo(0)
        }
    }
}



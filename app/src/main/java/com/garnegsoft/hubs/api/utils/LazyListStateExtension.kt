package com.garnegsoft.hubs.api.utils

import androidx.compose.foundation.lazy.LazyListState

suspend fun LazyListState.animateShortScrollToItem(itemIndex: Int, offset: Int = 0, teleportItemOffset: Int = 1) {
	if (this.firstVisibleItemIndex > itemIndex + teleportItemOffset) {
		this.scrollToItem(itemIndex + teleportItemOffset)
	}
	if (firstVisibleItemIndex < itemIndex - teleportItemOffset) {
		scrollToItem(itemIndex - teleportItemOffset)
	}
	animateScrollToItem(itemIndex, offset)
}
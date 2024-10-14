package com.garnegsoft.hubs.ui.screens.imageViewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class ImageViewerState(val offlineResourcesRootPath: String) {
    private var showOnScreen: Boolean by mutableStateOf(false)
    private var model: Any? by mutableStateOf(null)

    val show by derivedStateOf { showOnScreen }
    val imageModel by derivedStateOf { model }

    fun showImage(imageModel: Any) {
        showOnScreen = true
        model = imageModel
    }

    fun showImageOfflineMode(imageModel: String) {
        showOnScreen = true
        model = offlineResourcesRootPath + imageModel.drop(16)
    }

    fun close() {
        showOnScreen = false
    }
}

@Composable
fun rememberImageViewerState(offlineResourcesRootPath: String): ImageViewerState {
    return remember { ImageViewerState(offlineResourcesRootPath) }
}
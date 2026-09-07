package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.feedBottomGradient(): Modifier {
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val backgroundColor = MaterialTheme.colors.background
    return this.drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.linearGradient(listOf(backgroundColor.copy(0.0f), backgroundColor.copy(0.4f)), start = Offset(0f, size.height-navigationBarHeight.toPx()-12.dp.toPx()), end = Offset(0f, size.height)),
            topLeft = Offset(0f, size.height-navigationBarHeight.toPx()-12.dp.toPx()))
    }
}
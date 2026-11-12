package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun HubsTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = 0.dp
) {
    TopAppBar(
        title,
        WindowInsets.statusBars.union(WindowInsets.displayCutout),
        modifier.drawBehind {
            drawRect(
                brush = Brush.linearGradient(0f to backgroundColor.run { Color(red * 0.8f, green * 0.85f, blue * 0.85f) },
                    1f to backgroundColor,
                    end = Offset(0f, size.height)
                    )
            )
        },
        navigationIcon,
        actions,
        Color.Transparent,
        contentColor,
        elevation
    )
}
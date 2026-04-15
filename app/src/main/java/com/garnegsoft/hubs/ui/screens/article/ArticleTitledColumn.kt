package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.garnegsoft.hubs.ui.common.TitledColumn


@Composable
fun ArticleTitledColumn(
    title: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    TitledColumn(
        title = title,
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        titleStyle = MaterialTheme.typography.subtitle2.copy(
            color = MaterialTheme.colors.onBackground.copy(0.5f)
        ),
        content = content
    )
}
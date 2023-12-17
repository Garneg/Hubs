package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun BasicTitledColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    title: @Composable () -> Unit,
    divider: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        title()
        divider?.invoke()
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            this.content()
        }

    }
}

@Composable
fun TitledColumn(
    title: String,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    spaceAfterTitle: Dp = 6.dp,
    titleStyle: TextStyle = MaterialTheme.typography.subtitle2.copy(color = MaterialTheme.colors.onSurface),
    content: @Composable ColumnScope.() -> Unit
) {
    BasicTitledColumn(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        title = { Text(text = title, style = titleStyle) },
        divider = { Spacer(modifier = Modifier.height(spaceAfterTitle)) },
        content = content
    )
}


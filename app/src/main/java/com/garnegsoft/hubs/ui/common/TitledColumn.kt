package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.ui.theme.HubsTheme



@Composable
fun BasicTitledColumn(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    divider: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        title()
        divider()
        this.content()
    }
}

@Composable
fun TitledColumn(
    modifier: Modifier = Modifier,
    title: String,
    titleStyle: TextStyle = MaterialTheme.typography.subtitle2,
    content: @Composable ColumnScope.() -> Unit
) {
    HubsTheme() {
        BasicTitledColumn(
            modifier = modifier,
            title = { Text(text = title, style = titleStyle)},
            divider = { Spacer(modifier = Modifier.height(8.dp)) },
            content = content
        )
    }
}


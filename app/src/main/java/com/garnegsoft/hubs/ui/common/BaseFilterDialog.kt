package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.garnegsoft.hubs.ui.screens.main.Spacer

@Composable
fun BaseFilterDialog(
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    title: String = "Фильтр",
    content: @Composable () -> Unit,
) {
    Dialog(
        properties = DialogProperties(true, true),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colors.surface)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.height(IntrinsicSize.Min)) {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(dp = 12.dp)
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                Spacer(dp = 12.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDone) {
                        Text(text = "Применить")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HubsFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = if (MaterialTheme.colors.isLight) ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colors.onSurface.copy(0.075f),
            selectedBackgroundColor = MaterialTheme.colors.secondary,
            selectedContentColor = MaterialTheme.colors.onSecondary
        ) else ChipDefaults.filterChipColors(),
        content = content
    )
}
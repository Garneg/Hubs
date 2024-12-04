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


@Composable
fun BaseFilterDialog(
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    title: String = "Фильтр",
    content: @Composable () -> Unit,
) {
    BaseTitledDialog(
        onDismiss = onDismiss,
        dialogProperties = DialogProperties(true, true),
        title = title
    ) {
        Column(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = "Отмена")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDone,
                    elevation = null,
                    //colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary, contentColor = MaterialTheme.colors.surface)
                ) {
                    Text(text = "Применить")
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
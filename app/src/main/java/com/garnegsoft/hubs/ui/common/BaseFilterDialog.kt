package com.garnegsoft.hubs.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDone,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colors.onSurface),
                        ) {
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
    val colors = if (MaterialTheme.colors.isLight) ChipDefaults.filterChipColors(
        backgroundColor = MaterialTheme.colors.onSurface.copy(0.075f),
        selectedBackgroundColor = MaterialTheme.colors.secondary,
        selectedContentColor = MaterialTheme.colors.onSecondary
    ) else ChipDefaults.filterChipColors()
    
    val backgroundColor by animateColorAsState(
        targetValue = colors.backgroundColor(enabled = enabled, selected = selected).value,
        animationSpec = tween(75)
    )
    val contentColor by animateColorAsState(
        targetValue = colors.contentColor(enabled = enabled, selected = selected).value,
        animationSpec = tween(75)
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .heightIn(min = ChipDefaults.MinHeight)
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.body2
        ) {
            
            content()
        }
    }
//    FilterChip(
//        selected = selected,
//        onClick = onClick,
//        modifier = modifier,
//        enabled = enabled,
//        shape = RoundedCornerShape(8.dp),
//        colors = if (MaterialTheme.colors.isLight) ChipDefaults.filterChipColors(
//            backgroundColor = MaterialTheme.colors.onSurface.copy(0.075f),
//            selectedBackgroundColor = MaterialTheme.colors.secondary,
//            selectedContentColor = MaterialTheme.colors.onSecondary
//        ) else ChipDefaults.filterChipColors(),
//        content = content
//    )
}
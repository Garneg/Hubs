package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun BaseTitledDialog(
    onDismiss: () -> Unit,
    title: String,
    dialogProperties: DialogProperties = DialogProperties(true, true),
    content: @Composable () -> Unit,

) {
    Dialog(
        properties = dialogProperties,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colors.surface)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colors.onSurface,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}
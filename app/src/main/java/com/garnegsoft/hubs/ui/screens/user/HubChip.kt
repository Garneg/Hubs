package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.hub.list.HubSnippet

@Composable
fun HubChip(
    hub: HubSnippet,
    onClick: () -> Unit
) {
    val isLightColor = MaterialTheme.colors.isLight
    val backgroundColor = if (isLightColor) remember {
        if (hub.relatedData?.isSubscribed == true)
            Color(0x1A4BB80D)
        else
            Color(0x1A0E73B8)
    } else {
        if (hub.relatedData?.isSubscribed == true)
            MaterialTheme.colors.onSurface.copy(0.25f)
        else
            MaterialTheme.colors.onSurface.copy(0.1f)
    }
    val textColor = if (isLightColor) remember {
        if (hub.relatedData?.isSubscribed == true)
            Color(0xFF194600)
        else
            Color(0xFF003255)
    } else {
        if (hub.relatedData?.isSubscribed == true)
            MaterialTheme.colors.onSurface.copy(1f)
        else
            MaterialTheme.colors.onSurface.copy(0.8f)

    }

    Text(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        text = hub.title,
        color = textColor,
        style = MaterialTheme.typography.body2
    )
}

@Composable
fun HubChip(
    title: String,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colors.onSurface.copy(0.08f))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        text = title,
        color = MaterialTheme.colors.onSurface.copy(0.85f),
        style = MaterialTheme.typography.body2
    )
}
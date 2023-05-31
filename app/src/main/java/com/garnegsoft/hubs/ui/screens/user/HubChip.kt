package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.hub.list.HubSnippet

@Composable
fun HubChip(
    hub: HubSnippet,
    onClick: () -> Unit
) {
    val color = remember {
        if (hub.relatedData?.isSubscribed == true)
            Color(0xFF4BB80D)
        else
            Color(0xFF0E73B8)
    }
    Text(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(color.copy(0.1f))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        text = hub.title,
        color = color.run {
                     copy(this.alpha, this.red * 0.4f, this.green * 0.4f, this.blue * 0.4f)
        },
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
            .background(MaterialTheme.colors.onSurface.copy(0.05f))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        text = title,
        color = MaterialTheme.colors.onSurface.copy(0.65f),
        style = MaterialTheme.typography.body2
    )
}
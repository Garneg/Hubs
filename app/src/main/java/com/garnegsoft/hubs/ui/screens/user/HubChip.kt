package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
    Text(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(hub.relatedData?.let {
                if (it.isSubscribed)
                    Color(0xFFD5F0C7)
                else
                    null
            } ?: Color(0xFFDBEAF3))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        text = hub.title,
        color = hub.relatedData?.let {
            if (it.isSubscribed)
                Color(0xFF475E38)
            else
                null
        } ?: Color(0xFF4B697C),
        style = MaterialTheme.typography.body2
    )
}
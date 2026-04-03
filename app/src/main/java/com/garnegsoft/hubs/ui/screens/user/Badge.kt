package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun Badge(title: String) {
    val badgeColor = MaterialTheme.colors.secondaryVariant
    Box(modifier = Modifier
        .clip(MaterialTheme.shapes.small)
        .border(
            1.dp,
            badgeColor,
            shape = MaterialTheme.shapes.small
        )
        .padding(horizontal = 8.dp, vertical = 6.dp)){
        Text(color = badgeColor, text = title)
    }
}
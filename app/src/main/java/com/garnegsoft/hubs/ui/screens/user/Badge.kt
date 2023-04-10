package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val badgeColor = Color(0xFF3D96EE)

@Composable
fun Badge(title: String) {
    Box(modifier = Modifier
        .clip(CircleShape)
        .border(
            1.dp,
            badgeColor,
            shape = CircleShape
        )
        .background(Color.White)
        .padding(horizontal = 12.dp, vertical = 8.dp)){
        Text(color = badgeColor, text = title)
    }
}
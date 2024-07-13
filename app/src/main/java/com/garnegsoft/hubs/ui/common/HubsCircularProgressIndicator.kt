package com.garnegsoft.hubs.ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun HubsCircularProgressIndicator(
	modifier: Modifier = Modifier,
	
	) {
	CircularProgressIndicator(
		modifier = modifier,
		color = MaterialTheme.colors.secondary
	)
}
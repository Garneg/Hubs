package com.garnegsoft.hubs.ui.common

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun HubsFilterActionButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	ExtendedFloatingActionButton(
		modifier = modifier,
		text = { Text("Фильтры") }, onClick = onClick,
		icon = {
			Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
		},
		)
}
package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.dataStore.HubsDataStore

@Composable
fun SettingsCardItem(
	title: String,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	onClick: () -> Unit,
	trailingSlot: @Composable () -> Unit = {},
	enabled: Boolean = true
) {
	Row(modifier = Modifier
		.fillMaxWidth()
		.clip(RoundedCornerShape(10.dp))
		.clickable(
			interactionSource = interactionSource,
			indication = rememberRipple(),
			enabled = enabled,
			onClick = onClick
		)
		.padding(start = 4.dp)
		.height(48.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(modifier = Modifier.weight(1f), text = title)
		trailingSlot()
	}
	
}
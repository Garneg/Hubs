package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.RippleDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun SettingsCardItem(
	title: String,
	interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
	onClick: () -> Unit,
	trailingIcon: @Composable () -> Unit = {},
	enabled: Boolean = true
) {
	Row(modifier = Modifier
		.fillMaxWidth()
		.clip(RoundedCornerShape(10.dp))
		.clickable(
			interactionSource = interactionSource,
			indication = ripple(),
			enabled = enabled,
			onClick = onClick
		)
		.padding(start = 4.dp)
		.height(48.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(modifier = Modifier.weight(1f), text = title)
		Box(
			modifier = Modifier.sizeIn(48.dp),
			contentAlignment = Alignment.Center
		) {
			trailingIcon()
		}
		
	}
	
}
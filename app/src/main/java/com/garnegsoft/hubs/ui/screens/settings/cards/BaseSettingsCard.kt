package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.common.BasicTitledColumn

@Composable
fun SettingsCard(
	title: String,
	content: @Composable ColumnScope.() -> Unit
) {
	Column(
		modifier = Modifier
			.clip(RoundedCornerShape(26.dp))
			.background(MaterialTheme.colors.surface)
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(4.dp)
	) {
		BasicTitledColumn(
			title = {
				Text(
					modifier = Modifier.padding(bottom = 12.dp),
					text = title, style = MaterialTheme.typography.subtitle1
				)
			},
			divider = {
				// Divider()
			},
			
			) {
			Column(
				verticalArrangement = Arrangement.spacedBy(4.dp),
				content = content
			)
		}
	}
}

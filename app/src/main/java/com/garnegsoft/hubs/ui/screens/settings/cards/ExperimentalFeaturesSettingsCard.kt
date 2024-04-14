package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreenViewModel


@Composable
fun ExperimentalFeaturesSettingsCard(
	viewModel: SettingsScreenViewModel
) {
	val context = LocalContext.current
	val commentsDisplayMode by HubsDataStore.Settings
		.getValueFlow(context, HubsDataStore.Settings.CommentsDisplayMode)
		.collectAsState(initial = null)
	
	commentsDisplayMode?.let {
		SettingsCard(
			title = "Экспериментальные функции"
		) {
			Column(
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
			
			}
		}
		
	}
	
}
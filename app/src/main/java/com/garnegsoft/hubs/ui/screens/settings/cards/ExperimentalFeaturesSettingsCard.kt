package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreenViewModel
import com.garnegsoft.hubs.ui.screens.settings.noRipple
import kotlinx.coroutines.launch


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
			val commentsModeSwitchInteractionSource =
				remember { MutableInteractionSource() }
			var useThreadsComments by remember { mutableStateOf(it == HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Threads.ordinal) }
			Column(
				verticalArrangement = Arrangement.spacedBy(4.dp)
			) {
				
				Row(modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(10.dp))
					.clickable(
						interactionSource = commentsModeSwitchInteractionSource,
						indication = rememberRipple()
					) {
						useThreadsComments = !useThreadsComments
						viewModel.viewModelScope.launch {
							HubsDataStore.Settings
								.edit(
									context,
									HubsDataStore.Settings.CommentsDisplayMode,
									if (useThreadsComments) HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Threads.ordinal
									else HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Default.ordinal
								)
						}
					}
					.padding(start = 4.dp)
					.height(48.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						modifier = Modifier.weight(1f),
						text = "Скрывать ветки комментариев"
					)
					
					CompositionLocalProvider(LocalRippleTheme provides noRipple) {
						Checkbox(
							checked = useThreadsComments,
							onCheckedChange = {
								useThreadsComments = it
								viewModel.viewModelScope.launch {
									HubsDataStore.Settings
										.edit(
											context,
											HubsDataStore.Settings.CommentsDisplayMode,
											if (useThreadsComments) HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Threads.ordinal
											else HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Default.ordinal
										)
								}
								
							},
							interactionSource = commentsModeSwitchInteractionSource
						)
					}
				}
				
				
			}
		}
		
	}
	
}
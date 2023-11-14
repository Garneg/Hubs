package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreenViewModel
import com.garnegsoft.hubs.ui.screens.settings.noRipple

@Composable
fun AppearanceSettingsCard(
	viewModel: SettingsScreenViewModel,
	onFeedSettings: () -> Unit,
	onArticleScreenSettings: () -> Unit
) {
	val context = LocalContext.current
	val theme by viewModel.getTheme(context).collectAsState(initial = null)
	theme.let {
		SettingsCard(title = "Внешний вид") {
			// TODO: Create special item for settings. Replace checkboxes with switches in Material3
			val isSystemInDarkTheme = isSystemInDarkTheme()
			var useSystemDefinedTheme by rememberSaveable {
				mutableStateOf(
					it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined ||
						it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Undetermined
				)
			}
			val sharedInteractionSource = remember { MutableInteractionSource() }
			var useDarkTheme by remember {
				mutableStateOf(
					if (it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined) {
						isSystemInDarkTheme
					} else it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
				)
			}
			
			Row(modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(10.dp))
				.clickable(
					interactionSource = sharedInteractionSource,
					indication = rememberRipple()
				) {
					useSystemDefinedTheme = !useSystemDefinedTheme
					viewModel.setTheme(
						context,
						if (useSystemDefinedTheme) {
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined
						} else {
							if (isSystemInDarkTheme)
								HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
							else
								HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
						}
					)
				}
				.padding(start = 4.dp)
				.height(48.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(modifier = Modifier.weight(1f), text = "Системная тема")
				CompositionLocalProvider(LocalRippleTheme provides noRipple) {
					
					Checkbox(
						checked = useSystemDefinedTheme,
						onCheckedChange = {
							useSystemDefinedTheme = !useSystemDefinedTheme
							viewModel.setTheme(
								context,
								if (useSystemDefinedTheme) {
									HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined
								} else {
									if (isSystemInDarkTheme)
										HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
									else
										HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
								}
							)
						},
						interactionSource = sharedInteractionSource
					)
				}
			}
			
			val isDarkThemeInteractionSource =
				remember { MutableInteractionSource() }
			
			LaunchedEffect(
				key1 = useSystemDefinedTheme,
				key2 = isSystemInDarkTheme,
				block = {
					when {
						(useSystemDefinedTheme && isSystemInDarkTheme && !useDarkTheme) -> useDarkTheme =
							true
						
						(useSystemDefinedTheme && !isSystemInDarkTheme && useDarkTheme) -> useDarkTheme =
							false
					}
				})
			Row(modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(10.dp))
				.clickable(
					enabled = !useSystemDefinedTheme,
					interactionSource = isDarkThemeInteractionSource,
					indication = rememberRipple()
				) {
					useDarkTheme = !useDarkTheme
					viewModel.setTheme(
						context,
						if (useDarkTheme)
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
						else
							HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
					)
				}
				.padding(start = 4.dp)
				.heightIn(min = 48.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					modifier = Modifier
						.weight(1f)
						.alpha(if (useSystemDefinedTheme) 0.5f else 1f),
					text = "Тёмная тема"
				)
				CompositionLocalProvider(LocalRippleTheme provides noRipple) {
					
					Checkbox(
						checked = useDarkTheme,
						enabled = !useSystemDefinedTheme,
						onCheckedChange = {
							useDarkTheme = it
							viewModel.setTheme(
								context,
								if (useDarkTheme)
									HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
								else
									HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
							)
						},
						interactionSource = isDarkThemeInteractionSource
					)
				}
			}
			
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(10.dp))
					.clickable(onClick = onArticleScreenSettings)
					.padding(start = 4.dp)
					.heightIn(min = 48.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(text = "Внешний вид статьи")
					Text(
						text = "Размер шрифта, межстрочный интервал и т.д.",
						fontSize = 11.sp,
						color = MaterialTheme.colors.onSurface.copy(0.5f)
					)
				}
				Icon(
					modifier = Modifier.padding(12.dp),
					imageVector = Icons.Default.ArrowForward,
					contentDescription = null
				)
			}
			SettingsCardItem(
				title = "Внешний вид ленты", onClick = onFeedSettings,
				trailingIcon = {
					Icon(
						modifier = Modifier.padding(12.dp),
						imageVector = Icons.Default.ArrowForward,
						contentDescription = null)
				}
			)
		}
		
	}
}
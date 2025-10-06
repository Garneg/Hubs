package com.garnegsoft.hubs.ui.screens.settings.cards


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.BaseMenuContainer
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreenViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppearanceSettingsCard(
	viewModel: SettingsScreenViewModel,
	onFeedSettings: () -> Unit,
	onArticleScreenSettings: () -> Unit
) {
	val context = LocalContext.current
	val theme by viewModel.getTheme(context).collectAsState(initial = null)
	theme.let { themeMode ->
		SettingsCard(title = "Внешний вид") {
			var showSelectThemeMenu by remember { mutableStateOf(false) }
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(10.dp))
//				.clickable {
//					showSelectThemeMenu = true
//				}
					.padding(start = 4.dp)
					.height(48.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(modifier = Modifier.weight(1f), text = "Тема:")
				Box {
					Row(
						modifier = Modifier
							.clip(RoundedCornerShape(8.dp))
							.clickable { showSelectThemeMenu = true }
							.padding(vertical = 6.dp)
							.padding(start = 12.dp, end = 4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = when (themeMode) {
								HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light -> "Светлая"
								HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark -> "Тёмная"
								else -> "Системная"
							}
						)
						Spacer(modifier = Modifier.width(4.dp))

						Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)

					}
					val showMenuTransition = updateTransition(showSelectThemeMenu)

					val scaleFactor by showMenuTransition.animateFloat { if (it) 1f else 0f }
					if (showSelectThemeMenu || showMenuTransition.currentState || showMenuTransition.targetState) {
						Popup(
							onDismissRequest = {
								showSelectThemeMenu = false
							},
							properties = PopupProperties(true)
						) {
							Box(
								modifier = Modifier
									.offset {
										IntOffset(x = 8.dp.roundToPx(), y = 0)
									}
									.graphicsLayer {
										alpha = scaleFactor
										translationY = -(size.height - (size.height * scaleFactor))/2
										scaleY = scaleFactor
									}
								) {
								BaseMenuContainer(modifier = Modifier.padding(end = 32.dp)) {
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.heightIn(48.dp)
											.clickable {
												viewModel.setTheme(
													context,
													HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined
												)
												showSelectThemeMenu = false
											}
											.padding(horizontal = 16.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Text(
											modifier = Modifier.weight(1f),
											text = "Системная",
											style = MaterialTheme.typography.body1
										)
										if (themeMode == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined ||
											themeMode == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Undetermined
										) {
											Icon(
												modifier = Modifier.size(20.dp),
												imageVector = Icons.Default.Done,
												contentDescription = "Выбрано"
											)
										}
									}
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.heightIn(48.dp)
											.clickable {
												viewModel.setTheme(
													context,
													HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
												)
												showSelectThemeMenu = false
											}
											.padding(horizontal = 16.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Text(
											modifier = Modifier.weight(1f),
											text = "Светлая",
											style = MaterialTheme.typography.body1
										)

										if (themeMode == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light) {
											Icon(
												modifier = Modifier.size(20.dp),
												imageVector = Icons.Default.Done,
												contentDescription = "Выбрано"
											)
										}
									}
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.heightIn(48.dp)
											.clickable {
												viewModel.setTheme(
													context,
													HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
												)
												showSelectThemeMenu = false
											}
											.padding(horizontal = 16.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Text(
											modifier = Modifier.weight(1f),
											text = "Тёмная",
											style = MaterialTheme.typography.body1
										)

										if (themeMode == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark) {
											Icon(
												modifier = Modifier.size(20.dp),
												imageVector = Icons.Default.Done,
												contentDescription = "Выбрано"
											)
										}
									}
								}
							}

						}
					}
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
//					Text(
//						text = "Размер шрифта, межстрочный интервал и т.д.",
//						fontSize = 11.sp,
//						color = MaterialTheme.colors.onSurface.copy(0.5f)
//					)
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
						contentDescription = null
					)
				}
			)
		}
		
	}
}
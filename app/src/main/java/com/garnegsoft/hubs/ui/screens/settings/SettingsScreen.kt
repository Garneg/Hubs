package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.settingsDataStore
import com.garnegsoft.hubs.api.dataStore.settingsDataStoreFlow
import com.garnegsoft.hubs.api.dataStore.settingsDataStoreFlowWithDefault
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsScreenViewModel : ViewModel() {
	fun getTheme(context: Context): Flow<HubsDataStore.Settings.Keys.ThemeModes?> {
		return context.settingsDataStoreFlow(HubsDataStore.Settings.Keys.Theme)
			.map {
				it?.let {
					HubsDataStore.Settings.Keys.ThemeModes.values().get(it)
				} ?: HubsDataStore.Settings.Keys.ThemeModes.Undetermined
			}
	}
	
	fun setTheme(context: Context, theme: HubsDataStore.Settings.Keys.ThemeModes) {
		viewModelScope.launch(Dispatchers.IO) {
			context.settingsDataStore.edit {
				it.set(HubsDataStore.Settings.Keys.Theme, theme.ordinal)
			}
		}
	}
}

@Composable
fun SettingsScreen(
	onBack: () -> Unit,
	onArticleScreenSettings: () -> Unit,
) {
	val viewModel = viewModel<SettingsScreenViewModel>()
	val context = LocalContext.current
	
	val theme by viewModel.getTheme(context).collectAsState(initial = null)
	
	Scaffold(
		topBar = {
			TopAppBar(
				elevation = 0.dp,
				title = { Text("Настройки") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Назад"
						)
					}
				}
			)
		},
	) {
		val noRipple = object : RippleTheme {
			@Composable
			override fun defaultColor() = Color.Unspecified
			
			@Composable
			override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
		}
		Column(
			modifier = Modifier
				.padding(it)
				.verticalScroll(rememberScrollState())
				.padding(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			theme?.let {
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
								text = "Внешний вид", style = MaterialTheme.typography.subtitle1
							)
						},
						divider = {
							// Divider()
						}
					) {
						
						Column(
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							// TODO: Create special item for settings. Replace checkboxes with switches in Material3
							val isSystemInDarkTheme = isSystemInDarkTheme()
							var useSystemDefinedTheme by rememberSaveable {
								mutableStateOf(
									it == HubsDataStore.Settings.Keys.ThemeModes.SystemDefined ||
										it == HubsDataStore.Settings.Keys.ThemeModes.Undetermined
								)
							}
							val sharedInteractionSource = remember { MutableInteractionSource() }
							var useDarkTheme by remember {
								mutableStateOf(
									if (it == HubsDataStore.Settings.Keys.ThemeModes.SystemDefined) {
										isSystemInDarkTheme
									} else it == HubsDataStore.Settings.Keys.ThemeModes.Dark
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
											HubsDataStore.Settings.Keys.ThemeModes.SystemDefined
										} else {
											if (isSystemInDarkTheme)
												HubsDataStore.Settings.Keys.ThemeModes.Dark
											else
												HubsDataStore.Settings.Keys.ThemeModes.Light
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
													HubsDataStore.Settings.Keys.ThemeModes.SystemDefined
												} else {
													if (isSystemInDarkTheme)
														HubsDataStore.Settings.Keys.ThemeModes.Dark
													else
														HubsDataStore.Settings.Keys.ThemeModes.Light
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
											HubsDataStore.Settings.Keys.ThemeModes.Dark
										else
											HubsDataStore.Settings.Keys.ThemeModes.Light
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
													HubsDataStore.Settings.Keys.ThemeModes.Dark
												else
													HubsDataStore.Settings.Keys.ThemeModes.Light
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
						}
					}
					
					
				}
			}
			val commentsDisplayMode by context.settingsDataStoreFlowWithDefault(
				HubsDataStore.Settings.Keys.Comments.CommentsDisplayMode,
				HubsDataStore.Settings.Keys.Comments.CommentsDisplayModes.Default.ordinal
			).collectAsState(initial = null)
			
			commentsDisplayMode?.let {
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
								text = "Комментарии", style = MaterialTheme.typography.subtitle1
							)
						},
						divider = {
							// Divider()
						}
					) {
						val commentsModeSwitchInteractionSource =
							remember { MutableInteractionSource() }
						var useThreadsComments by remember { mutableStateOf(it == HubsDataStore.Settings.Keys.Comments.CommentsDisplayModes.Threads.ordinal) }
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
												context.settingsDataStore.edit { prefs ->
													prefs.set(
														HubsDataStore.Settings.Keys.Comments.CommentsDisplayMode,
														if (it) HubsDataStore.Settings.Keys.Comments.CommentsDisplayModes.Threads.ordinal else HubsDataStore.Settings.Keys.Comments.CommentsDisplayModes.Default.ordinal
													)
												}
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
			
			
		}
	}
	
}

package com.garnegsoft.hubs.ui.screens.settings.cards


import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
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
                val showMenuTransition = updateTransition(showSelectThemeMenu)
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
                        val rotationAnimated by showMenuTransition.animateFloat { if (it) 180f else 0f }
                        Icon(
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = rotationAnimated
                                },
                            imageVector = Icons.Default.ArrowDropDown, contentDescription = null
                        )

                    }

                    val scaleFactor by showMenuTransition.animateFloat { if (it) 1f else 0f }
                    val offsetFactor by showMenuTransition.animateFloat { if (it) 0f else 1f }
                    if (showSelectThemeMenu || showMenuTransition.currentState || showMenuTransition.targetState) {
                        Popup(
                            popupPositionProvider = object : PopupPositionProvider {
                                override fun calculatePosition(
                                    anchorBounds: IntRect,
                                    windowSize: IntSize,
                                    layoutDirection: LayoutDirection,
                                    popupContentSize: IntSize
                                ): IntOffset {
                                    return IntOffset(
                                        anchorBounds.right - popupContentSize.width,
                                        anchorBounds.bottom
                                    )
                                }

                            },
                            onDismissRequest = {
                                showSelectThemeMenu = false
                            },
                            properties = PopupProperties(true)
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(x = 8.dp.roundToPx(), y = -8.dp.roundToPx())
                                    }
                                    .graphicsLayer {
                                        alpha = scaleFactor
                                        translationY = -8.dp.roundToPx() * offsetFactor
//										scaleY = scaleFactor
                                    }
                            ) {
                                BaseMenuContainer() {
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
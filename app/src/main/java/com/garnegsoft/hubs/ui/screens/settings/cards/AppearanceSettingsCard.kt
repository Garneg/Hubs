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
import com.garnegsoft.hubs.ui.screens.settings.SettingsCardItemPicker
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
            SettingsCardItemPicker(
                title = "Тема:",
                items = listOf("Светлая", "Тёмная", "Системная"),
                pickedItemIndex = when (themeMode) {
                    HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light -> 0
                    HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark -> 1
                    else -> 2
                },
                onItemPicked = { index ->
                    when (index) {
                        0 -> {
                            viewModel.setTheme(
                                context,
                                HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Light
                            )
                        }

                        1 -> {
                            viewModel.setTheme(
                                context,
                                HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark
                            )
                        }

                        else -> {
                            viewModel.setTheme(
                                context,
                                HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined
                            )
                        }
                    }
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onArticleScreenSettings)
                    .padding(start = 12.dp)
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
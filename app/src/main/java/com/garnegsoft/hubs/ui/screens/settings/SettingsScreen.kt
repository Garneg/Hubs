package com.garnegsoft.hubs.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.settingsDataStore
import com.garnegsoft.hubs.settingsDataStoreFlow
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.flow.map

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val themeInt = context.settingsDataStoreFlow(HubsDataStore.Settings.Keys.Theme).collectAsState(
        initial = 0
    ).value ?: 0
    HubsDataStore.Settings.Keys.ThemeMode.SystemDefined
    val theme = HubsDataStore.Settings.Keys.ThemeMode.values()[themeInt]
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
        Column(modifier = Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
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
                        //                        Divider()
                    }
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {


                        var useSystemDefinedTheme by rememberSaveable { mutableStateOf(false) }
                        val sharedInteractionSource = remember { MutableInteractionSource() }
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = sharedInteractionSource,
                                indication = rememberRipple()
                            ) { useSystemDefinedTheme = !useSystemDefinedTheme }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .height(50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(modifier = Modifier.weight(1f), text = "Системная тема")
                            CompositionLocalProvider(LocalRippleTheme provides noRipple) {

                                Checkbox(
                                    checked = useSystemDefinedTheme,
                                    onCheckedChange = {
                                        useSystemDefinedTheme = !useSystemDefinedTheme
                                    },
                                    interactionSource = sharedInteractionSource
                                )
                            }
                        }
                        val isDarkThemeInteractionSource = remember { MutableInteractionSource() }
                        var isChecked by remember { mutableStateOf(false) }
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = isDarkThemeInteractionSource,
                                indication = rememberRipple()
                            ) {
                                isChecked = !isChecked
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .height(50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (useSystemDefinedTheme) 0.5f else 1f),
                                text = "Темная тема"
                            )


                            CompositionLocalProvider(LocalRippleTheme provides noRipple) {
                                Checkbox(
                                    checked = isChecked,
                                    enabled = !useSystemDefinedTheme,
                                    onCheckedChange = { isChecked = it },
                                    interactionSource = isDarkThemeInteractionSource
                                )
                            }
                        }
                    }
                }

            }


        }
    }

}
package com.garnegsoft.hubs.ui.screens.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.DialogHost
import androidx.navigation.compose.DialogNavigator
import coil.compose.AsyncImage
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.theme.HubsTheme

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FilterDialog() {
    HubsTheme() {


        Box() {
            Dialog(
                properties = DialogProperties(true, true),
                onDismissRequest = { Log.e("aboboa", "adlajflsd") }) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colors.surface)
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Text(
                            text = "Фильтр",
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.subtitle1)
                        Spacer(dp = 12.dp)
                        var showLast by remember { mutableStateOf(true) }
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {

                            TitledColumn(title = "Сначала показывать") {
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HubsFilterChip(selected = showLast, onClick = { showLast = true }) {
                                        Text(text = "Новые")
                                    }
                                    HubsFilterChip(
                                        selected = !showLast,
                                        onClick = { showLast = false }) {
                                        Text(text = "Лучшие")
                                    }
                                }

                            }
                            if (showLast) {
                                TitledColumn(title = "Порог рейтинга") {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HubsFilterChip(
                                            selected = showLast,
                                            onClick = { showLast = true }) {
                                            Text(text = "Все")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "≥0")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "≥10")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "≥25")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "≥50")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "≥100")
                                        }
                                    }
                                }
                            }
                            else {
                                TitledColumn(title = "Период") {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        HubsFilterChip(
                                            selected = showLast,
                                            onClick = { showLast = true }) {
                                            Text(text = "Сутки")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "Неделя")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "Месяц")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "Год")
                                        }
                                        HubsFilterChip(
                                            selected = !showLast,
                                            onClick = { showLast = false }) {
                                            Text(text = "Все время")
                                        }

                                    }
                                }
                            }

                            TitledColumn(title = "Уровень сложности") {
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HubsFilterChip(selected = showLast, onClick = { showLast = true }) {
                                        Text(text = "Все")
                                    }
                                    HubsFilterChip(
                                        selected = !showLast,
                                        onClick = { showLast = false }) {
                                        Text(text = "Простой")
                                    }
                                    HubsFilterChip(
                                        selected = !showLast,
                                        onClick = { showLast = false }) {
                                        Text(text = "Средний")
                                    }
                                    HubsFilterChip(
                                        selected = !showLast,
                                        onClick = { showLast = false }) {
                                        Text(text = "Сложный")
                                    }

                                }

                            }


                        }
                        Spacer(dp = 12.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { /*TODO*/ }) {
                                Text(text = "Применить")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { /*TODO*/ }) {
                                Text(text = "Отмена")
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.Spacer(dp: Dp) {
    Spacer(modifier = Modifier.height(dp))
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HubsFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable RowScope.() -> Unit)
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = if (MaterialTheme.colors.isLight) ChipDefaults.filterChipColors(
            backgroundColor = MaterialTheme.colors.onSurface.copy(0.075f),
            selectedBackgroundColor = MaterialTheme.colors.secondary,
            selectedContentColor = MaterialTheme.colors.onSecondary
        ) else ChipDefaults.filterChipColors(),
        content = content
    )
}
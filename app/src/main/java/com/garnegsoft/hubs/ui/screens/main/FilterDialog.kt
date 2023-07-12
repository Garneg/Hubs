package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.theme.HubsTheme


@Composable
fun FilterDialog(
    defaultValues: ArticlesFilterDialogResult,
    onDismiss: () -> Unit,
    onDone: (ArticlesFilterDialogResult) -> Unit
) {

    var showLast by rememberSaveable {
        mutableStateOf(defaultValues.showLast)
    }
    var minRating by rememberSaveable {
        mutableStateOf(defaultValues.minRating)
    }
    var period by rememberSaveable {
        mutableStateOf(defaultValues.period)
    }
    var complexity by rememberSaveable {
        mutableStateOf(defaultValues.complexity)
    }

    BaseFilterDialog(onDismiss = onDismiss, onDone = {
        onDone(
            ArticlesFilterDialogResult(
                showLast,
                minRating,
                period,
                complexity
            )
        )
    }) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            TitledColumn(title = "Сначала показывать") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
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
                            selected = minRating == -1,
                            onClick = { minRating = -1 }) {
                            Text(text = "Все")
                        }
                        HubsFilterChip(
                            selected = minRating == 0,
                            onClick = { minRating = 0 }) {
                            Text(text = "≥0")
                        }
                        HubsFilterChip(
                            selected = minRating == 10,
                            onClick = { minRating = 10 }) {
                            Text(text = "≥10")
                        }
                        HubsFilterChip(
                            selected = minRating == 25,
                            onClick = { minRating = 25 }) {
                            Text(text = "≥25")
                        }
                        HubsFilterChip(
                            selected = minRating == 50,
                            onClick = { minRating = 50 }) {
                            Text(text = "≥50")
                        }
                        HubsFilterChip(
                            selected = minRating == 100,
                            onClick = { minRating = 100 }) {
                            Text(text = "≥100")
                        }
                    }
                }
            } else {
                TitledColumn(title = "Период") {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HubsFilterChip(
                            selected = period == FilterPeriod.Day,
                            onClick = { period = FilterPeriod.Day }) {
                            Text(text = "Сутки")
                        }
                        HubsFilterChip(
                            selected = period == FilterPeriod.Week,
                            onClick = { period = FilterPeriod.Week }) {
                            Text(text = "Неделя")
                        }
                        HubsFilterChip(
                            selected = period == FilterPeriod.Month,
                            onClick = { period = FilterPeriod.Month }) {
                            Text(text = "Месяц")
                        }
                        HubsFilterChip(
                            selected = period == FilterPeriod.Year,
                            onClick = { period = FilterPeriod.Year }) {
                            Text(text = "Год")
                        }
                        HubsFilterChip(
                            selected = period == FilterPeriod.AllTime,
                            onClick = { period = FilterPeriod.AllTime }) {
                            Text(text = "Все время")
                        }

                    }
                }
            }

            TitledColumn(title = "Уровень сложности") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HubsFilterChip(
                        selected = complexity == PostComplexity.None,
                        onClick = { complexity = PostComplexity.None }) {
                        Text(text = "Все")
                    }
                    HubsFilterChip(
                        selected = complexity == PostComplexity.Low,
                        onClick = { complexity = PostComplexity.Low }) {
                        Text(text = "Простой")
                    }
                    HubsFilterChip(
                        selected = complexity == PostComplexity.Medium,
                        onClick = { complexity = PostComplexity.Medium }) {
                        Text(text = "Средний")
                    }
                    HubsFilterChip(
                        selected = complexity == PostComplexity.High,
                        onClick = { complexity = PostComplexity.High }) {
                        Text(text = "Сложный")
                    }

                }

            }


        }
    }


}

data class ArticlesFilterDialogResult(
    val showLast: Boolean,
    val minRating: Int = -1,
    val period: FilterPeriod = FilterPeriod.Day,
    val complexity: PostComplexity
)

enum class FilterPeriod {
    Day,
    Week,
    Month,
    Year,
    AllTime
}

@Composable
fun ColumnScope.Spacer(dp: Dp) {
    Spacer(modifier = Modifier.height(dp))
}


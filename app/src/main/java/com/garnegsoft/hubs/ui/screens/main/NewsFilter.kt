package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn
import kotlinx.serialization.Serializable

@Composable
fun NewsFilterDialog(
    defaultValues: NewsFilter,
    onDismiss: () -> Unit,
    onDone: (NewsFilter) -> Unit
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
    
    BaseFilterDialog(onDismiss = onDismiss, onDone = {
        onDone(
            NewsFilter(
                showLast,
                minRating,
                period,
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

        }
    }
}


@Serializable
data class NewsFilter(
    val showLast: Boolean,
    val minRating: Int = -1,
    val period: FilterPeriod,
) : Filter {
    override fun toArgsMap(): Map<String, String> {
        val argsMap: Map<String, String> =
            if (showLast) {
                if (minRating == -1) {
                    mapOf(
                        "sort" to "rating",
                    )
                } else {
                    mapOf(
                        "sort" to "rating",
                        "score" to minRating.toString()
                    )
                }
            } else {
                mapOf(
                    "period" to when (period) {
                        FilterPeriod.Day -> "daily"
                        FilterPeriod.Week -> "weekly"
                        FilterPeriod.Month -> "monthly"
                        FilterPeriod.Year -> "yearly"
                        FilterPeriod.AllTime -> "alltime"
                        else -> ""
                    },
                )
            }
        return argsMap
    }
    
    override fun getTitle(): String {
        if (showLast){
            if (minRating == -1)
                return "Все подряд"
            else
                return "Новые с рейтингом ≥$minRating"
        } else {
            return "Лучшие за " +
            when (period) {
                FilterPeriod.Day -> "сутки"
                FilterPeriod.Week -> "неделю"
                FilterPeriod.Month -> "месяц"
                FilterPeriod.Year -> "год"
                FilterPeriod.AllTime -> "все время"
            }
        }
    }
    
    companion object {
        
        val defaultValues = NewsFilter(showLast = true, period = FilterPeriod.Day)
    }
}
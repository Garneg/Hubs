package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn


@Composable
fun ArticlesFilterDialog(
	defaultValues: ArticlesFilterState,
	onDismiss: () -> Unit,
	onDone: (ArticlesFilterState) -> Unit
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
			ArticlesFilterState(
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
						selected = complexity == PublicationComplexity.None,
						onClick = { complexity = PublicationComplexity.None }) {
						Text(text = "Все")
					}
					HubsFilterChip(
						selected = complexity == PublicationComplexity.Low,
						onClick = { complexity = PublicationComplexity.Low }) {
						Text(text = "Простой")
					}
					HubsFilterChip(
						selected = complexity == PublicationComplexity.Medium,
						onClick = { complexity = PublicationComplexity.Medium }) {
						Text(text = "Средний")
					}
					HubsFilterChip(
						selected = complexity == PublicationComplexity.High,
						onClick = { complexity = PublicationComplexity.High }) {
						Text(text = "Сложный")
					}
					
				}
				
			}
			
			
		}
	}
}

data class ArticlesFilterState(
	val showLast: Boolean,
	val minRating: Int = -1,
	val period: FilterPeriod = FilterPeriod.Day,
	val complexity: PublicationComplexity
) : Filter {
	override fun toArgsMap(): Map<String, String> {
		var argsMap: Map<String, String> =
			if (showLast) {
				if (minRating == -1) {
					mapOf("sort" to "rating",)
				} else {
					mapOf(
						"sort" to "rating",
						"score" to minRating.toString()
					)
				}
			} else {
				mapOf(
					"sort" to "date",
					"period" to when (period) {
						FilterPeriod.Day -> "daily"
						FilterPeriod.Week -> "weekly"
						FilterPeriod.Month -> "monthly"
						FilterPeriod.Year -> "yearly"
						FilterPeriod.AllTime -> "alltime"
					},
				)
			}
		
		if (complexity != PublicationComplexity.None) {
			argsMap += mapOf(
				"complexity" to when (complexity) {
					PublicationComplexity.Low -> "easy"
					PublicationComplexity.Medium -> "medium"
					PublicationComplexity.High -> "hard"
					else -> throw IllegalArgumentException("mapping of this complexity is not supported")
				}
			)
		}
		return argsMap
	}
	
	override fun getTitle(): String {
		return if (showLast) {
			if (minRating == -1) {
				"Все подряд"
			} else {
				"Новые с рейтингом ≥${minRating}"
			}
		} else {
			"Лучшие за " + when (period) {
				FilterPeriod.Day -> "сутки"
				FilterPeriod.Week -> "неделю"
				FilterPeriod.Month -> "месяц"
				FilterPeriod.Year -> "год"
				FilterPeriod.AllTime -> "все время"
			}
		} + when (complexity) {
			PublicationComplexity.High -> ", сложные"
			PublicationComplexity.Medium -> ", средние"
			PublicationComplexity.Low -> ", простые"
			else -> ""
		}
		
	}
	
}





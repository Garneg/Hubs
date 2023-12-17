package com.garnegsoft.hubs.ui.screens.main

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn


data class MyFeedFilter(
	val showArticles: Boolean,
	val showNews: Boolean,
	val minRating: Int,
	val complexity: PublicationComplexity
) : Filter {
	override fun toArgsMap(): Map<String, String> {
		return mutableMapOf<String, String>().apply {
			var argsCount = 0
			if (showArticles) {
				this.put("types[0]", "articles")
				argsCount++
			}
			if (showNews)
				this.put("types[$argsCount]", "news")
			
			put(
				"complexity", when (complexity) {
					PublicationComplexity.None -> "all"
					PublicationComplexity.Low -> "easy"
					PublicationComplexity.Medium -> "medium"
					PublicationComplexity.High -> "hard"
					else -> throw IllegalArgumentException("mapping of this complexity is not supported")
				}
			)
			
			if (minRating == -1) {
				put("score", "all")
			} else {
				put("score", minRating.toString())
			}
			
		}
	}
	
	override fun getTitle(): String {
		return buildString {
			if (showNews && showArticles) append("Статьи & Новости")
			else if (showArticles) append("Статьи") else append("Новости")
			
			if (minRating > -1)
				append(" с рейтингом ≥${minRating}")
			
			when (complexity) {
				PublicationComplexity.High -> append(", сложные")
				PublicationComplexity.Medium -> append(", средние")
				PublicationComplexity.Low -> append(", простые")
				else -> {}
			}
		}
		
	}
	
}

@Composable
fun MyFeedFilter(
	defaultValues: MyFeedFilter,
	onDismiss: () -> Unit,
	onDone: (MyFeedFilter) -> Unit
) {
	var showArticles by rememberSaveable {
		mutableStateOf(defaultValues.showArticles)
	}
	var showNews by rememberSaveable {
		mutableStateOf(defaultValues.showNews)
	}
	
	var minRating by rememberSaveable {
		mutableStateOf(defaultValues.minRating)
	}
	
	var complexity by rememberSaveable {
		mutableStateOf(defaultValues.complexity)
	}
	
	val context = LocalContext.current
	
	BaseFilterDialog(
		onDismiss = onDismiss,
		onDone = {
			if (!showArticles && !showNews){
				Toast.makeText(context, "Выберите 1 тип публикаций", Toast.LENGTH_SHORT).show()
			} else {
				onDone(
					MyFeedFilter(
						showNews = showNews,
						showArticles = showArticles,
						minRating = minRating,
						complexity = complexity
					)
				)
			}
		}
	) {
		Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
			
			
			TitledColumn(title = "Тип публикации") {
				Row(
					modifier = Modifier.horizontalScroll(rememberScrollState()),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					HubsFilterChip(
						selected = showArticles,
						onClick = { showArticles = !showArticles }) {
						Text(text = "Статьи")
					}
					HubsFilterChip(
						selected = showNews,
						onClick = { showNews = !showNews }) {
						Text(text = "Новости")
					}
				}
			}
			
			TitledColumn(title = "Порог рейтинга") {
				Row(
					modifier = Modifier.horizontalScroll(rememberScrollState()),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					HubsFilterChip(
						selected = minRating == -1,
						onClick = { minRating = -1 }
					) {
						Text(text = "Все")
					}
					HubsFilterChip(
						selected = minRating == 0,
						onClick = { minRating = 0 }
					) {
						Text(text = "≥0")
					}
					HubsFilterChip(
						selected = minRating == 10,
						onClick = { minRating = 10 }
					) {
						Text(text = "≥10")
					}
					HubsFilterChip(
						selected = minRating == 25,
						onClick = { minRating = 25 }
					) {
						Text(text = "≥25")
					}
					HubsFilterChip(
						selected = minRating == 50,
						onClick = { minRating = 50 }
					) {
						Text(text = "≥50")
					}
					HubsFilterChip(
						selected = minRating == 100,
						onClick = { minRating = 100 }
					) {
						Text(text = "≥100")
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
						onClick = { complexity = PublicationComplexity.None }
					) {
						Text(text = "Все")
					}
					
					HubsFilterChip(
						selected = complexity == PublicationComplexity.Low,
						onClick = { complexity = PublicationComplexity.Low }
					) {
						Text(text = "Простой")
					}
					
					HubsFilterChip(
						selected = complexity == PublicationComplexity.Medium,
						onClick = { complexity = PublicationComplexity.Medium }
					) {
						Text(text = "Средний")
					}
					
					HubsFilterChip(
						selected = complexity == PublicationComplexity.High,
						onClick = { complexity = PublicationComplexity.High }
					) {
						Text(text = "Сложный")
					}
					
					
				}
			}
		}
	}
	
}
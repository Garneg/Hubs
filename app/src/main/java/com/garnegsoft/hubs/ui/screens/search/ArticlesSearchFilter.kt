package com.garnegsoft.hubs.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn

class ArticlesSearchFilter(
	val order: SearchFilterOrder,
	val query: String
) : Filter {
	
	override fun toArgsMap(): Map<String, String> {
		return mapOf(
			"order" to when (order) {
				SearchFilterOrder.Relevance -> "relevance"
				SearchFilterOrder.Date -> "date"
				SearchFilterOrder.Rating -> "rating"
			},
			"query" to query
		)
	}
	
	override fun getTitle(): String {
		return when (order) {
			SearchFilterOrder.Relevance -> "По релевантности"
			SearchFilterOrder.Date -> "По времени"
			SearchFilterOrder.Rating -> "По рейтингу"
		}
	}
	
	enum class SearchFilterOrder {
		Relevance,
		Date,
		Rating
	}
}

@Composable
fun ArticlesSearchFilter(
	defaultValues: ArticlesSearchFilter,
	onDismiss: () -> Unit,
	onDone: (ArticlesSearchFilter) -> Unit
) {
	
	var order by remember {
		mutableStateOf(defaultValues.order)
	}
	
	BaseFilterDialog(
		onDismiss = onDismiss,
		onDone = { onDone(ArticlesSearchFilter(order, defaultValues.query)) }) {
		Column {
			TitledColumn(title = "Сортировать") {
				
				HubsFilterChip(
					selected = order == ArticlesSearchFilter.SearchFilterOrder.Relevance,
					onClick = { order = ArticlesSearchFilter.SearchFilterOrder.Relevance }) {
					Text(text = "По релевантности")
				}
				HubsFilterChip(
					selected = order == ArticlesSearchFilter.SearchFilterOrder.Date,
					onClick = { order = ArticlesSearchFilter.SearchFilterOrder.Date }) {
					Text(text = "По времени")
				}
				HubsFilterChip(
					selected = order == ArticlesSearchFilter.SearchFilterOrder.Rating,
					onClick = { order = ArticlesSearchFilter.SearchFilterOrder.Rating }) {
					Text(text = "По рейтингу")
				}
			}
		}
	}
}
package com.garnegsoft.hubs.ui.screens.company

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn


data class CompanyBlogArticlesFilter(
	val showNew: Boolean
) : Filter {
	
	override fun toArgsMap(): Map<String, String> {
		return if (showNew) emptyMap() else mapOf(
			"sort" to "rating"
		)
		
	}
	
	override fun getTitle(): String {
		return if (showNew) "Новые" else "Лучшие"
	}
}

@Composable
fun CompanyBlogArticlesFilter(
	defaultValues: CompanyBlogArticlesFilter,
	onDismiss: () -> Unit,
	onDone: (CompanyBlogArticlesFilter) -> Unit
) {
	var showNew by rememberSaveable {
		mutableStateOf(defaultValues.showNew)
	}
	
	BaseFilterDialog(onDismiss = onDismiss, onDone = { onDone(CompanyBlogArticlesFilter(showNew)) }) {
		TitledColumn(title = "Сначала показывать") {
			Row(
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				HubsFilterChip(selected = showNew, onClick = { showNew = true }) {
					Text("Новые")
				}
				HubsFilterChip(selected = !showNew, onClick = { showNew = false }) {
					Text("Лучшие")
				}
			}
		}
	}
}
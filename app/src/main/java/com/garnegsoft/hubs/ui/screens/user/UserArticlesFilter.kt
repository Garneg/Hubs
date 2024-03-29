package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
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


class UserArticlesFilter(
	val showNews: Boolean
) : Filter {
	override fun toArgsMap(): Map<String, String> {
		return if (showNews) mapOf("news" to "true") else emptyMap()
	}
	
	override fun getTitle(): String {
		return if (showNews) "Новости" else "Статьи"
	}
	
}

@Composable
fun UserArticlesFilter(
	defaultValues: UserArticlesFilter,
	onDismiss: () -> Unit,
	onDone: (UserArticlesFilter) -> Unit
) {
	var showNews by rememberSaveable {
		mutableStateOf(defaultValues.showNews)
	}
	
	BaseFilterDialog(onDismiss = onDismiss, onDone = { onDone(UserArticlesFilter(showNews))}) {
		TitledColumn(title = "Тип публикаций") {
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				HubsFilterChip(selected = !showNews, onClick = { showNews = false }) {
					Text(text = "Статьи")
				}
				HubsFilterChip(selected = showNews, onClick = { showNews = true }) {
					Text(text = "Новости")
				}
			}
		}
	}
	
}
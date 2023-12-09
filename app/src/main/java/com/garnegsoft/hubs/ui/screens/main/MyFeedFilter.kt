package com.garnegsoft.hubs.ui.screens.main

import android.widget.Toast
import androidx.appcompat.view.menu.ShowableListMenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn


data class MyFeedFilter(
	val showArticles: Boolean,
	val showNews: Boolean
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
		}
	}
	
	override fun getTitle(): String {
		return if (showNews && showArticles) "Статьи & Новости"
		else if (showArticles) "Статьи" else "Новости"
	}
	
}

@Composable
fun MyFeedFilter(
	defaultValues: MyFeedFilter,
	onDismiss: () -> Unit,
	onDone: (MyFeedFilter) -> Unit
) {
	val context = LocalContext.current
	var showArticles by rememberSaveable {
		mutableStateOf(defaultValues.showArticles)
	}
	var showNews by rememberSaveable {
		mutableStateOf(defaultValues.showNews)
	}
	
	BaseFilterDialog(
		onDismiss = onDismiss,
		onDone = {
			if (!showNews && !showArticles){
				Toast.makeText(context, "Выберите хотя бы 1 тип публикаций", Toast.LENGTH_SHORT).show()
			} else {
				onDone(MyFeedFilter(showNews = showNews, showArticles = showArticles))
			}
		}
	) {
		TitledColumn(title = "Тип публикации") {
			Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				HubsFilterChip(selected = showArticles, onClick = { showArticles = !showArticles }) {
					Text(text = "Статьи")
				}
				HubsFilterChip(selected = showNews, onClick = { showNews = !showNews }) {
					Text(text = "Новости")
				}
			}
		}
	}
	
}
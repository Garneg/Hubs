package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.ui.common.BaseFilterDialog
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn


data class UserBookmarksFilter(
	val bookmarks: Bookmarks
) : Filter {
	override fun toArgsMap(): Map<String, String> {
		return if (bookmarks == Bookmarks.News) mapOf("user_bookmarks_news" to "true") else
			if (bookmarks == Bookmarks.Articles) mapOf("user_bookmarks" to "true") else emptyMap()
	}
	
	override fun getTitle(): String {
		return when(bookmarks) {
			Bookmarks.Articles -> "Статьи"
			Bookmarks.News -> "Новости"
			Bookmarks.Comments -> "Комментарии"
			
		}
	}
	
	enum class Bookmarks {
		Articles,
		News,
		Comments
	}
}

@Composable
fun UserBookmarksFilter(
	defaultValues: UserBookmarksFilter,
	onDismiss: () -> Unit,
	onDone: (UserBookmarksFilter) -> Unit
) {
	var bookmarksType by remember {
		mutableStateOf(
			defaultValues.bookmarks
		)
	}
	
	BaseFilterDialog(onDismiss = onDismiss, onDone = {
		onDone(UserBookmarksFilter(bookmarksType))
	}
	) {
		TitledColumn(title = "Тип закладок") {
			HubsFilterChip(
				selected = bookmarksType == UserBookmarksFilter.Bookmarks.Articles,
				onClick = { bookmarksType = UserBookmarksFilter.Bookmarks.Articles }
			) {
				Text(text = "Статьи")
			}
			HubsFilterChip(
				selected = bookmarksType == UserBookmarksFilter.Bookmarks.News,
				onClick = { bookmarksType = UserBookmarksFilter.Bookmarks.News }
			) {
				Text(text = "Новости")
			}
			// TODO: Add comments bookmarks 
//			HubsFilterChip(
//				selected = bookmarksType == UserBookmarksFilter.Bookmarks.Comments,
//				onClick = { bookmarksType = UserBookmarksFilter.Bookmarks.Comments }
//			) {
//				Text(text = "Комментарии")
//			}
		}
	}
}
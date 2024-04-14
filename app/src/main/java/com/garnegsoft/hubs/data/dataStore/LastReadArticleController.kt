package com.garnegsoft.hubs.data.dataStore

import android.content.Context
import kotlinx.coroutines.flow.Flow

class LastReadArticleController {
	companion object {
		suspend fun clearLastArticle(context: Context) {
			HubsDataStore.LastRead.edit(
				context,
				HubsDataStore.LastRead.LastArticleRead,
				0
			)
		}
		
		suspend fun setLastArticle(context: Context, articleId: Int) {
			HubsDataStore.LastRead.edit(
				context,
				HubsDataStore.LastRead.LastArticleRead,
				articleId
			)
		}
		
		fun getLastArticleFlow(context: Context): Flow<Int> {
			return HubsDataStore.LastRead.getValueFlow(context, HubsDataStore.LastRead.LastArticleRead)
		}
		
	}
}
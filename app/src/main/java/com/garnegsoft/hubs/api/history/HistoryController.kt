package com.garnegsoft.hubs.api.history

import ArticleController
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryController {
	companion object {
		suspend fun insertArticle(articleId: Int, context: Context) {
			withContext(Dispatchers.IO) {
				val dao = HistoryDatabase.getDb(context).dao()
				dao.getEventsPaged(0, 1).firstOrNull()?.let {
					if (it.actionType == HistoryActionType.Article && it.getArticle().articleId == articleId)
						return@withContext
				}

				ArticleController.getSnippet(articleId)?.let {
					val data = HistoryArticle(
						articleId,
						it.title,
						it.author?.alias ?: "",
						it.author?.avatarUrl ?: "",
						it.imageUrl
					)
					dao.insertEvent(data.toHistoryEntity())

				}
			}
		}
	}
}
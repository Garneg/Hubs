package com.garnegsoft.hubs.api.history

import ArticleController
import android.content.Context

class HistoryController {
	companion object {
		fun insertArticle(articleId: Int, context: Context) {
			val dao = HistoryDatabase.getDb(context).dao()
			dao.getEventsPaged(0, 1).firstOrNull()?.let {
				if (it.actionType == HistoryActionType.Article && it.getArticle().articleId == articleId)
					return
			}
			
			ArticleController.getSnippet(articleId)?.let {
				val data = HistoryArticle(articleId, it.title, it.author?.alias ?: "", it.author?.avatarUrl ?: "", it.imageUrl)
				dao.insertEvent(data.toHistoryEntity())
				
			}
		}
	}
}
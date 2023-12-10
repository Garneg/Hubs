package com.garnegsoft.hubs.api.history

import ArticleController
import android.content.Context

class HistoryController {
	companion object {
		fun insertArticle(articleId: Int, context: Context) {
			ArticleController.getSnippet(articleId)?.let {
				val data = HistoryArticle(articleId, it.title, it.author?.alias ?: "", it.author?.avatarUrl ?: "", it.imageUrl)
				val dao = HistoryDatabase.getDb(context).dao()
				dao.insertEvent(data.toHistoryEntity())
			}
		}
	}
}
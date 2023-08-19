package com.garnegsoft.hubs.api.article.offline

import ArticleController
import android.content.Context


class OfflineArticlesController {
	companion object {
		
		private var _dao: OfflineArticlesDao? = null
		
		private fun getDao(context: Context): OfflineArticlesDao {
			if (_dao == null){
				synchronized(this){
					_dao = OfflineArticlesDatabase.getDb(context).articlesDao()
					return _dao!!
				}
			} else {
				return _dao!!
				
			}
			
		}
		
		suspend fun downloadArticle(articleId: Int, context: Context): Boolean {
			val dao = getDao(context)
			
			val article = ArticleController.getOfflineArticle(articleId)
			val articleSnippet = ArticleController.getOfflineArticleSnippet(articleId)
			if (article == null || articleSnippet == null) {
				return false
			}
			dao.insert(article)
			dao.insertSnippet(articleSnippet)
			return true
		}
		
		suspend fun deleteArticle(articleId: Int, context: Context): Boolean {
			val dao = getDao(context)
			if (dao.exists(articleId)){
				dao.delete(articleId)
				dao.deleteSnippet(articleId)
				return true
			}
			return false
		}
	}
}
package com.garnegsoft.hubs.api.article.offline

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.garnegsoft.hubs.api.article.offline.workers.DeleteOfflineArticleWorker
import com.garnegsoft.hubs.api.article.offline.workers.DownloadOfflineArticleWorker
import java.io.File


class OfflineArticlesController {
	companion object {
		
		private var _dao: OfflineArticlesDao? = null
		
		val Context.articles_offline_resource_dir
			get() = File(filesDir, "offline_resources")
		
		fun getDao(context: Context): OfflineArticlesDao {
			
			if (_dao == null) {
				synchronized(this) {
					_dao = OfflineArticlesDatabase.getDb(context).articlesDao()
					return _dao!!
				}
			} else {
				return _dao!!
			}
			
		}
		
		fun downloadArticle(articleId: Int, context: Context) {
			val request = OneTimeWorkRequestBuilder<DownloadOfflineArticleWorker>()
				.setConstraints(Constraints(NetworkType.CONNECTED))
				.setInputData(Data.Builder().putInt("ARTICLE_ID", articleId).build())
				.build()
			WorkManager.getInstance(context).enqueue(request)
		}
		
		fun deleteArticle(articleId: Int, context: Context): Boolean {
			val request = OneTimeWorkRequestBuilder<DeleteOfflineArticleWorker>()
				.setInputData(Data.Builder().putInt("ARTICLE_ID", articleId).build())
				.build()
			WorkManager.getInstance(context).enqueue(request)
			
			return false
		}
		
		fun deleteAllArticles(context: Context) {
			val request = OneTimeWorkRequestBuilder<DeleteOfflineArticleWorker>()
				.setConstraints(Constraints(NetworkType.NOT_REQUIRED))
				.build()
			WorkManager.getInstance(context).enqueue(request)
		}
	}
	
}
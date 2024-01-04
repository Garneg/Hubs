package com.garnegsoft.hubs.api.article.offline.workers

import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeleteOfflineArticleWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {
	override suspend fun getForegroundInfo(): ForegroundInfo {
		val notification = if (Build.VERSION.SDK_INT > 26) {
			Notification.Builder(applicationContext, "delete_article_worker_channel")
				.setSmallIcon(R.drawable.offline).build()
		} else {
			Notification()
		}
		return ForegroundInfo(0, notification)
		
	}
	override suspend fun doWork(): Result {
		setForeground(getForegroundInfo())
		val articleId = inputData.getInt("ARTICLE_ID", -1)
		if (articleId < 1) {
			Looper.prepare()
			Toast.makeText(
				applicationContext,
				"Invalid article id was passed to the worker!",
				Toast.LENGTH_SHORT
			).show()
			return Result.failure()
		}
		withContext(Dispatchers.IO) {
			deleteArticle(articleId, applicationContext)
			Looper.prepare()
			Toast.makeText(
				applicationContext,
				"Статья удалена из сохраненных!",
				Toast.LENGTH_SHORT
			).show()
		}
		return Result.success()
	}
	
	private fun deleteArticle(articleId: Int, context: Context): Boolean {
		
		val dir = File(context.filesDir, "offline_resources")
		if (!dir.exists())
			return false
		
		val thisArticleDir = File(dir, articleId.toString())
		
		if (!thisArticleDir.exists() || !thisArticleDir.deleteRecursively())
			return false
		
		val dao = OfflineArticlesController.getDao(context)
		if (dao.exists(articleId)) {
			dao.delete(articleId)
			dao.deleteSnippet(articleId)
			return true
		}
		return false
	}
	
}
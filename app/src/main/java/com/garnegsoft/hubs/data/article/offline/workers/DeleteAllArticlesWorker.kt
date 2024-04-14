package com.garnegsoft.hubs.data.article.offline.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.data.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.data.article.offline.OfflineArticlesController.Companion.articles_offline_resource_dir


class DeleteAllArticlesWorker(
	context: Context,
	workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
	override suspend fun doWork(): Result {
		val dao = OfflineArticlesController.getDao(applicationContext)
		dao.clearArticlesTable()
		dao.clearSnippetsTable()
		if (applicationContext.articles_offline_resource_dir.exists()){
			applicationContext.articles_offline_resource_dir.deleteRecursively()
		}
		return Result.success()
	}
}
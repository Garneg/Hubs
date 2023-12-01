package com.garnegsoft.hubs.api.article.offline

import ArticleController
import android.app.Notification
import android.content.Context
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File


class OfflineArticlesController {
	companion object {
		
		private var _dao: OfflineArticlesDao? = null
		
		private fun getDao(context: Context): OfflineArticlesDao {
			
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
			val request = OneTimeWorkRequestBuilder<DownloadOfflineArticleResourcesWorker>()
				.setInputData(Data.Builder().putInt("ARTICLE_ID", articleId).build())
				.build()
			WorkManager.getInstance(context).enqueue(request)
		}
		
		private fun _downloadArticle(articleId: Int, context: Context): Boolean {
			val dao = getDao(context)
			
			val article = ArticleController.getOfflineArticle(articleId)
			val articleSnippet = ArticleController.getOfflineArticleSnippet(articleId)
			if (article == null || articleSnippet == null) {
				return false
			}
			
			val newArticle = Downloader.downloadArticleResources(article, context)
			val newArticleSnippet = Downloader.downloadArticleSnippetResources(articleSnippet, context)
			
			dao.insert(newArticle)
			dao.insertSnippet(newArticleSnippet)
			return true
		}
		
		fun deleteArticle(articleId: Int, context: Context): Boolean {
			val request = OneTimeWorkRequestBuilder<DeleteOfflineArticleResourcesWorker>()
				.setInputData(Data.Builder().putInt("ARTICLE_ID", articleId).build())
				.build()
			WorkManager.getInstance(context).enqueue(request)
			
			return false
		}
		
		private fun _deleteArticle(articleId: Int, context: Context): Boolean {
			
			val dir = File(context.filesDir, "offline_resources")
			if (!dir.exists())
				return false
			
			val thisArticleDir = File(dir, articleId.toString())
			
			if (!thisArticleDir.exists() || !thisArticleDir.deleteRecursively())
				return false
			
			val dao = getDao(context)
			if (dao.exists(articleId)) {
				dao.delete(articleId)
				dao.deleteSnippet(articleId)
				return true
			}
			return false
		}
		
		class DownloadOfflineArticleResourcesWorker(
			appContext: Context,
			params: WorkerParameters
		) : CoroutineWorker(appContext, params) {
			override suspend fun getForegroundInfo(): ForegroundInfo {
				val notification = if (Build.VERSION.SDK_INT > 26) {
					Notification.Builder(applicationContext, "download_article_worker_channel")
						.setSmallIcon(R.drawable.download).build()
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
					_downloadArticle(articleId, applicationContext)
					Looper.prepare()
					Toast.makeText(
						applicationContext,
						"Статья скачана!",
						Toast.LENGTH_SHORT
					).show()
				}
				return Result.success()
			}
			
		}
		
		
		class DeleteOfflineArticleResourcesWorker(
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
					_deleteArticle(articleId, applicationContext)
					Looper.prepare()
					Toast.makeText(
						applicationContext,
						"Статья удалена из сохраненных!",
						Toast.LENGTH_SHORT
					).show()
				}
				return Result.success()
			}
			
		}
		
		object Downloader {
			val client: OkHttpClient = OkHttpClient()
			
			fun downloadArticleResources(article: OfflineArticle, context: Context): OfflineArticle {
				var articleResult = article
				var urls = mutableListOf<String>()
				
				var imageCounter = 0
				val result = Jsoup.parse(article.contentHtml).forEachNode {
					if(it is Element && it.tagName() == "img"){
						var url = if (it.hasAttr("data-src")){
							it.attr("data-src")
						} else {
							it.attr("src")
						}
						
						urls.add(url)
						it.attr("data-src", "offline-article:${article.articleId}/img$imageCounter.${url.split(".").last()}")
						
						imageCounter++
					}
					
				} as Document
				
				downloadImages(urls, article.articleId, context)
				
				article.authorAvatarUrl?.let { url ->
					val request = Request.Builder().get().url(url).build()
					client.newCall(request).execute().body?.let {
						val dir = File(context.filesDir, "offline_resources")
						if (!dir.exists())
							dir.mkdir()
						val thisArticleDir = File(dir, article.articleId.toString())
						val filename = "authorAvatar.png"
						val avatarImageFile = File(thisArticleDir, filename)
						avatarImageFile.writeBytes(it.bytes())
					}
					articleResult = articleResult.copy(authorAvatarUrl = "offline-article:${article.articleId}/authorAvatar.png")
				}
				
				return articleResult.copy(contentHtml = result.body().html())
			}
			
			private fun downloadImages(urls: Collection<String>, articleId: Int, context: Context) {
				val dir = File(context.filesDir, "offline_resources")
				if (!dir.exists())
					dir.mkdir()
				val thisArticleDir = File(dir, articleId.toString())
				thisArticleDir.mkdir()
				runBlocking(Dispatchers.IO) {
					urls.forEachIndexed { index, url ->
						launch {
							val request = Request.Builder().get().url(url).build()
							val bytes = client.newCall(request).execute().body?.bytes()
							bytes?.let {
								val resourceFile =
									File(thisArticleDir, "img$index.${url.split(".").last()}")
								resourceFile.createNewFile()
								resourceFile.writeBytes(it)
							}
						}
					}
				}
			}
			
			// Downloads only thumbnail image because avatar of user should be downloaded by downloadArticleResources method
			fun downloadArticleSnippetResources(snippet: OfflineArticleSnippet, context: Context): OfflineArticleSnippet {
				var articleSnippet = snippet
				snippet.thumbnailUrl?.let { url ->
					val request = Request.Builder().get().url(url).build()
					client.newCall(request).execute().body?.let {
						val dir = File(context.filesDir, "offline_resources")
						if (!dir.exists())
							dir.mkdir()
						val thisArticleDir = File(dir, snippet.articleId.toString())
						if (!thisArticleDir.exists())
							thisArticleDir.mkdir()
						val filename = "thumbnail.${url.split(".").last()}"
						val thumbnailImageFile = File(thisArticleDir, filename)
						thumbnailImageFile.createNewFile()
						thumbnailImageFile.writeBytes(it.bytes())
						articleSnippet = articleSnippet.copy(thumbnailUrl = "offline-article:${snippet.articleId}/$filename")
					}
					
				}
				articleSnippet = articleSnippet.copy(authorAvatarUrl = "offline-article:${snippet.articleId}/authorAvatar.png")
				return articleSnippet
			}
		}
	}
	
	
}
package com.garnegsoft.hubs.api.article.offline.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.drawToBitmap
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController.Companion.articles_offline_resource_dir
import com.garnegsoft.hubs.api.offlineResourcesDir
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

val downloadArticleNotificationChannelId = "download_article_worker_channel"

class DownloadOfflineArticleWorker(
	appContext: Context,
	params: WorkerParameters
) : CoroutineWorker(appContext, params) {
	
	override suspend fun getForegroundInfo(): ForegroundInfo {
		
		if (Build.VERSION.SDK_INT >= 26){
			val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			
			if (notificationManager.notificationChannels.find { it.id == downloadArticleNotificationChannelId } == null) {
				val channel = NotificationChannel(downloadArticleNotificationChannelId, "Article Download", NotificationManager.IMPORTANCE_LOW)
				notificationManager.createNotificationChannel(channel)
			}
		}
		
		val notification = if (Build.VERSION.SDK_INT >= 26) {
			Notification.Builder(applicationContext, "download_article_worker_channel")
				.setSmallIcon(R.drawable.article_download)
				.setContentTitle("Статья загружается...")
				.setProgress(0, 0, true)
				.build()
		} else {
			Notification()
		}
		
		return ForegroundInfo(inputData.getInt("ARTICLE_ID", 0), notification)
		
	}
	override suspend fun doWork(): Result {
		setForeground(getForegroundInfo())
		
		val articleId = inputData.getInt("ARTICLE_ID", -1)
		if (articleId < 1) {
			withContext(Dispatchers.Main) {
				Toast.makeText(
					applicationContext,
					"Не удалось скачать статью. (невозможный id)",
					Toast.LENGTH_SHORT
				).show()
			}
			return Result.failure()
		}
		if (checkArticleLoaded()) {
			withContext(Dispatchers.Main) {
				Toast.makeText(
					applicationContext,
					"Статья уже загружается или загружена",
					Toast.LENGTH_SHORT
				).show()
			}
			return Result.failure()
		}
		withContext(Dispatchers.Main) {
			Toast.makeText(applicationContext, "Статья скачивается.", Toast.LENGTH_SHORT).show()
		}
		withContext(Dispatchers.IO) {
			downloadArticle(articleId, applicationContext)
			withContext(Dispatchers.Main) {
				Toast.makeText(
					applicationContext,
					"Статья скачана!",
					Toast.LENGTH_SHORT
				).show()
			}
		}
		return Result.success()
	}
	
	private fun checkArticleLoaded(): Boolean {
		return File(applicationContext.articles_offline_resource_dir, inputData.getInt("ARTICLE_ID", 0).toString()).exists()
	}
	
	private fun downloadArticle(articleId: Int, context: Context): Boolean {
		val dao = OfflineArticlesController.getDao(context)
		
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
	
	private object Downloader {
		val client: OkHttpClient = OkHttpClient()
		
		fun downloadArticleResources(article: OfflineArticle, context: Context): OfflineArticle {
			var articleResult = article
			var urls = mutableListOf<String>()
			
			var imageCounter = 0
			val result = Jsoup.parse(article.contentHtml).forEachNode {
				if(it is Element && it.tagName() == "img"){
					var url = if (it.attr("data-blurred") == "true"){
						it.attr("data-src")
					} else {
						it.attr("src")
					}
					
					urls.add(url)
					it.attr("src", "offline-article:${article.articleId}/img$imageCounter.${url.split(".").last()}")
					it.attr("data-src", "offline-article:${article.articleId}/img$imageCounter.${url.split(".").last()}")
					
					imageCounter++
				}
				
			} as Document
			
			downloadImages(urls, article.articleId, context)
			
			article.authorAvatarUrl?.let { url ->
				val request = Request.Builder().get().url(url).build()
				client.newCall(request).execute().body?.let {
					val dir = context.articles_offline_resource_dir
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
			val dir = context.articles_offline_resource_dir
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
					val dir = context.articles_offline_resource_dir
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
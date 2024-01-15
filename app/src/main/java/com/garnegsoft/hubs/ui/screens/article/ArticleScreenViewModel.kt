package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import ArticlesListController
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.article.offline.offlineArticlesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ArticleScreenViewModel : ViewModel() {
	private var _article = MutableLiveData<Article?>()
	val article: LiveData<Article?> get() = _article
	
	
	
	fun loadArticle(id: Int) {
		viewModelScope.launch(Dispatchers.IO) {
			ArticleController.get(id)?.let {
				_article.postValue(it)
				
			}
		}
	}
	
	fun saveArticle(id: Int, context: Context) {
		viewModelScope.launch(Dispatchers.IO) {
			OfflineArticlesController.downloadArticle(id, context)
			Log.e("offlineArticle", "loading done")
		}
	}
	
	fun deleteSavedArticle(id: Int, context: Context) {
		OfflineArticlesController.deleteArticle(id, context)
	}
	
	fun articleExists(context: Context, articleId: Int): Flow<Boolean> {
		return context.offlineArticlesDatabase.articlesDao().existsFlow(articleId)
	}
	
	private var _mostReadingArticles = MutableLiveData<List<ArticleSnippet>>()
	val mostReadingArticles: LiveData<List<ArticleSnippet>> get() = _mostReadingArticles
	
	fun loadMostReading() {
		viewModelScope.launch(Dispatchers.IO) {
			ArticlesListController.getArticlesSnippets("articles/most-reading")?.let {
				_mostReadingArticles.postValue(it.list.take(5))
			}
		}
	}
	
	private val _updatedPolls = MutableLiveData<List<Article.Poll>>()
	val updatedPolls: LiveData<List<Article.Poll>> get() = _updatedPolls
	
	fun vote(pollId: Int, variantsIds: List<Int>) {
		viewModelScope.launch(Dispatchers.IO) {
			ArticleController.vote(pollId = pollId, variantsIds = variantsIds)?.let { poll ->
				_updatedPolls.value?.let {
					_updatedPolls.postValue(it + poll)
				}
			}
		}
	}
	
	val parsedArticleContent =
		MutableLiveData<List<(@Composable (SpanStyle, ElementSettings) -> Unit)?>>()
	
}
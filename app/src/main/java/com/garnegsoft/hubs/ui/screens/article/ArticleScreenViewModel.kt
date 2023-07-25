package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import ArticlesListController
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.offline.HubsList
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.article.offline.offlineArticlesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ArticleScreenViewModel : ViewModel() {
    private var _article = MutableLiveData<Article?>()
    val article: LiveData<Article?> get() = _article

    private var _offlineArticle = MutableLiveData<OfflineArticle?>()
    val offlineArticle: LiveData<OfflineArticle?> get() = _offlineArticle

    fun loadArticle(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ArticleController.get(id)?.let {
                _article.postValue(it)

            }
        }
    }

    fun loadArticleFromLocalDatabase(id: Int, context: Context) {
        viewModelScope.launch(Dispatchers.IO){
            val dao = context.offlineArticlesDatabase.articlesDao()
            if (dao.exists(id)) {
                _offlineArticle.postValue(dao._getArticleById(id))

            } else {
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Статья не найдена в скачанных\nПопробуйте скачать ее заново", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveArticle(id: Int, context: Context){
        viewModelScope.launch(Dispatchers.IO){
            val dao = context.offlineArticlesDatabase.articlesDao()
            ArticleController.getSnippet(id)?.let {
                dao.insertSnippet(
                    OfflineArticleSnippet(
                        articleId = it.id,
                        authorName = it.author?.alias,
                        authorAvatarUrl = it.author?.avatarUrl,
                        timePublished = "",
                        title = it.title,
                        readingTime = it.readingTime,
                        isTranslation = it.isTranslation,
                        textSnippet = it.textSnippet,
                        hubs = HubsList(it.hubs?.map { if (it.isProfiled) it.title + "*" else it.title } ?: emptyList()),
                        thumbnailUrl = it.imageUrl
                    )
                )
            }

            ArticleController.get(id)?.let {
                dao.insert(
                    OfflineArticle(
                        articleId = it.id,
                        authorName = it.author?.alias,
                        authorAvatarUrl = it.author?.avatarUrl,
                        timePublished = "",
                        title = it.title,
                        readingTime = it.readingTime,
                        isTranslation = it.translationData.isTranslation,
                        hubs = HubsList(it.hubs.map { if (it.isProfiled) it.title + "*" else it.title }),
                        contentHtml = it.contentHtml
                    )
                )
            }
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Статья скачана!", Toast.LENGTH_SHORT).show()
            }
            Log.e("offlineArticle", "loading done")
        }
    }

    fun deleteSavedArticle(id: Int, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val dao = context.offlineArticlesDatabase.articlesDao()
            dao.delete(id)
            dao.deleteSnippet(id)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Статья удалена!", Toast.LENGTH_SHORT).show()
            }
        }
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

    val parsedArticleContent = MutableLiveData<List<(@Composable (SpanStyle) -> Unit)?>>()

}
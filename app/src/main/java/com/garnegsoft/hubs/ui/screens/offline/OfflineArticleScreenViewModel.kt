package com.garnegsoft.hubs.ui.screens.offline

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.data.article.offline.OfflineArticle
import com.garnegsoft.hubs.data.article.offline.offlineArticlesDatabase
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OfflineArticleScreenViewModel : ViewModel() {
	
	private var _offlineArticle = MutableLiveData<OfflineArticle?>()
	val offlineArticle: LiveData<OfflineArticle?> get() = _offlineArticle
	
	val parsedArticleContent = MutableLiveData<List<(@Composable (SpanStyle, ElementSettings) -> Unit)?>>()
	
	fun loadArticle(id: Int, context: Context) {
		viewModelScope.launch(Dispatchers.IO) {
			val dao = context.offlineArticlesDatabase.articlesDao()
			if (dao.exists(id)) {
				val article = dao.getArticleById(id)
				_offlineArticle.postValue(article)
				
			} else {
				withContext(Dispatchers.Main) {
					Toast.makeText(
						context,
						"Статья не найдена в скачанных\nПопробуйте скачать ее заново",
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}
	}
	
}
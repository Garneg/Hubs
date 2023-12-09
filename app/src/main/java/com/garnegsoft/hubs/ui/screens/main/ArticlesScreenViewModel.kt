package com.garnegsoft.hubs.ui.screens.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.CompaniesListModel
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.HubsListModel
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.user.UsersListModel
import com.garnegsoft.hubs.api.user.list.UserSnippet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArticlesScreenViewModel : ViewModel() {
	
	val myFeedArticlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("myFeed" to "true", "complexity" to "all", "score" to "all"),
		initialFilter = MyFeedFilter(showNews = false, showArticles = true)
	)
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = ArticlesFilterState(showLast = true, complexity = PostComplexity.None)
	)
	
	val newsListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = NewsFilter(showLast = true, period = FilterPeriod.Day),
		baseArgs = arrayOf("news" to "true")
	)
	
	val hubsListModel = HubsListModel(
		path = "hubs",
		coroutineScope = viewModelScope,
	)
	
	
	val authorsListModel = UsersListModel(
		path = "users",
		coroutineScope = viewModelScope
	)
	
	val companiesListModel = CompaniesListModel(
		path = "companies",
		coroutineScope = viewModelScope,
		"order" to "rating"
	)
	
}
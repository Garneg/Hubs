package com.garnegsoft.hubs.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.PostsListModel
import com.garnegsoft.hubs.api.company.CompaniesListModel
import com.garnegsoft.hubs.api.hub.HubsListModel
import com.garnegsoft.hubs.api.user.UsersListModel

class MainScreenViewModel : ViewModel() {
	
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
	
	val postsListModel = PostsListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		"posts" to "true", "sort" to "all"
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
package com.garnegsoft.hubs.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.data.FilterPeriod
import com.garnegsoft.hubs.data.PublicationComplexity
import com.garnegsoft.hubs.data.article.ArticlesListModel
import com.garnegsoft.hubs.data.company.CompaniesListModel
import com.garnegsoft.hubs.data.hub.HubsListModel
import com.garnegsoft.hubs.data.user.UsersListModel

class MainScreenViewModel : ViewModel() {
	
	val myFeedArticlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("myFeed" to "true"),
		initialFilter = MyFeedFilter(showNews = false, showArticles = true, minRating = -1, complexity = PublicationComplexity.None)
	)
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = ArticlesFilterState(showLast = true, complexity = PublicationComplexity.None)
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
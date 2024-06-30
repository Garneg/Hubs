package com.garnegsoft.hubs.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.data.FilterPeriod
import com.garnegsoft.hubs.data.PublicationComplexity
import com.garnegsoft.hubs.data.article.ArticlesListModel
import com.garnegsoft.hubs.data.company.CompaniesListModel
import com.garnegsoft.hubs.data.hub.HubsListModel
import com.garnegsoft.hubs.data.user.UsersListModel

class MainScreenViewModel(
	myFeedFilterInitialValue: MyFeedFilter = MyFeedFilter.defaultValues,
	articlesFilterInitialValue: ArticlesFilter = ArticlesFilter.defaultValues,
	newsFilterInitialValue: NewsFilter = NewsFilter.defaultValues
) : ViewModel() {
	
	val myFeedArticlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("myFeed" to "true"),
		initialFilter = myFeedFilterInitialValue
	)
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = articlesFilterInitialValue
	)
	
	val newsListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = newsFilterInitialValue,
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
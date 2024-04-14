package com.garnegsoft.hubs.ui.screens.hub

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.data.FilterPeriod
import com.garnegsoft.hubs.data.PublicationComplexity
import com.garnegsoft.hubs.data.article.ArticlesListModel
import com.garnegsoft.hubs.data.company.CompaniesListModel
import com.garnegsoft.hubs.data.hub.Hub
import com.garnegsoft.hubs.data.hub.HubController
import com.garnegsoft.hubs.data.user.UsersListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HubScreenViewModel(val alias: String) : ViewModel() {
	
	var hub = MutableLiveData<Hub>()
	
	val isRefreshing = MutableLiveData(false)
	
	fun refresh() {
		isRefreshing.value = true
		viewModelScope.launch(Dispatchers.IO) {
			HubController.get(alias)?.let {
				hub.postValue(it)
			}
			
			isRefreshing.postValue(false)
		}
	}
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("hub" to alias, "sort" to "all"),
		initialFilter = HubArticlesFilter(
			showLast = true,
			minRating = -1,
			period = FilterPeriod.Day,
			complexity = PublicationComplexity.None
		)
	)
	
	val newsListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("hub" to alias, "news" to "true"),
		initialFilter = HubsNewsFilter(
			showLast = true,
			minRating = -1,
			period = FilterPeriod.Day
		)
	)
	
	val authorsListModel = UsersListModel(
		path = "hubs/$alias/authors",
		coroutineScope = viewModelScope
	)
	
	var companiesListModel = CompaniesListModel(
		path = "hubs/$alias/companies",
		coroutineScope = viewModelScope
	)
}
package com.garnegsoft.hubs.ui.screens.hub

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.FilterPeriod
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.Hub
import com.garnegsoft.hubs.api.user.list.UserSnippet

class HubScreenViewModel(alias: String) : ViewModel() {
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("hub" to alias, "sort" to "all"),
		initialFilter = HubArticlesFilter(
			showLast = true,
			minRating = -1,
			period = FilterPeriod.Day,
			complexity = PostComplexity.None
		)
	)
	
	var hub = MutableLiveData<Hub>()
	var authors = MutableLiveData<HabrList<UserSnippet>>()
	var companies = MutableLiveData<HabrList<CompanySnippet>>()
}
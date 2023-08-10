package com.garnegsoft.hubs.ui.screens.company

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.user.list.UserSnippet

class CompanyScreenViewModel(alias: String) : ViewModel() {
	var companyProfile = MutableLiveData<Company>()
	var companyWhoIs = MutableLiveData<Company.WhoIs>()
	
	val blogArticlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("company" to alias)
	)
	
	var blogArticles = MutableLiveData<HabrList<ArticleSnippet>>()
	var blogNews = MutableLiveData<HabrList<ArticleSnippet>>()
	var followers = MutableLiveData<HabrList<UserSnippet>>()
	var employees = MutableLiveData<HabrList<UserSnippet>>()
}
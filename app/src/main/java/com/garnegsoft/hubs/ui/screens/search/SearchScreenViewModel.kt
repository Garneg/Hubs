package com.garnegsoft.hubs.ui.screens.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchScreenViewModel : ViewModel() {
	
	val articlesListModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		initialFilter = ArticlesSearchFilter(
			order = ArticlesSearchFilter.SearchFilterOrder.Relevance,
			query = ""
		)
	)
	
	var hubs = MutableLiveData<HabrList<HubSnippet>>()
	var companies = MutableLiveData<HabrList<CompanySnippet>>()
	var users = MutableLiveData<HabrList<UserSnippet>>()
	var comments = MutableLiveData<HabrList<CommentSnippet>>()
	
	fun loadHubs(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
		viewModelScope.launch(Dispatchers.IO) {
			HubsListController.get(
				"hubs/search",
				mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
			)?.let {
				if (hubs.value != null && page > 1) {
					hubs.postValue(hubs.value!! + it)
				} else {
					hubs.postValue(it)
				}
			}
		}
	}
	
	fun loadCompanies(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
		viewModelScope.launch(Dispatchers.IO) {
			CompaniesListController.get(
				"companies/search",
				mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
			)?.let {
				if (companies.value != null && page > 1)
					companies.postValue(companies.value!! + it)
				else
					companies.postValue(it)
			}
		}
	}
	
	fun loadUsers(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
		viewModelScope.launch(Dispatchers.IO) {
			UsersListController.get(
				"users/search",
				mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
			)?.let {
				if (users.value != null && page > 1) {
					users.postValue(users.value!! + it)
				} else
					users.postValue(it)
			}
		}
	}
	
	
	private var _mostReadingArticles = MutableLiveData<List<ArticleSnippet>>()
	val mostReadingArticles: LiveData<List<ArticleSnippet>> get() = _mostReadingArticles
	
	fun loadMostReading() {
		viewModelScope.launch(Dispatchers.IO) {
			ArticlesListController.getMostReading()?.let {
				_mostReadingArticles.postValue(it.list.take(10))
			}
		}
	}
}
package com.garnegsoft.hubs.ui.screens.search

import ArticleController
import com.garnegsoft.hubs.api.article.list.ArticlesListController
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.HubController
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.SearchUrlHandler
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

	private var _clipboardLinkData = MutableLiveData<ClipboardLinkSnippetData>()
	val clipboardLinkSnippetData: LiveData<ClipboardLinkSnippetData> get() = _clipboardLinkData

	private var _urlDataType = MutableLiveData<SearchUrlHandler.UrlDataType>()
	val urlDataType: LiveData<SearchUrlHandler.UrlDataType> get() = _urlDataType

	// Represents both id of articles and alias of users/hubs/companies
	private var _urlDataIdentifier = MutableLiveData<String>()
	val urlDataIdentifier: LiveData<String> get() = _urlDataIdentifier

	// Loads data of copied link for snippet
	private fun loadUrlData(type: SearchUrlHandler.UrlDataType, identifier: String) {
		when(type) {
			SearchUrlHandler.UrlDataType.Article -> {
				viewModelScope.launch(Dispatchers.IO) {
					val article = ArticleController.getSnippet(identifier.toInt())
					article?.let {
						_clipboardLinkData.postValue(
							ClipboardLinkSnippetData(
								title = article.title,
								type = type,
								imageUrl = article.imageUrl
								)
						)
					}

				}
			}
			SearchUrlHandler.UrlDataType.User -> {
				viewModelScope.launch(Dispatchers.IO) {
					val user = UserController.get(identifier)
					user?.let { user ->
						_clipboardLinkData.postValue(
							ClipboardLinkSnippetData(
								title = user.fullname?.let { it + "\n@${user.alias}"} ?: "@${user.alias}",
								type = type,
								imageUrl = user.avatarUrl
							)
						)
					}
				}
			}
			SearchUrlHandler.UrlDataType.Hub -> {
				viewModelScope.launch(Dispatchers.IO) {
					val hub = HubController.get(identifier)
					hub?.let {
						_clipboardLinkData.postValue(
							ClipboardLinkSnippetData(
								title = it.title,
								type = type,
								imageUrl = it.avatarUrl
							)
						)
					}
				}
			}
			SearchUrlHandler.UrlDataType.Company -> {
				viewModelScope.launch(Dispatchers.IO) {
					val company = CompanyController.get(identifier)
					company?.let {
						_clipboardLinkData.postValue(
							ClipboardLinkSnippetData(
								title = it.title,
								type = type,
								imageUrl = it.avatarUrl
							)
						)
					}

				}
			}
			else -> {}
		}
	}

	fun loadCopiedLinkSnippetData(url: String) {
		SearchUrlHandler.recongnizeUrlDataTypeAndIdentifier(url).let { result ->
			if (result.first != SearchUrlHandler.UrlDataType.Unknown) {
				_urlDataType.postValue(result.first)
				_urlDataIdentifier.postValue(result.second)
				loadUrlData(result.first, result.second!!)
			}
		}
	}

	/**
	 * Recognizes which Url data type is and returns type and identifier:
	 * for article - id,
	 * for hub/user/company - alias
	 */
	fun parseUrl(url: String): Pair<SearchUrlHandler.UrlDataType, String?> =
		SearchUrlHandler.recongnizeUrlDataTypeAndIdentifier(url)

	
	private var _mostReadingArticles = MutableLiveData<List<ArticleSnippet>>()
	val mostReadingArticles: LiveData<List<ArticleSnippet>> get() = _mostReadingArticles
	
	fun loadMostReading() {
		viewModelScope.launch(Dispatchers.IO) {
			ArticlesListController.getMostReading()?.let {
				_mostReadingArticles.postValue(it.list.take(5))
			}
		}
	}
}
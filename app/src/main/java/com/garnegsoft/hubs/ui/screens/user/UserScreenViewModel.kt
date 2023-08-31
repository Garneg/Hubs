package com.garnegsoft.hubs.ui.screens.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.ArticlesListModel
import com.garnegsoft.hubs.api.comment.CommentsListModel
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.user.UsersListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserScreenViewModel(val userAlias: String) : ViewModel() {
	
	val user = MutableLiveData<User>()
	
	val isRefreshingUser = MutableLiveData(false)
	
	fun refreshUser() {
		isRefreshingUser.postValue(true)
		viewModelScope.launch(Dispatchers.IO) {
			loadUser()
			loadNote()
			loadWhoIs()
			subscribedHubsPage = 1
			loadSubscribedHubs()
			isRefreshingUser.postValue(false)
		}
	}
	
	private fun loadUser() {
		UserController.get(userAlias)?.let {
			user.postValue(it)
		}
	}
	
	val articlesModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("user" to userAlias),
		initialFilter = UserArticlesFilter(false),
		
		)
	
	val commentsModel = CommentsListModel(
		path = "users/$userAlias/comments",
		coroutineScope = viewModelScope,
		
		)
	
	val bookmarksFilter =
		MutableLiveData(UserBookmarksFilter(UserBookmarksFilter.Bookmarks.Articles))
	val bookmarksModel = ArticlesListModel(
		path = "articles",
		coroutineScope = viewModelScope,
		baseArgs = arrayOf("user" to userAlias),
		initialFilter = bookmarksFilter.value
	)
	val commentsBookmarksModel = CommentsListModel(
		path = "users/$userAlias/bookmarks/comments",
		coroutineScope = viewModelScope,
		initialFilter = UserBookmarksFilter(UserBookmarksFilter.Bookmarks.Comments)
	)
	
	
	val followersModel = UsersListModel(
		path = "users/${userAlias}/followers",
		coroutineScope = viewModelScope
	)
	
	val subscriptionsModel = UsersListModel(
		path = "users/$userAlias/followed",
		coroutineScope = viewModelScope
	)
	
	private val _note = MutableLiveData<User.Note>()
	val note: LiveData<User.Note> get() = _note
	fun loadNote() {
		viewModelScope.launch(Dispatchers.IO) {
			user.value?.let {
				UserController.note(it.alias)?.let {
					_note.postValue(it)
				}
			}
		}
	}
	
	private val _subscribedHubs = MutableLiveData<HabrList<HubSnippet>>()
	val subscribedHubs: LiveData<HabrList<HubSnippet>> get() = _subscribedHubs
	
	private var subscribedHubsPage = 1
	val moreHubsAvailable: Boolean
		get() {
			if (_subscribedHubs.isInitialized)
				return (subscribedHubs.value?.pagesCount ?: 1) >= subscribedHubsPage
			
			return true
		}
	
	fun loadSubscribedHubs() {
		viewModelScope.launch(Dispatchers.IO) {
			if (subscribedHubsPage == 1) {
				HubsListController.get(
					"users/${userAlias}/subscriptions/hubs"
				)?.let {
					_subscribedHubs.postValue(it)
					subscribedHubsPage++
				}
			} else {
				if (subscribedHubs.isInitialized && moreHubsAvailable)
					HubsListController.get(
						"users/${userAlias}/subscriptions/hubs",
						mapOf("page" to subscribedHubsPage.toString())
					)?.let {
						_subscribedHubs.postValue(subscribedHubs.value!! + it)
						subscribedHubsPage++
					}
			}
		}
	}
	
	private var _whoIs = MutableLiveData<User.WhoIs>()
	val whoIs: LiveData<User.WhoIs> get() = _whoIs
	
	fun loadWhoIs() {
		viewModelScope.launch(Dispatchers.IO) {
			UserController.whoIs(userAlias)?.let {
				_whoIs.postValue(it)
			}
		}
	}
	
	fun loadUserProfile() {
		viewModelScope.launch(Dispatchers.IO) {
			loadUser()
		}
	}
	
	
}
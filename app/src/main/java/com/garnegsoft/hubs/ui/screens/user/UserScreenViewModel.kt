package com.garnegsoft.hubs.ui.screens.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserScreenViewModel : ViewModel() {
    val user = MutableLiveData<User>()
    val articles = MutableLiveData<HabrList<ArticleSnippet>>()
    val comments = MutableLiveData<HabrList<CommentSnippet>>()
    val bookmarks = MutableLiveData<HabrList<ArticleSnippet>>()
    val followers = MutableLiveData<HabrList<UserSnippet>>()
    val follow = MutableLiveData<HabrList<UserSnippet>>()

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
                    "users/${user.value?.alias}/subscriptions/hubs"
                )?.let {
                    _subscribedHubs.postValue(it)
                    subscribedHubsPage++
                }
            } else {
                if (subscribedHubs.isInitialized && moreHubsAvailable)
                    HubsListController.get(
                        "users/${user.value?.alias}/subscriptions/hubs",
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
            if (user.value?.relatedData != null) {
                UserController.whoIs(user.value!!.alias)?.let {
                    _whoIs.postValue(it)
                }
            }
        }
    }

    fun loadUserProfile(alias: String) {
        viewModelScope.launch(Dispatchers.IO) {
            UserController.get(alias)?.let {
                user.postValue(it)
            }
        }
    }


}
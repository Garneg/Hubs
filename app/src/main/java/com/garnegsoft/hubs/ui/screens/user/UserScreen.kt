package com.garnegsoft.hubs.ui.screens.user


import com.garnegsoft.hubs.api.hub.list.HubsListController
import ArticlesListController
import android.content.Intent
import android.graphics.Paint.Align
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.*
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

enum class UserScreenPages {
    Profile,
    Articles,
    Comments,
    Bookmarks,
    Subscribers,
    Subscriptions
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserScreen(
    alias: String,
    onBack: () -> Unit,
    onArticleClicked: (articleId: Int) -> Unit,
    onCommentClicked: (parentArticleId: Int, commentId: Int) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCommentsClicked: (postId: Int) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    initialPage: UserScreenPages = UserScreenPages.Profile,
    isAppUser: Boolean = false,
    onLogout: (() -> Unit)? = null,
    viewModelStoreOwner: ViewModelStoreOwner,
) {
    val viewModel = viewModel<UserScreenViewModel>(viewModelStoreOwner)
    val pagerState = rememberPagerState(initialPage = initialPage.ordinal)

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                title = { Text("Пользователь") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, "")
                    }
                },
                actions = {
                    val context = LocalContext.current
                    val enabled = remember(viewModel.user.isInitialized) { viewModel.user.isInitialized }
                    IconButton(
                        enabled = enabled,
                        onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            if (viewModel.user.value?.fullname != null)
                                "${viewModel.user.value!!.fullname} — https://habr.com/ru/users/${viewModel.user.value!!.alias}/"
                            else
                                "${viewModel.user.value!!.alias} — https://habr.com/ru/users/${viewModel.user.value!!.alias}/"
                        )
                        sendIntent.setType("text/plain")
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "")
                    }
                }
            )
        }
    ) {
        LaunchedEffect(key1 = Unit) {
            viewModel.loadUserProfile(alias)
        }
        viewModel.user.observeAsState().value?.let { usr ->
            val tabs = listOf<String>(
                "Профиль",
                "Публикации${
                    if (viewModel.user.value!!.articlesCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.articlesCount
                    ) else ""
                }",
                "Комментарии${
                    if (viewModel.user.value!!.commentsCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.commentsCount
                    ) else ""
                }",
                "Закладки${
                    if (viewModel.user.value!!.favoritesCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.favoritesCount
                    ) else ""
                }",
                "Подписчики${
                    if (viewModel.user.value!!.followersCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.followersCount
                    ) else ""
                }",
                "Подписки${
                    if (viewModel.user.value!!.followsCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.followsCount
                    ) else ""
                }"
            )
            Column(modifier = Modifier.padding(it)) {
                HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
                HorizontalPager(
                    state = pagerState,
                    pageCount = 6
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> {
                            val user by viewModel.user.observeAsState()
                            if (user != null) {

                                val note by viewModel.note.observeAsState()
                                val hubs by viewModel.subscribedHubs.observeAsState()
                                val whoIs by viewModel.whoIs.observeAsState()

                                if (hubs != null)
                                    UserProfile(
                                        user!!,
                                        isAppUser,
                                        onLogout,
                                        onHubClicked,
                                        viewModel
                                    )
                                else
                                    LaunchedEffect(key1 = Unit, block = {
                                        viewModel.loadNote()
                                        viewModel.loadSubscribedHubs()
                                        viewModel.loadWhoIs()
                                    })
                            }
                        }
                        1 -> {
                            val articles by viewModel.articles.observeAsState()

                            if (articles != null) {
                                PagedHabrSnippetsColumn(
                                    data = articles!!,
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            ArticlesListController.getArticlesSnippets(
                                                "articles",
                                                mapOf(
                                                    "user" to viewModel.user.value!!.alias,
                                                    "page" to it.toString()
                                                )
                                            )?.let {
                                                viewModel.articles.postValue(
                                                    articles!! + it
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onCommentsClick = { onCommentsClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) }
                                    )
                                }
                            } else {
                                if (!viewModel.articles.isInitialized){
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.Center))
                                    }
                                }
                                LaunchedEffect(key1 = Unit, block = {
                                    launch(Dispatchers.IO) {
                                        viewModel.articles.postValue(
                                            ArticlesListController.getArticlesSnippets(
                                                "articles",
                                                mapOf("user" to viewModel.user.value!!.alias)
                                            )
                                        )
                                    }
                                })
                            }
                        }
                        2 -> {
                            val userComments by viewModel.comments.observeAsState()
                            if (userComments != null) {
                                PagedHabrSnippetsColumn(
                                    data = userComments!!,
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            CommentsListController.getCommentsSnippets(
                                                "users/${alias}/comments",
                                                mapOf("page" to it.toString())
                                            )?.let {
                                                viewModel.comments.postValue(
                                                    userComments!! + it
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    CommentCard(
                                        comment = it,
                                        onCommentClick = {
                                            onCommentClicked(
                                                it.parentPost.id,
                                                it.id
                                            )
                                        },
                                        onAuthorClick = { onUserClicked(it.author.alias) },
                                        onParentPostClick = { onArticleClicked(it.parentPost.id) }
                                    )
                                }
                            } else {
                                if (!viewModel.comments.isInitialized){
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.Center))
                                    }
                                }
                                LaunchedEffect(key1 = Unit, block = {
                                    launch(Dispatchers.IO) {
                                        viewModel.comments.postValue(
                                            CommentsListController.getCommentsSnippets("users/${alias}/comments")
                                        )
                                    }
                                })
                            }
                        }
                        3 -> {
                            val bookmarks by viewModel.bookmarks.observeAsState()

                            if (bookmarks != null) {
                                PagedHabrSnippetsColumn(
                                    data = bookmarks!!,
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            ArticlesListController.getArticlesSnippets(
                                                path = "articles",
                                                args = mapOf(
                                                    "user" to alias,
                                                    "user_bookmarks" to "true",
                                                    "page" to it.toString()
                                                )
                                            )?.let {
                                                viewModel.bookmarks.postValue(bookmarks!! + it)
                                            }
                                        }
                                    }
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onCommentsClick = { onCommentsClicked(it.id) },
                                        onAuthorClick = {
                                            onUserClicked(it.author!!.alias)
                                        }
                                    )
                                }
                            } else {
                                if (!viewModel.bookmarks.isInitialized){
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.Center))
                                    }
                                }
                                LaunchedEffect(key1 = Unit, block = {
                                    launch(Dispatchers.IO) {
                                        ArticlesListController.getArticlesSnippets(
                                            path = "articles",
                                            args = mapOf(
                                                "user" to alias,
                                                "user_bookmarks" to "true"
                                            )
                                        )?.let {
                                            viewModel.bookmarks.postValue(it)
                                        }
                                    }
                                })
                            }
                        }
                        4 -> {
                            val followers by viewModel.followers.observeAsState()
                            if (followers != null) {
                                PagedHabrSnippetsColumn(
                                    data = followers!!,
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            UsersListController.get(
                                                "users/${alias}/followers",
                                                mapOf("page" to it.toString())
                                            )?.let {
                                                viewModel.followers.postValue(
                                                    followers!! + it
                                                )
                                            }

                                        }
                                    }
                                ) {
                                    UserCard(user = it, onClick = { onUserClicked(it.alias) })
                                }
                            } else {
                                if (!viewModel.followers.isInitialized){
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.Center))
                                    }
                                }
                                LaunchedEffect(key1 = Unit, block = {
                                    launch(Dispatchers.IO) {
                                        viewModel.followers.postValue(
                                            UsersListController.get("users/${alias}/followers")
                                        )
                                    }
                                })
                            }
                        }
                        5 -> {
                            val follows by viewModel.follow.observeAsState()

                            if (follows != null) {
                                PagedHabrSnippetsColumn(
                                    data = follows!!,
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            UsersListController.get(
                                                "users/${alias}/followed",
                                                mapOf("page" to it.toString())
                                            )?.let {
                                                viewModel.follow.postValue(
                                                    follows!! + it
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    UserCard(user = it, onClick = { onUserClicked(it.alias) })
                                }
                            } else {
                                if (!viewModel.follow.isInitialized){
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        CircularProgressIndicator(modifier = Modifier.align(
                                            Alignment.Center))
                                    }
                                }
                                LaunchedEffect(key1 = Unit, block = {
                                    launch(Dispatchers.IO) {
                                        viewModel.follow.postValue(
                                            UsersListController.get("users/${alias}/followed")
                                        )
                                    }
                                })
                            }

                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

}

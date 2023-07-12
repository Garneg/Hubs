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
    onCompanyClick: (alias: String) -> Unit,
    initialPage: UserScreenPages = UserScreenPages.Profile,
    isAppUser: Boolean = false,
    onLogout: (() -> Unit)? = null,
    viewModelStoreOwner: ViewModelStoreOwner,
) {
    val viewModel = viewModel<UserScreenViewModel>(viewModelStoreOwner)
    val pagerState = rememberPagerState(initialPage = initialPage.ordinal)
    val commonCoroutineScope = rememberCoroutineScope()
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
            var tabs = mapOf<String, @Composable () -> Unit>(
                "Профиль" to {
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
                                onCompanyClick,
                                viewModel,
                            )
                        else
                            LaunchedEffect(key1 = Unit, block = {
                                viewModel.loadNote()
                                viewModel.loadSubscribedHubs()
                                viewModel.loadWhoIs()
                            })
                    }
                })
            if (usr.articlesCount > 0) {
                tabs += "Публикации${
                    if (viewModel.user.value!!.articlesCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.articlesCount
                    ) else ""
                }" to {
                    val articles by viewModel.articles.observeAsState()

                    if (articles != null) {
                        PagedHabrSnippetsColumn(
                            data = articles!!,
                            onNextPageLoad = {
                                commonCoroutineScope.launch(Dispatchers.IO) {
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
            }
            if (usr.commentsCount > 0) {
                tabs += "Комментарии${
                    if (viewModel.user.value!!.commentsCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.commentsCount
                    ) else ""
                }" to {
                    val userComments by viewModel.comments.observeAsState()
                    if (userComments != null) {
                        PagedHabrSnippetsColumn(
                            data = userComments!!,
                            onNextPageLoad = {
                                commonCoroutineScope.launch(Dispatchers.IO) {
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
            }
            if (usr.favoritesCount > 0) {
                tabs += "Закладки${
                    if (viewModel.user.value!!.favoritesCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.favoritesCount
                    ) else ""
                }" to {
                    val bookmarks by viewModel.bookmarks.observeAsState()

                    if (bookmarks != null) {
                        PagedHabrSnippetsColumn(
                            data = bookmarks!!,
                            onNextPageLoad = {
                                commonCoroutineScope.launch(Dispatchers.IO) {
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
            }
            if (usr.followersCount > 0) {
                tabs += "Подписчики${
                    if (viewModel.user.value!!.followersCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.followersCount
                    ) else ""
                }" to {
                    val followers by viewModel.followers.observeAsState()
                    if (followers != null) {
                        PagedHabrSnippetsColumn(
                            data = followers!!,
                            onNextPageLoad = {
                                commonCoroutineScope.launch(Dispatchers.IO) {
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
            }
            if (usr.followsCount > 0) {
                tabs += "Подписки${
                    if (viewModel.user.value!!.followsCount > 0) " " + formatLongNumbers(
                        viewModel.user.value!!.followsCount
                    ) else ""
                }" to {
                    val follows by viewModel.follow.observeAsState()

                    if (follows != null) {
                        PagedHabrSnippetsColumn(
                            data = follows!!,
                            onNextPageLoad = {
                                commonCoroutineScope.launch(Dispatchers.IO) {
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

            Column(modifier = Modifier.padding(it)) {
                if (tabs.size > 1) {
                    HabrScrollableTabRow(pagerState = pagerState, tabs = tabs.keys.toList())
                }
                HorizontalPager(
                    state = pagerState,
                    pageCount = tabs.size
                ) { pageIndex ->
                    tabs.values.elementAt(pageIndex).invoke()
                }
            }
        } ?: Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

}

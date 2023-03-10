package com.garnegsoft.hubs.ui.screens.user

import ArticlesListController
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserScreenViewModel : ViewModel() {
    var user = MutableLiveData<User>()
    var articles = MutableLiveData<HabrList<ArticleSnippet>>()
    var comments = MutableLiveData<HabrList<CommentSnippet>>()
    var bookmarks = MutableLiveData<HabrList<ArticleSnippet>>()
    var followers = MutableLiveData<HabrList<UserSnippet>>()
    var follow = MutableLiveData<HabrList<UserSnippet>>()
}

// TODO: remove default actions for navigation events
@OptIn(ExperimentalPagerApi::class)
@Composable
fun UserScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    user: User,
    onBack: () -> Unit,
    onArticleClicked: (articleId: Int) -> Unit,
    onCommentClicked: (parentArticleId: Int, commentId: Int) -> Unit = { i, a -> },
    onUserClicked: (alias: String) -> Unit,
    onCommentsClicked: (postId: Int) -> Unit = { }
) {
    val viewModel = viewModel<UserScreenViewModel>(viewModelStoreOwner)
    val pagerState = rememberPagerState()

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
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "https://habr.com/ru/users/${user.alias}/"
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
        val tabs = listOf<String>(
            "Профиль",
            "Публикации${if (user.articlesCount > 0) " " + formatLongNumbers(user.articlesCount) else ""}",
            "Комментарии${if (user.commentsCount > 0) " " + formatLongNumbers(user.commentsCount) else ""}",
            "Закладки${if (user.favoritesCount > 0) " " + formatLongNumbers(user.favoritesCount) else ""}",
            "Подписчики${if (user.followersCount > 0) " " + formatLongNumbers(user.followersCount) else ""}",
            "Подписки${if (user.followsCount > 0) " " + formatLongNumbers(user.followsCount) else ""}"
        )
        Column(modifier = Modifier.padding(it)) {
            HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
            HorizontalPager(
                state = pagerState,
                count = 6
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        UserProfilePage(user)
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
                                                "user" to user.alias,
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
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.articles.postValue(
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("user" to user.alias)
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
                                            "users/${user.alias}/comments",
                                            mapOf("page" to it.toString())
                                        )?.let {
                                            viewModel.comments.postValue(
                                                it + userComments!!
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.comments.postValue(
                                        CommentsListController.getCommentsSnippets("users/${user.alias}/comments")
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
                                                "user" to user.alias,
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
                                    onCommentsClick = { onCommentsClicked(it.id) }
                                )
                            }
                        } else {
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    ArticlesListController.getArticlesSnippets(
                                        path = "articles",
                                        args = mapOf(
                                            "user" to user.alias,
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
                                            "users/${user.alias}/followers",
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.followers.postValue(
                                        UsersListController.get("users/${user.alias}/followers")
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
                                            "users/${user.alias}/followed",
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator()
                            }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.follow.postValue(
                                        UsersListController.get("users/${user.alias}/followed")
                                    )
                                }
                            })
                        }

                    }
                }
            }
        }
    }

}

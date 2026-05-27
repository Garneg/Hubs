package com.garnegsoft.hubs.ui.screens.user


import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.HabrScrollableTabRow
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import com.garnegsoft.hubs.ui.common.ScrollUpMethods
import com.garnegsoft.hubs.ui.common.feedCards.comment.CommentCard
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CommentsListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.CommonPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage


enum class UserScreenPages {
    Profile,
    Articles,
    Comments,
    Bookmarks,
    Followers,
    Follows
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
    val viewModel = viewModel { UserScreenViewModel(alias) }

    Scaffold(
        topBar = {
            HubsTopAppBar(
                elevation = 0.dp,
                title = { Text("Пользователь") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, "")
                    }
                },
                actions = {
                    val context = LocalContext.current

                    IconButton(
                        onClick = {
                            viewModel.user.value?.let { user ->
                                val sendIntent = Intent(Intent.ACTION_SEND)
                                sendIntent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    if (user.fullname != null)
                                        "${user.fullname} (@${user.alias}) — https://habr.com/ru/users/${user.alias}/"
                                    else
                                        "${user.alias} — https://habr.com/ru/users/${user.alias}/"
                                )
                                sendIntent.setType("text/plain")
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        }) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "")
                    }
                }
            )
        }
    ) {
        LaunchedEffect(key1 = Unit) {
            if (!viewModel.user.isInitialized) {
                viewModel.loadUserProfile()
            }
        }

        val profilePageScrollState = rememberScrollState()

        val articlesLazyListState = rememberLazyListState()
        val articlesFilterContentState = rememberCollapsingContentState()

        val commentsLazyListState = rememberLazyListState()

        val bookmarksLazyListState = rememberLazyListState()
        val bookmarksFilterContentState = rememberCollapsingContentState()

        val followersLazyListState = rememberLazyListState()

        val subscriptionsLazyListState = rememberLazyListState()
        val user by viewModel.user.observeAsState()

        val pagesMap = remember(user) {
            buildMap<UserScreenPages, @Composable () -> Unit> {
                put(UserScreenPages.Profile, {

                    UserProfile(
                        isAppUser,
                        onLogout,
                        onHubClicked,
                        onCompanyClick,
                        profilePageScrollState,
                        viewModel,
                    )
                    LaunchedEffect(key1 = Unit, block = {
                        if (!viewModel.note.isInitialized) {
                            viewModel.loadNote()
                        }
                        if (!viewModel.subscribedHubs.isInitialized) {
                            viewModel.loadSubscribedHubs()
                        }
                        if (!viewModel.whoIs.isInitialized) {
                            viewModel.loadWhoIs()
                        }
                    })


                })
                user?.let { user ->
                    if (user.articlesCount > 0 || initialPage == UserScreenPages.Articles) {
                        put(UserScreenPages.Articles, {
                            ArticlesListPageWithFilter(
                                listModel = viewModel.articlesModel,
                                lazyListState = articlesLazyListState,
                                collapsingContentState = articlesFilterContentState,
                                onArticleSnippetClick = onArticleClicked,
                                onArticleAuthorClick = onUserClicked,
                                onArticleCommentsClick = onCommentsClicked,
                                ignoreBlackList = true
                            ) { defaultValues, onDismiss, onDone ->
                                UserArticlesFilter(
                                    defaultValues = defaultValues,
                                    onDismiss = onDismiss,
                                    onDone = onDone
                                )
                            }
                        })
                    }
                    if (user.commentsCount > 0 || initialPage == UserScreenPages.Comments) {
                        put(
                            UserScreenPages.Comments,
                            {
                                CommentsListPage(
                                    listModel = viewModel.commentsModel,
                                    lazyListState = commentsLazyListState,
                                    onArticleClick = onArticleClicked,
                                    onCommentClick = onCommentClicked,
                                    onUserClick = onUserClicked
                                )
                            })
                    }
                    if (user.bookmarksCount > 0 || initialPage == UserScreenPages.Bookmarks) {
                        put(
                            UserScreenPages.Bookmarks,
                            {
                                val bookmarksFilter by viewModel.bookmarksFilter.observeAsState(initial = null)
                                bookmarksFilter?.let {
                                    if (it.bookmarks != UserBookmarksFilter.Bookmarks.Comments) {
                                        ArticlesListPageWithFilter(
                                            listModel = viewModel.bookmarksModel,
                                            lazyListState = bookmarksLazyListState,
                                            collapsingContentState = bookmarksFilterContentState,
                                            onArticleSnippetClick = onArticleClicked,
                                            onArticleAuthorClick = onUserClicked,
                                            onArticleCommentsClick = onCommentsClicked
                                        ) { defaultValues, onDismiss, onDone ->
                                            UserBookmarksFilter(
                                                defaultValues = defaultValues,
                                                onDismiss = onDismiss,
                                                onDone = {
                                                    viewModel.bookmarksFilter.value = it
                                                    if (it.bookmarks != UserBookmarksFilter.Bookmarks.Comments)
                                                        onDone(it)
                                                }
                                            )
                                        }
                                    } else {
                                        CommonPageWithFilter<CommentSnippet, UserBookmarksFilter>(
                                            listModel = viewModel.commentsBookmarksModel,
                                            filterDialog = { defaultValues, onDismiss, onDone ->
                                                UserBookmarksFilter(
                                                    defaultValues = defaultValues,
                                                    onDismiss = onDismiss,
                                                    onDone = {
                                                        viewModel.bookmarksFilter.value = it
                                                        onDone(it)
                                                    }
                                                )
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
                                                onParentPostClick = { onArticleClicked(it.parentPost.id) })
                                        }
                                    }
                                }
                            })
                    }
                    if (user.followersCount > 0 || initialPage == UserScreenPages.Followers) {
                        put(
                            UserScreenPages.Followers,
                            {
                                UsersListPage(
                                    listModel = viewModel.followersModel,
                                    lazyListState = followersLazyListState,
                                    onUserClick = onUserClicked
                                )
                            }
                        )
                    }
                    if (user.subscriptionsCount > 0 || initialPage == UserScreenPages.Follows) {
                        put(
                            UserScreenPages.Follows,
                            {
                                UsersListPage(
                                    listModel = viewModel.subscriptionsModel,
                                    lazyListState = subscriptionsLazyListState,
                                    onUserClick = onUserClicked
                                )
                            }
                        )
                    }
                }
            }
        }
        if (initialPage == UserScreenPages.Profile || (pagesMap.size > 1)) {
            val tabs = remember(pagesMap) {
                pagesMap?.keys?.map {
                    when (it) {
                        UserScreenPages.Profile -> "Профиль"
                        UserScreenPages.Articles -> "Публикации${
                            if (viewModel.user.value!!.articlesCount > 0) " " + formatLongNumbers(
                                viewModel.user.value!!.articlesCount
                            ) else ""
                        }"

                        UserScreenPages.Comments -> "Комментарии${
                            if (viewModel.user.value!!.commentsCount > 0) " " + formatLongNumbers(
                                viewModel.user.value!!.commentsCount
                            ) else ""
                        }"

                        UserScreenPages.Bookmarks -> "Закладки${
                            if (viewModel.user.value!!.bookmarksCount > 0) " " + formatLongNumbers(
                                viewModel.user.value!!.bookmarksCount
                            ) else ""
                        }"

                        UserScreenPages.Followers -> "Подписчики${
                            if (viewModel.user.value!!.followersCount > 0) " " + formatLongNumbers(
                                viewModel.user.value!!.followersCount
                            ) else ""
                        }"

                        UserScreenPages.Follows -> "Подписки${
                            if (viewModel.user.value!!.subscriptionsCount > 0) " " + formatLongNumbers(
                                viewModel.user.value!!.subscriptionsCount
                            ) else ""
                        }"
                    }
                }
            }
            val pagerState = rememberPagerState(initialPage = remember(pagesMap.size) {
                if (pagesMap.size == 1) 0 else pagesMap.keys.indexOf(initialPage)
            }) { pagesMap.size }

            Column(modifier = Modifier.padding(it)) {
                AnimatedVisibility(
                    visible = pagesMap.size > 1,
                    enter = slideInVertically { -it } + fadeIn()
                ) {
                    if (tabs != null) {
                        HabrScrollableTabRow(pagerState = pagerState, tabs = tabs) { index, title ->
                            when {
                                title.startsWith("Профиль") -> {
                                    ScrollUpMethods.scrollNormalList(profilePageScrollState)
                                }

                                title.startsWith("Публикации") -> {
                                    articlesFilterContentState.show()
                                    ScrollUpMethods.scrollLazyList(articlesLazyListState)
                                }

                                title.startsWith("Комментарии") -> {
                                    ScrollUpMethods.scrollLazyList(commentsLazyListState)
                                }

                                title.startsWith("Закладки") -> {
                                    bookmarksFilterContentState.show()
                                    ScrollUpMethods.scrollLazyList(bookmarksLazyListState)
                                }

                                title.startsWith("Подписчики") -> {
                                    ScrollUpMethods.scrollLazyList(followersLazyListState)
                                }

                                title.startsWith("Подписки") -> {
                                    ScrollUpMethods.scrollLazyList(subscriptionsLazyListState)
                                }

                            }
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState
                ) { pageIndex ->
                    pagesMap.values
                        .elementAtOrNull(pageIndex)
                        ?.invoke()
                }
            }
        }
    }


}

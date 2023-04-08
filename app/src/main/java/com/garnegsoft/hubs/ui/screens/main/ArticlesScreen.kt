package com.garnegsoft.hubs.ui.screens.main


import ArticlesListController
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.DataStoreKeys
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.authDataStore
import com.garnegsoft.hubs.authDataStoreFlow
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.screens.user.UserScreenPages
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map


class ArticlesScreenViewModel : ViewModel() {

    var myFeedArticles = MutableLiveData<HabrList<ArticleSnippet>>()
    var articles = MutableLiveData<HabrList<ArticleSnippet>>()
    var news = MutableLiveData<HabrList<ArticleSnippet>>()
    var hubs = MutableLiveData<HabrList<HubSnippet>>()
    var authors = MutableLiveData<HabrList<UserSnippet>>()
    var companies = MutableLiveData<HabrList<CompanySnippet>>()

}



@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ArticlesScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClicked: (articleId: Int) -> Unit,
    onSearchClicked: () -> Unit,
    onCommentsClicked: (articleId: Int) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    menu: @Composable () -> Unit,
) {
    val isAuthorized by LocalContext.current.authDataStoreFlow(DataStoreKeys.Auth.Authorized).collectAsState(
        initial = false
    )
    val viewModel = viewModel<ArticlesScreenViewModel>(viewModelStoreOwner = viewModelStoreOwner)

    Scaffold(
        topBar = {
            TopAppBar(
                contentColor = Color.White,
                title = {
                    Text(
                        text = "Хабы"
                    )
                },
                actions = {
                    IconButton(onClick = { onSearchClicked() }) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(20)),
                            painter = painterResource(id = R.drawable.search_icon),
                            contentDescription = "search",
                            tint = Color.White
                        )
                    }
                    menu()
                })
        }
    ) {
        Column(
            Modifier.padding(it)
        ) {

            val articlesLazyListState = rememberLazyListState()

            val newsLazyListState = rememberLazyListState()

            val pages = remember(isAuthorized) {
                var map = mapOf<String, @Composable () -> Unit>(
                    "Статьи" to {
                        val articles by viewModel.articles.observeAsState()

                        var updateFeedCoroutineScope = rememberCoroutineScope()
                        var pageNumber = rememberSaveable { mutableStateOf(1) }

                        if (articles != null) {

                            var refreshing by remember { mutableStateOf(false) }
                            val refreshingState = rememberPullRefreshState(
                                refreshing = refreshing,
                                refreshThreshold = 50.dp,
                                onRefresh = {
                                    updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                        refreshing = true
                                        pageNumber.value = 1
                                        var newArticlesList =
                                            ArticlesListController.getArticlesSnippets(
                                                "articles",
                                                mapOf("sort" to "rating")
                                            )
                                        if (newArticlesList != null) {
                                            viewModel.articles.postValue(newArticlesList)
                                        }
                                        delay(400)

                                        refreshing = false
                                    }
                                })
                            Box(
                                modifier = Modifier.pullRefresh(
                                    state = refreshingState
                                )
                            ) {
                                PagedHabrSnippetsColumn(
                                    data = articles!!,
                                    lazyListState = articlesLazyListState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(8.dp),
                                    onNextPageLoad = {
                                        launch(Dispatchers.IO) {
                                            ArticlesListController
                                                .getArticlesSnippets(
                                                    "articles",
                                                    mapOf(
                                                        "sort" to "rating",
                                                        "page" to it.toString()
                                                    )
                                                )?.let {
                                                    viewModel.articles.postValue(articles!! + it)
                                                }

                                        }

                                    },
                                    page = pageNumber
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onCommentsClick = { onCommentsClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) }
                                    )
                                }
                                PullRefreshIndicator(
                                    refreshing = refreshing,
                                    state = refreshingState,
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    contentColor = SecondaryColor
                                )
                            }
                        } else {
                            LaunchedEffect(key1 = Unit) {
                                launch(Dispatchers.IO) {
                                    ArticlesListController.getArticlesSnippets(
                                        "articles",
                                        mapOf("sort" to "rating")
                                    )?.let {
                                        viewModel.articles.postValue(it)
                                    }

                                }
                            }
                        }


                    },
                    "Новости" to {
                        val newsList by viewModel.news.observeAsState()
                        var pageNumber = rememberSaveable { mutableStateOf(1) }
                        var updateFeedCoroutineScope = rememberCoroutineScope()
                        var isRefreshing by rememberSaveable { mutableStateOf(false) }
                        var swipestate = rememberPullRefreshState(
                            refreshing = isRefreshing,
                            refreshThreshold = 50.dp,
                            onRefresh = {
                                updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                    isRefreshing = true
                                    pageNumber.value = 1
                                    var newArticlesList =
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("sort" to "rating", "news" to "true")
                                        )
                                    if (newArticlesList != null) {
                                        viewModel.news.postValue(newArticlesList)
                                    }
                                    //delay(400)
                                    //newsLazyListState.scrollToItem(0)
                                    isRefreshing = false

                                }
                            })


                        Box(
                            modifier = Modifier.pullRefresh(
                                state = swipestate
                            )
                        ) {

                            if (newsList != null) {
                                PagedHabrSnippetsColumn(
                                    data = newsList!!,
                                    page = pageNumber,
                                    lazyListState = newsLazyListState,
                                    onNextPageLoad = {
                                        updateFeedCoroutineScope.launch(Dispatchers.IO) {

                                            ArticlesListController
                                                .getArticlesSnippets(
                                                    "articles",
                                                    mapOf(
                                                        "sort" to "rating",
                                                        "news" to "true",
                                                        "page" to it.toString()
                                                    )
                                                )?.let {
                                                    viewModel.news.postValue(newsList!! + it)
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
                                PullRefreshIndicator(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    contentColor = SecondaryColor,
                                    refreshing = isRefreshing, state = swipestate
                                )
                            } else {
                                LaunchedEffect(key1 = Unit) {
                                    launch(Dispatchers.IO) {
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("news" to "true", "sort" to "rating")
                                        )?.let {
                                            viewModel.news.postValue(it)
                                        }
                                    }
                                }
                            }
                        }

                    },
                    "Хабы" to {
                        val hubs by viewModel.hubs.observeAsState()
                        if (hubs != null) {
                            PagedHabrSnippetsColumn(
                                lazyListState = rememberLazyListState(),
                                data = hubs!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        HubsListController.get(
                                            "hubs",
                                            mapOf("page" to it.toString())
                                        )?.let {
                                            viewModel.hubs.postValue(
                                                hubs!! + it
                                            )
                                        }
                                    }
                                }
                            ) {
                                HubCard(
                                    hub = it,
                                    onClick = { onHubClicked(it.alias) }
                                )
                            }
                        } else {
                            LaunchedEffect(key1 = Unit) {
                                launch(Dispatchers.IO) {
                                    viewModel.hubs.postValue(HubsListController.get("hubs"))
                                }
                            }
                        }
                    },
                    "Авторы" to {
                        val authors by viewModel.authors.observeAsState()
                        if (authors != null) {
                            PagedHabrSnippetsColumn(
                                data = authors!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        UsersListController.get(
                                            "users",
                                            mapOf("page" to it.toString())
                                        )?.let {
                                            viewModel.authors.postValue(
                                                authors!! + it
                                            )
                                        }
                                    }
                                }
                            ) {
                                UserCard(user = it, onClick = { onUserClicked(it.alias) })
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                            }
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.authors.postValue(UsersListController.get("users"))
                                }
                            })

                        }
                    },
                    "Компании" to {
                        val companies by viewModel.companies.observeAsState()
                        if (companies != null) {
                            PagedHabrSnippetsColumn(
                                data = companies!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        CompaniesListController.get(
                                            "companies",
                                            mapOf("order" to "rating", "page" to it.toString())
                                        )?.let {
                                            viewModel.companies.postValue(
                                                companies!! + it
                                            )
                                        }
                                    }
                                }
                            ) {
                                CompanyCard(
                                    company = it,
                                    onClick = { onCompanyClicked(it.alias) })
                            }
                        } else {

                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.companies.postValue(
                                        CompaniesListController.get(
                                            "companies",
                                            args = mapOf("order" to "rating")
                                        )
                                    )
                                }
                            })
                        }
                    }
                )
                if (isAuthorized == true) map = mapOf<String, @Composable () -> Unit>("Моя лента" to {
                    val articles by viewModel.myFeedArticles.observeAsState()
                    if (articles != null){
                        PagedHabrSnippetsColumn(
                            data = articles!!,
                            onNextPageLoad = {
                                launch(Dispatchers.IO) {
                                   ArticlesListController.getArticlesSnippets(
                                       "articles",
                                       mapOf("custom" to "true",
                                       "page" to it.toString())
                                   )?.let{
                                       viewModel.myFeedArticles.postValue(
                                           articles!! + it
                                       )
                                   }
                                }
                            }
                        ) {
                            ArticleCard(
                                article = it,
                                onClick = { onArticleClicked(it.id) },
                                onAuthorClick = { onUserClicked(it.author!!.alias) },
                                onCommentsClick = { onCommentsClicked(it.id) })
                        }
                    }
                    else {
                        Box(modifier = Modifier.fillMaxSize())
                        LaunchedEffect(key1 = Unit, block = {
                            launch(Dispatchers.IO) {
                                ArticlesListController.getArticlesSnippets(
                                    "articles",
                                    mapOf("custom" to "true")
                                )?.let {
                                    viewModel.myFeedArticles.postValue(
                                        it
                                    )
                                }
                            }
                        })
                    }
                }) + map
                map
            }
            val pagerState = rememberPagerState()



            HabrScrollableTabRow(pagerState = pagerState, tabs = pages.keys.toList())
            HorizontalPager(
                state = pagerState,
                pageCount = pages.size,

            ) {
                pages.values.elementAt(it)( )

            }
        }
    }
}


@Composable
fun UnauthorizedMenu(
    onLoginClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "menu")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
    ) {
        MenuItem(title = "Войти", icon = {
            Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "")
        }, onClick = onLoginClick)

        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "О приложении", icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "",
                modifier = Modifier.size(24.dp)
            )
        }, onClick = onAboutClick)
    }
}

@Composable
fun AuthorizedMenu(
    userAlias: String,
    avatarUrl: String?,
    onProfileClick: () -> Unit,
    onArticlesClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        if (avatarUrl != null){
            AsyncImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentScale = ContentScale.FillBounds,
                model = avatarUrl, contentDescription = "")
        } else {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))

                    .border(
                        width = 2.dp, color = placeholderColor(userAlias),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(1.dp)
                    .background(Color.White)
                    .padding(1.5.dp),
                painter = painterResource(id = R.drawable.user_avatar_placeholder),
                contentDescription = "",
                tint = placeholderColor(userAlias)
            )
        }

    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
    ) {
        MenuItem(title = userAlias, icon = {
            if (avatarUrl != null){
                AsyncImage(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillBounds,
                    model = avatarUrl, contentDescription = "")
            } else {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 2.dp, color = placeholderColor(userAlias),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.5.dp),
                    painter = painterResource(id = R.drawable.user_avatar_placeholder),
                    contentDescription = "",
                    tint = placeholderColor(userAlias)
                )
            }
        }, onClick = onProfileClick)
        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "Статьи", icon = { 
            Icon(painter = painterResource(id = R.drawable.article), contentDescription = "")
        }, onClick = onArticlesClick)

        MenuItem(title = "Комментарии", icon = { 
            Icon(
                painter = painterResource(id = R.drawable.comments_icon), contentDescription = "")
        }, onClick = onCommentsClick)

        MenuItem(title = "Закладки", icon = {
            Icon(painter = painterResource(id = R.drawable.bookmark), contentDescription = "")
        }, onClick = onBookmarksClick)

        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "О приложении", icon = {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "")
        }, onClick = onAboutClick)
    }
}

@Composable
fun MenuItem(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(14.dp))
        Text(title)
        Spacer(modifier = Modifier.width(14.dp))
        Spacer(modifier = Modifier.weight(1f))
    }
}




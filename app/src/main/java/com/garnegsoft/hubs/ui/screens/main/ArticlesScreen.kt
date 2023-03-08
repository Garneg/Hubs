package com.garnegsoft.hubs.ui.screens.main


import ArticlesListController
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.*

class ArticlesScreenViewModel : ViewModel() {
    var articles = MutableLiveData<HabrList<ArticleSnippet>>()
    var news = MutableLiveData<HabrList<ArticleSnippet>>()
    var hubs = MutableLiveData<HabrList<HubSnippet>>()
    var authors = MutableLiveData<HabrList<UserSnippet>>()
    var companies = MutableLiveData<HabrList<CompanySnippet>>()

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ArticlesScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClicked: (articleId: Int) -> Unit,
    onSearchClicked: () -> Unit,
    onGoToCommentsClicked: (articleId: Int) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit
) {
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
                    //MainMenuButton()

                })
        }
    ) {
        Column(
            Modifier.padding(it)
        ) {

            val tabs = remember {
                listOf(
                    "Статьи",
                    "Новости",
                    "Хабы",
                    "Авторы",
                    "Компании"
                )
            }
            var pagerState = rememberPagerState()


            var articlesLazyListState = rememberLazyListState()


            var newsLazyListState = rememberLazyListState()


            HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
            HorizontalPager(
                state = pagerState,
                count = 5
            ) {
                when (it) {
                    // all articles
                    0 -> {
                        val articles by viewModel.articles.observeAsState()

                        var updateFeedCoroutineScope = rememberCoroutineScope()
                        var isRefreshing by rememberSaveable { mutableStateOf(false) }
                        var swipestate = rememberSwipeRefreshState(isRefreshing = isRefreshing)
                        var pageNumber = rememberSaveable { mutableStateOf(1) }
                        LaunchedEffect(key1 = articles?.list?.first(), block = {
                            articlesLazyListState.scrollToItem(0)
                        })

                        SwipeRefresh(
                            state = swipestate,
                            indicator = { state, offset ->
                                SwipeRefreshIndicator(
                                    state = state,
                                    refreshTriggerDistance = 50.dp,
                                    contentColor = SecondaryColor,
                                )
                            },
                            onRefresh = {
                                updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                    swipestate.isRefreshing = true
                                    pageNumber.value = 1
                                    var newArticlesList =
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("sort" to "rating")
                                        )
                                    if (newArticlesList != null) {
                                        viewModel.articles.postValue(newArticlesList)
                                    }
                                    swipestate.isRefreshing = false
                                }
                            }) {
                            if (articles != null) {
                                PagedHabrSnippetsColumn(
                                    data = articles!!,
                                    lazyListState = articlesLazyListState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(8.dp),
                                    onNextPageLoad = {
                                        updateFeedCoroutineScope.launch(Dispatchers.IO) {
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
                                        onCommentsClick = { onGoToCommentsClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) }
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

                        }

                    }
                    // news
                    1 -> {
                        val newsList by viewModel.news.observeAsState()

                        var updateFeedCoroutineScope = rememberCoroutineScope()
                        var isRefreshing by rememberSaveable { mutableStateOf(false) }
                        var swipestate = rememberSwipeRefreshState(isRefreshing = isRefreshing)
                        var pageNumber by rememberSaveable { mutableStateOf(1) }


                        SwipeRefresh(
                            state = swipestate,
                            indicator = { state, offset ->
                                SwipeRefreshIndicator(
                                    state = state,
                                    refreshTriggerDistance = 50.dp,
                                    contentColor = SecondaryColor,
                                )
                            },
                            onRefresh = {
                                updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                    swipestate.isRefreshing = true
                                    pageNumber = 1
                                    var newArticlesList =
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("sort" to "rating", "news" to "true")
                                        )
                                    if (newArticlesList != null) {
                                        viewModel.news.postValue(newArticlesList)
                                    }
                                    swipestate.isRefreshing = false

                                }
                            }) {

                            if (newsList != null) {
                                LazyHabrSnippetsColumn(
                                    data = newsList!!,
                                    lazyListState = newsLazyListState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(8.dp),
                                    onScrollEnd = {
                                        updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                            pageNumber++

                                            ArticlesListController
                                                .getArticlesSnippets(
                                                    "articles",
                                                    mapOf(
                                                        "sort" to "rating",
                                                        "news" to "true",
                                                        "page" to pageNumber.toString()
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
                                        onCommentsClick = { onGoToCommentsClicked(it.id) }
                                    )
                                }
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

                    }
                    // hubs
                    2 -> {
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
                    }
                    // authors
                    3 -> {
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
                    }
                    // companies
                    4 -> {
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
                }
            }


        }
    }


}



@Composable
fun MainMenuButton() {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "menu")
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(onClick = {
            expanded = false
        }) {
            Text("Настройки")
        }
        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        DropdownMenuItem(onClick = {
            expanded = false
        }) {
            Text(text = "О приложении")
        }
    }
}




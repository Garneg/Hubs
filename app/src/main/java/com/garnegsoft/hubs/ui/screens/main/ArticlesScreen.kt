package com.garnegsoft.hubs.ui.screens.main


import ArticleController
import ArticlesListController
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.CollapsingContent
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.authDataStoreFlow
import com.garnegsoft.hubs.lastReadDataStore
import com.garnegsoft.hubs.lastReadDataStoreFlow
import com.garnegsoft.hubs.ui.common.*
import kotlinx.coroutines.*


class ArticlesScreenViewModel : ViewModel() {
    var myFeedArticles = MutableLiveData<HabrList<ArticleSnippet>>()

    var articles = MutableLiveData<HabrList<ArticleSnippet>?>()
    var articlesFilter = MutableLiveData<ArticlesFilterDialogResult>(
        ArticlesFilterDialogResult(
            showLast = true,
            complexity = PostComplexity.None
        )
    )

    fun updateFilter(newFilter: ArticlesFilterDialogResult) {
        if (articlesFilter.value?.hasChanged(newFilter) ?: false) {
            articles.postValue(null)
            articlesFilter.postValue(newFilter)
            viewModelScope.launch(Dispatchers.IO) {
                var argsMap: Map<String, String> =
                    if (newFilter.showLast) {
                        if (newFilter.minRating == -1) {
                            mapOf(
                                "sort" to "rating",
                            )
                        } else {
                            mapOf(
                                "sort" to "rating",
                                "score" to newFilter.minRating.toString()
                            )
                        }
                    } else {
                        mapOf(
                            "sort" to "date",
                            "period" to when (newFilter.period) {
                                FilterPeriod.Day -> "daily"
                                FilterPeriod.Week -> "weekly"
                                FilterPeriod.Month -> "monthly"
                                FilterPeriod.Year -> "yearly"
                                FilterPeriod.AllTime -> "alltime"
                            },
                        )
                    }

                if (newFilter.complexity != PostComplexity.None) {
                    argsMap += mapOf(
                        "complexity" to when (newFilter.complexity) {
                            PostComplexity.Low -> "easy"
                            PostComplexity.Medium -> "medium"
                            PostComplexity.High -> "hard"
                            else -> throw IllegalArgumentException("mapping of this complexity is not supported")
                        }
                    )
                }
                ArticlesListController.getArticlesSnippets("articles", argsMap)?.let {
                    articles.postValue(it)
                }

            }

        }
    }

    val isLoadingArticles = MutableLiveData<Boolean>(false)

    fun loadArticles(page: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            isLoadingArticles.postValue(true)
            val filter = articlesFilter.value ?: ArticlesFilterDialogResult(
                showLast = true,
                complexity = PostComplexity.None
            )
            var argsMap: Map<String, String> =
                if (filter.showLast) {
                    if (filter.minRating == -1) {
                        mapOf(
                            "sort" to "rating",
                        )
                    } else {
                        mapOf(
                            "sort" to "rating",
                            "score" to filter.minRating.toString()
                        )
                    }
                } else {
                    mapOf(
                        "sort" to "date",
                        "period" to when (filter.period) {
                            FilterPeriod.Day -> "daily"
                            FilterPeriod.Week -> "weekly"
                            FilterPeriod.Month -> "monthly"
                            FilterPeriod.Year -> "yearly"
                            FilterPeriod.AllTime -> "alltime"
                        },
                    )
                }

            if (filter.complexity != PostComplexity.None) {
                argsMap += mapOf(
                    "complexity" to when (filter.complexity) {
                        PostComplexity.Low -> "easy"
                        PostComplexity.Medium -> "medium"
                        PostComplexity.High -> "hard"
                        else -> throw IllegalArgumentException("mapping of this complexity is not supported")
                    }
                )
            }
            argsMap += mapOf("page" to page.toString())
            ArticlesListController.getArticlesSnippets("articles", argsMap)?.let {
                if (page > 1) {
                    articles.value?.let { firstArticles ->
                        articles.postValue(firstArticles + it)
                    }
                } else {
                    articles.postValue(it)
                }
            }
            isLoadingArticles.postValue(false)


        }
    }

    fun ArticlesFilterDialogResult.hasChanged(newFilter: ArticlesFilterDialogResult): Boolean {
        if (showLast != newFilter.showLast)
            return true
        if (complexity != newFilter.complexity)
            return true
        if (showLast) {
            return minRating != newFilter.minRating
        } else
            return period != newFilter.period
    }

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
    val context = LocalContext.current
    var isAuthorized by rememberSaveable() { mutableStateOf(false) }
    val authorizedState by context.authDataStoreFlow(HubsDataStore.Auth.Keys.Authorized)
        .collectAsState(initial = false)
    LaunchedEffect(key1 = authorizedState, block = {
        isAuthorized = authorizedState == true
    })

    val viewModel = viewModel<ArticlesScreenViewModel>(viewModelStoreOwner = viewModelStoreOwner)

    val scaffoldState = rememberScaffoldState()

    val lastArticleRead by context.lastReadDataStoreFlow(HubsDataStore.LastRead.Keys.LastArticleRead)
        .collectAsState(initial = -1)

    var showSnackBar by rememberSaveable {
        mutableStateOf(true)
    }

    LaunchedEffect(key1 = lastArticleRead, block = {

        if (showSnackBar && lastArticleRead != null && lastArticleRead!! > 0) {

            launch(Dispatchers.IO) {
                val snippet = ArticleController.getSnippet(lastArticleRead!!)

                snippet?.let {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = it.title,
                        actionLabel = it.imageUrl,
                        duration = SnackbarDuration.Indefinite
                    )
                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        launch(Dispatchers.Main) { onArticleClicked(it.id) }
                    } else {
                        context.lastReadDataStore.edit {
                            it[HubsDataStore.LastRead.Keys.LastArticleRead] = 0
                        }
                    }
                    showSnackBar = false

                }
            }
        } else if (lastArticleRead == 0) {
            showSnackBar = false
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                title = {
                    Text(
                        text = "Хабы"
                    )
                },
                actions = {
                    IconButton(onClick = {
                        onSearchClicked()
                    }) {
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
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) {
                ContinueReadSnackBar(data = it)
            }
        }
    ) {
        Column(
            Modifier.padding(it)
        ) {
            val commonCoroutineScope = rememberCoroutineScope()
            val myFeedLazyListState = rememberLazyListState()
            val articlesLazyListState = rememberLazyListState()
            val newsLazyListState = rememberLazyListState()
            val hubsLazyListState = rememberLazyListState()
            val authorsLazyListState = rememberLazyListState()
            val companiesLazyListState = rememberLazyListState()

            val pages = remember(key1 = isAuthorized) {
                var map = mapOf<String, @Composable () -> Unit>(
                    "Статьи" to {
                        val articles by viewModel.articles.observeAsState()

                        val updateFeedCoroutineScope = rememberCoroutineScope()
                        val pageNumber = rememberSaveable { mutableStateOf(1) }

                        val readyToScrollUp = remember { mutableStateOf(false) }

                        val isUpdatingInProgress = remember { mutableStateOf(false) }

                        val filter by viewModel.articlesFilter.observeAsState()
                        val filterTitle = remember(filter) {
                            filter?.let {
                                var result = ""
                                if (it.showLast) {
                                    if (it.minRating == -1){
                                        result = "Все подряд"
                                    } else {
                                        result = "Новые с рейтингом ≥${it.minRating}"
                                    }
                                } else {
                                    result += "Лучшие за "
                                    when(it.period){
                                        FilterPeriod.Day -> result += "сутки"
                                        FilterPeriod.Week -> result += "неделю"
                                        FilterPeriod.Month -> result += "месяц"
                                        FilterPeriod.Year -> result += "год"
                                        FilterPeriod.AllTime -> result += "все время"

                                    }
                                }
                                when(it.complexity){
                                    PostComplexity.High -> result += ", сложные"
                                    PostComplexity.Medium -> result += ", средние"
                                    PostComplexity.Low -> result += ", простые"

                                    else -> {}
                                }
                                result
                            } ?: ""
                        }

                        var showFilterDialog by remember { mutableStateOf(false) }
                        if (showFilterDialog && filter != null) {
                            FilterDialog(
                                defaultValues = filter!!,
                                onDismiss = { showFilterDialog = false },
                                onDone = {
                                    updateFeedCoroutineScope.launch {
                                        articlesLazyListState.scrollToItem(0)

                                    }
                                    viewModel.updateFilter(it)
                                    showFilterDialog = false
                                })
                        }
                        var refreshing by remember { mutableStateOf(false) }

                        if (articles != null) {
                            LaunchedEffect(key1 = viewModel.isLoadingArticles.value, block = {
                                if (viewModel.isLoadingArticles.value == false){
                                    refreshing = false
                                }
                            })

                            CollapsingContent(
                                collapsingContent = {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colors.surface)
                                                .clickable { showFilterDialog = true }
                                                .padding(12.dp),
                                            text = filterTitle,
                                            style = MaterialTheme.typography.body2,
                                            fontWeight = FontWeight.W500,
                                            color = MaterialTheme.colors.onSurface.copy(0.75f)
                                        )
                                        Divider(modifier = Modifier.align(Alignment.BottomCenter))
                                    }
                                },
                                doCollapse = !isUpdatingInProgress.value
                            ) {
                                PagedRefreshableHabrSnippetsColumn(
                                    data = articles!!,
                                    lazyListState = articlesLazyListState,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    contentPadding = PaddingValues(8.dp),
                                    onNextPageLoad = viewModel::loadArticles,
                                    page = pageNumber,
                                    refreshing = refreshing,
                                    isInProgress = isUpdatingInProgress,
                                    onRefresh = {
                                        updateFeedCoroutineScope.launch(Dispatchers.IO) {
                                            refreshing = true
                                            pageNumber.value = 1
                                            viewModel.loadArticles(1)
                                            //readyToScrollUp.value = true
//                                            refreshing = false
                                        }
                                    },
                                    readyToScrollUpAfterRefresh = readyToScrollUp
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onCommentsClick = { onCommentsClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) }
                                    )
                                }
                            }


                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            LaunchedEffect(key1 = isAuthorized) {
                                if (!viewModel.articles.isInitialized) {
                                    viewModel.loadArticles(1)
                                }
                            }
                        }


                    },
                    "Новости" to {
                        val newsList by viewModel.news.observeAsState()
                        val pageNumber = rememberSaveable { mutableStateOf(1) }
                        val updateFeedCoroutineScope = rememberCoroutineScope()
                        var isRefreshing by rememberSaveable { mutableStateOf(false) }
                        val swipestate = rememberPullRefreshState(
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
                                    contentColor = MaterialTheme.colors.primary,
                                    refreshing = isRefreshing, state = swipestate
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
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
                                lazyListState = hubsLazyListState,
                                data = hubs!!,
                                onNextPageLoad = {
                                    commonCoroutineScope.launch(Dispatchers.IO) {
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
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
                                lazyListState = authorsLazyListState,
                                data = authors!!,
                                onNextPageLoad = {
                                    commonCoroutineScope.launch(Dispatchers.IO) {
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
                                lazyListState = companiesLazyListState,
                                data = companies!!,
                                onNextPageLoad = {
                                    commonCoroutineScope.launch(Dispatchers.IO) {
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
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
                if (isAuthorized) map =
                    mapOf<String, @Composable () -> Unit>(
                        "Моя лента" to {
                            var refreshing by remember { mutableStateOf(false) }
                            var scrollAfterRefresh = remember { mutableStateOf(false) }
                            val articles by viewModel.myFeedArticles.observeAsState()
                            if (articles != null) {
                                PagedRefreshableHabrSnippetsColumn(
                                    lazyListState = myFeedLazyListState,
                                    data = articles!!,
                                    onNextPageLoad = {
                                        commonCoroutineScope.launch(Dispatchers.IO) {
                                            ArticlesListController.getArticlesSnippets(
                                                "articles",
                                                mapOf(
                                                    "custom" to "true",
                                                    "page" to it.toString()
                                                )
                                            )?.let {
                                                viewModel.myFeedArticles.postValue(
                                                    articles!! + it
                                                )
                                            }
                                        }
                                    },
                                    refreshing = refreshing,
                                    readyToScrollUpAfterRefresh = scrollAfterRefresh,
                                    onRefresh = {
                                        refreshing = true
                                        viewModel.viewModelScope.launch(Dispatchers.IO) {
                                            ArticlesListController.getArticlesSnippets(
                                                "articles",
                                                mapOf("custom" to "true")
                                            )?.let {
                                                viewModel.myFeedArticles.postValue(it)
                                                scrollAfterRefresh.value = true
                                            }
                                            refreshing = false
                                        }
                                    }
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) },
                                        onCommentsClick = { onCommentsClicked(it.id) },
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
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

            HabrScrollableTabRow(
                pagerState = pagerState,
                tabs = pages.keys.toList(),
                onCurrentPositionTabClick = { index, title ->
                    when (title) {
                        "Моя лента" -> ScrollUpMethods.scrollLazyList(myFeedLazyListState)
                        "Статьи" -> ScrollUpMethods.scrollLazyList(articlesLazyListState)
                        "Новости" -> ScrollUpMethods.scrollLazyList(newsLazyListState)
                        "Хабы" -> ScrollUpMethods.scrollLazyList(hubsLazyListState)
                        "Авторы" -> ScrollUpMethods.scrollLazyList(authorsLazyListState)
                        "Компании" -> ScrollUpMethods.scrollLazyList(companiesLazyListState)
                    }
                })
            HorizontalPager(
                state = pagerState,
                pageCount = pages.size,
            ) {
                pages.values.elementAt(it)()

            }
        }
    }
}



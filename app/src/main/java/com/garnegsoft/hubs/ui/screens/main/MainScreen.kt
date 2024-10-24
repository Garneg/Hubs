package com.garnegsoft.hubs.ui.screens.main


import ArticleController
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.FilterSavingController
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CompaniesListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.HubsListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClicked: (articleId: Int) -> Unit,
    onSearchClicked: () -> Unit,
    onCommentsClicked: (articleId: Int) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onSavedArticles: () -> Unit,
    menu: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isAuthorized by rememberSaveable { mutableStateOf(false) }
    val authorizedState by AuthDataController.isAuthorizedFlow(context)
        .collectAsState(initial = null)

    LaunchedEffect(key1 = authorizedState, block = {
        authorizedState?.let {
            isAuthorized = it
        }
    })

    val viewModel = viewModel<MainScreenViewModel>(viewModelStoreOwner = viewModelStoreOwner) {
        // todo: Replace runblocking with something better as it probably slows down initialization of main screen
        runBlocking {
            MainScreenViewModel(
                myFeedFilterInitialValue = FilterSavingController.getMyFeedFilter(
                    context,
                    MyFeedFilter.defaultValues
                ),
                articlesFilterInitialValue = FilterSavingController.getArticlesFilter(
                    context,
                    ArticlesFilter.defaultValues
                ),
                newsFilterInitialValue = FilterSavingController.getNewsFilter(
                    context,
                    NewsFilter.defaultValues
                )
            )
        }
    }

    val scaffoldState = rememberScaffoldState()

    val lastArticleRead by LastReadArticleController.getLastArticleFlow(context)
        .collectAsState(initial = null)

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
                        LastReadArticleController.clearLastArticle(context)
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
                    IconButton(
                        onClick = {
                            onSearchClicked()
                        }) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp),
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
        if (authorizedState != null)
            Column(
                Modifier.padding(it)
            ) {

                val myFeedLazyListState = rememberLazyListState()
                val myFeedFilterContentState = rememberCollapsingContentState()

                val articlesLazyListState = rememberLazyListState()
                val articlesListFilterContentState = rememberCollapsingContentState()

                val newsLazyListState = rememberLazyListState()
                val newsListFilterContentState = rememberCollapsingContentState()

                val hubsLazyListState = rememberLazyListState()
                val authorsLazyListState = rememberLazyListState()
                val companiesLazyListState = rememberLazyListState()


                val pages = remember(key1 = isAuthorized) {
                    var map = mapOf<String, @Composable () -> Unit>(
                        "Статьи" to {
                            ArticlesListPageWithFilter(
                                listModel = viewModel.articlesListModel,
                                lazyListState = articlesLazyListState,
                                collapsingContentState = articlesListFilterContentState,
                                onArticleSnippetClick = onArticleClicked,
                                onArticleAuthorClick = onUserClicked,
                                onArticleCommentsClick = onCommentsClicked,
                                filterDialog = { defVals, onDissmis, onDone ->
                                    ArticlesFilterDialog(defVals, onDissmis,
                                        onDone = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                FilterSavingController.saveArticlesFilter(
                                                    context,
                                                    it
                                                )
                                            }
                                            onDone(it)
                                        })
                                }
                            )
                        },
                        "Новости" to {
                            ArticlesListPageWithFilter(
                                listModel = viewModel.newsListModel,
                                lazyListState = newsLazyListState,
                                collapsingContentState = newsListFilterContentState,
                                onArticleSnippetClick = onArticleClicked,
                                onArticleAuthorClick = onUserClicked,
                                onArticleCommentsClick = onCommentsClicked,
                                filterDialog = { defVals, onDismiss, onDone ->
                                    NewsFilterDialog(
                                        defaultValues = defVals,
                                        onDismiss = onDismiss,
                                        onDone = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                FilterSavingController.saveNewsFilter(context, it)
                                            }
                                            onDone(it)
                                        }
                                    )
                                }
                            )
                        },
                        "Хабы" to {
                            HubsListPage(
                                listModel = viewModel.hubsListModel,
                                lazyListState = hubsLazyListState,
                                onHubClick = onHubClicked
                            )
                        },
                        "Авторы" to {
                            UsersListPage(
                                listModel = viewModel.authorsListModel,
                                lazyListState = authorsLazyListState,
                                onUserClick = onUserClicked
                            )
                        },
                        "Компании" to {
                            CompaniesListPage(
                                listModel = viewModel.companiesListModel,
                                lazyListState = companiesLazyListState,
                                onCompanyClick = onCompanyClicked
                            )
                        }
                    )
                    if (isAuthorized) map =
                        mapOf<String, @Composable () -> Unit>(
                            "Моя лента" to {
                                ArticlesListPageWithFilter(
                                    listModel = viewModel.myFeedArticlesListModel,
                                    collapsingContentState = myFeedFilterContentState,
                                    lazyListState = myFeedLazyListState,
                                    onArticleSnippetClick = onArticleClicked,
                                    onArticleAuthorClick = onUserClicked,
                                    onArticleCommentsClick = onCommentsClicked
                                ) { defaultValues, onDismiss, onDone ->
                                    MyFeedFilter(
                                        defaultValues = defaultValues,
                                        onDismiss = onDismiss,
                                        onDone = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                FilterSavingController.saveMyFeedFilter(context, it)
                                            }
                                            onDone(it)
                                        }
                                    )
                                }
                            }) + map
                    map
                }
                val pagerState = rememberPagerState { pages.size }

                var showNoInternetConnectionElement by rememberSaveable {
                    mutableStateOf(false)
                }


                LaunchedEffect(key1 = Unit, block = {
                    showNoInternetConnectionElement = !CheckInternetConnection(context)
                })
                if (showNoInternetConnectionElement) {

                    NoInternetElement(
                        onTryAgain = {
                            val connected = CheckInternetConnection(context)
                            showNoInternetConnectionElement = !connected
                            connected
                        },
                        onSavedArticles = onSavedArticles
                    )

                } else {

                    HabrScrollableTabRow(
                        pagerState = pagerState,
                        tabs = pages.keys.toList(),
                        onCurrentPositionTabClick = { index, title ->
                            when (title) {
                                "Моя лента" -> {
                                    myFeedFilterContentState.show()
                                    ScrollUpMethods.scrollLazyList(myFeedLazyListState)
                                }

                                "Статьи" -> {
                                    articlesListFilterContentState.show()
                                    ScrollUpMethods.scrollLazyList(articlesLazyListState)
                                }

                                "Новости" -> {
                                    newsListFilterContentState.show()
                                    ScrollUpMethods.scrollLazyList(newsLazyListState)
                                }

                                "Хабы" -> ScrollUpMethods.scrollLazyList(hubsLazyListState)
                                "Авторы" -> ScrollUpMethods.scrollLazyList(authorsLazyListState)
                                "Компании" -> ScrollUpMethods.scrollLazyList(companiesLazyListState)
                            }
                        })
                    HorizontalPager(
                        state = pagerState,
                    ) {
                        pages.values.elementAt(it)()

                    }
                }
            }
    }
}

suspend fun CheckInternetConnection(context: Context): Boolean {

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager.activeNetwork == null) {
        return false
    } else {
        return withContext(Dispatchers.IO) {
            for (i in 1..3) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("habr.com", 80), 2000)
                    }
                    break
                } catch (e: IOException) {
                    if (i == 3) {
                        return@withContext false
                    }

                }
            }
            return@withContext true
        }

    }


}


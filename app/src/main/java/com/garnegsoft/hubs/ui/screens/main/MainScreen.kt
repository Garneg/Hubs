package com.garnegsoft.hubs.ui.screens.main


import ArticleController
import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.FilterSavingController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.api.tts.LocalMediaController
import com.garnegsoft.hubs.api.utils.checkAppCanOpenLinks
import com.garnegsoft.hubs.ui.common.HabrScrollableTabRow
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import com.garnegsoft.hubs.ui.common.PlayerDialog
import com.garnegsoft.hubs.ui.common.ScrollUpMethods
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CompaniesListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.HubsListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClicked: (articleId: Int) -> Unit,
    onSubscriptionsClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    onCommentsClicked: (articleId: Int) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onSavedArticles: () -> Unit,
    menu: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mediaController = LocalMediaController.current
    val coroutineScope = rememberCoroutineScope()
    var isAuthorized by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val authorizedState by AuthDataController.isAuthorizedFlow(context)
        .collectAsState(initial = null)

    LaunchedEffect(key1 = authorizedState, block = {
        authorizedState?.let {
            isAuthorized = it
        }
    })


    // TODO: Move it to its own file so it won't bother when refactoring main screen 
    // Kinda ugly, isn't it? Have to come up with something better next time when building things
    val showSetOpenUrlByDefaultDialogPreference by collectPreferenceAsState(
        HubsDataStore.applicationFlags.ShowSetOpenUrlByDefaultDialog
    )

    // allows/disallows launched effect check values and show dialog (works as cache)
    var setOpenByDefaultDialogShown by rememberSaveable { mutableStateOf(false) }

    // actually shows dialog
    var showSetOpenByDefaultDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showSetOpenUrlByDefaultDialogPreference) {
        if (!setOpenByDefaultDialogShown && showSetOpenUrlByDefaultDialogPreference == true && !checkAppCanOpenLinks(
                context
            )
        ) {
            showSetOpenByDefaultDialog = true
            setOpenByDefaultDialogShown = true
        }
    }

    if (showSetOpenByDefaultDialog) {
        HandleUrlByDefaultAdviceDialog(
            onDismissRequest = {
                showSetOpenByDefaultDialog = false
            },
            onNeverShowAgain = {
                showSetOpenByDefaultDialog = false
                coroutineScope.launch {
                    HubsDataStore.applicationFlags.edit(
                        context,
                        HubsDataStore.applicationFlags.ShowSetOpenUrlByDefaultDialog,
                        false
                    )
                }
            },
            onRedirectedToSettings = {
                showSetOpenByDefaultDialog = false
            })
    }

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
        modifier = modifier,
        topBar = {
            HubsTopAppBar(
                elevation = 0.dp,
                title = { Text(text = "Хабы") },
                actions = {
                    IconButton(
                        onClick = { onSearchClicked() }
                    ) {
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

            var showPlaybackSnackElement by rememberSaveable { mutableStateOf(false) }
            val playPauseButtonState = rememberPlayPauseButtonState(mediaController)

            LaunchedEffect(mediaController) {
                mediaController?.addListener(
                    object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            showPlaybackSnackElement = playbackState == Player.STATE_READY
                            super.onPlaybackStateChanged(playbackState)
                        }
                    }
                )
            }
            var showPlayerDialog by remember { mutableStateOf(false) }

            PlayerDialog(
                show = showPlayerDialog,
                onDismissRequest = { showPlayerDialog = false },
                mediaController = mediaController,
                onAuthorClick = {
                    mediaController?.mediaMetadata?.author?.let {
                        onUserClicked(it.drop(1).toString())
                    }
                },
                onTitleClick = {
                    mediaController?.mediaMetadata?.extras?.getInt("articleId")?.let {
                                if (it != 0) {
                                    onArticleClicked(it)
                                }
                            }
                },
                article = null
            )

            if (showPlaybackSnackElement) {

                Box(
                    modifier = Modifier
                        .pointerInput(Unit) { }
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black.copy(0.33f)),
                            )
                        }
                        .navigationBarsPadding()
                        .padding(12.dp)
                        .fillMaxWidth()
                        .widthIn(max = 550.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
//                            mediaController?.mediaMetadata?.extras?.getInt("articleId")?.let {
//                                if (it != 0) {
//                                    onArticleClicked(it)
//                                }
//                            }
                            showPlayerDialog = true
                        }
                        .background(MaterialTheme.colors.surface.run {
                            if (MaterialTheme.colors.isLight)
                                copy(1f, red * 0.95f, green * 0.95f, blue * 0.95f)
                            else
                                copy(1f, red * 1.75f, green * 1.83f, blue * 1.9f)
                        })
                        .padding(8.dp),
                ) {
                    Row {
                        AsyncImage(
                            modifier = Modifier
                                .size(48.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colors.onSurface.copy(0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clip(RoundedCornerShape(4.dp)),
                            model = mediaController?.mediaMetadata?.artworkUri,
                            contentDescription = "article artwork",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.article)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.basicMarquee(3),
                                text = mediaController?.mediaMetadata?.title?.toString() ?: "Проигрывается статья",
                                fontWeight = FontWeight.W700,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mediaController?.mediaMetadata?.artist?.toString() ?: "Неизвестный автор",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                                color = MaterialTheme.colors.onSurface.copy(0.5f)
                            )
                        }


                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(

                            onClick = {
                                playPauseButtonState.onClick()
                            }
                        ) {
                            if (playPauseButtonState.showPlay) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "play",
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.pause_icon),
                                    contentDescription = "pause"
                                )
                            }
                        }
                    }
                }

            } else {
                SnackbarHost(
                    modifier = Modifier.safeDrawingPadding(),
                    hostState = it
                ) {
                    ContinueReadSnackBar(data = it)
                }
            }
        }
    ) {
        if (isAuthorized != null)
            Column(
                Modifier
                    .padding(it)
                    .padding(
                        WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal).asPaddingValues(),
                    )
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


                val pages = remember(isAuthorized) {
                    buildMap<String, @Composable () -> Unit> {
                        if (isAuthorized == true) {
                            put("Моя лента", {
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
                            })
                        }
                        put("Статьи", {
                            ArticlesListPageWithFilter(
                                listModel = viewModel.articlesListModel,
                                lazyListState = articlesLazyListState,
                                collapsingContentState = articlesListFilterContentState,
                                onArticleSnippetClick = onArticleClicked,
                                onArticleAuthorClick = onUserClicked,
                                onArticleCommentsClick = onCommentsClicked,
                                filterDialog = { defVals, onDissmis, onDone ->
                                    ArticlesFilterDialog(
                                        defVals, onDissmis,
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
                        })
                        put("Новости", {
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
                        })
                        put("Хабы", {
                            HubsListPage(
                                listModel = viewModel.hubsListModel,
                                lazyListState = hubsLazyListState,
                                onHubClick = onHubClicked
                            )
                        })
                        put("Авторы", {
                            UsersListPage(
                                listModel = viewModel.authorsListModel,
                                lazyListState = authorsLazyListState,
                                onUserClick = onUserClicked
                            )
                        })
                        put("Компании", {
                            CompaniesListPage(
                                listModel = viewModel.companiesListModel,
                                lazyListState = companiesLazyListState,
                                onCompanyClick = onCompanyClicked
                            )
                        })
                    }
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
                        key = { pages.keys.elementAt(it) }
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


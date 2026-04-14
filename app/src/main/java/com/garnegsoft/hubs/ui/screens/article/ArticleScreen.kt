package com.garnegsoft.hubs.ui.screens.article

import android.content.Intent
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.history.HistoryController
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(
    modifier: Modifier = Modifier,
    articleId: Int,
    onBackButtonClicked: () -> Unit,
    onCommentsClick: () -> Unit,
    onAuthorClick: (alias: String) -> Unit,
    onHubClick: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
    onViewImageRequest: (url: String) -> Unit,
    onArticleClick: (id: Int) -> Unit,
    navigationTransition: Transition<EnterExitState>,
    viewModelStoreOwner: ViewModelStoreOwner
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val userAuthenticated by AuthDataController.isAuthorizedFlow(context).collectAsState(false)
    val fontSizePreference by HubsDataStore.Settings.ArticleScreen.FontSize
        .getFlow(context)
        .collectAsState(initial = null)
    var fontSize by rememberSaveable { mutableStateOf<Float?>(null) }

    val viewModel = viewModel<ArticleScreenViewModel>(viewModelStoreOwner)
    val article by viewModel.article.observeAsState()
    val company by viewModel.company.observeAsState()

    var revealArticleContent by rememberSaveable { mutableStateOf(false) }

    var firstVisibleItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var firstVisibleItemOffset by rememberSaveable { mutableIntStateOf(0) }

    val lazyListState =
        rememberSaveable(saver = LazyListState.Saver) { LazyListState(firstVisibleItemIndex, firstVisibleItemOffset) }

    Log.i("LAZYLISTSTATE", firstVisibleItemIndex.toString())

    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
        firstVisibleItemOffset = lazyListState.firstVisibleItemScrollOffset
    }

    LaunchedEffect(lazyListState.layoutInfo.totalItemsCount) {
        if (lazyListState.layoutInfo.totalItemsCount > 0 && lazyListState.firstVisibleItemIndex == 0 && firstVisibleItemIndex > 0) {
            lazyListState.scrollToItem(firstVisibleItemIndex, firstVisibleItemOffset)
            revealArticleContent = true
        }
    }


    LaunchedEffect(key1 = Unit, block = {
        if (!viewModel.article.isInitialized) {
            viewModel.loadArticle(articleId)
        }
        if (!viewModel.mostReadingArticles.isInitialized) {
            viewModel.loadMostReading()
        }
    })

    LaunchedEffect(fontSizePreference) {
        if (fontSizePreference != null && fontSize != fontSizePreference) {
            fontSize = fontSizePreference
        }
    }


//    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
//
//    }


    val shareIntent = remember(article?.title) {
        val sendIntent = Intent(Intent.ACTION_SEND)

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${article?.title ?: ""} — https://habr.com/p/${articleId}/"
        )
        sendIntent.setType("text/plain")
        Intent.createChooser(sendIntent, null)
    }
    val articleSaved by viewModel.articleExists(LocalContext.current, articleId)
        .collectAsState(false)
    var articleContentParsed by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            HubsTopAppBar(
                title = { Text(text = "Публикация") },
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { onBackButtonClicked() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    if (articleSaved) {
                        IconButton(
                            onClick = {
                                viewModel.deleteSavedArticle(
                                    id = articleId,
                                    context = context
                                )
                            },
                            enabled = true
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "")
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.saveArticle(id = articleId, context = context) },
                            enabled = true
                        ) {
                            Icon(painterResource(id = R.drawable.download), contentDescription = "")
                        }
                    }

                    IconButton(
                        onClick = { context.startActivity(shareIntent) },
                        enabled = article != null
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "")
                    }
                })
        },
        backgroundColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else MaterialTheme.colors.background,
        bottomBar = {
            article?.let { article ->
                AnimatedVisibility(
                    visible = revealArticleContent && articleContentParsed,
                    enter = slideInVertically { it } + fadeIn()
                ) {
                    ArticleScreenBottomBar(
                        article = article,
                        enableBookmarkButton = userAuthenticated,
                        onCommentsClick = onCommentsClick
                    )
                }

            }
        }
    ) {
        val coroutineScope = rememberCoroutineScope()
        LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
            coroutineScope.launch {
                delay(400) // minimal amount of time for article content to wait before appearing on screen (made to avoid flickering)
                revealArticleContent = true
            }
        }
        RevealContainer(
            hideContent = article == null || !articleContentParsed || (article?.isCorporative == true && company == null) || !revealArticleContent,
            overlappingContent = {
                GenericSkeleton(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (revealArticleContent) Modifier else Modifier.pointerInput(Unit) {}), articleId
                )
//				Box(modifier = Modifier.fillMaxSize().background(Color.Red).pointerInput(Unit) {})
            }
        ) {
            article?.let { article ->
                val color = MaterialTheme.colors.onSurface
                val spanStyle = remember(fontSize, color) {
                    SpanStyle(
                        color = color,
                        fontSize = fontSize?.sp ?: 16.sp
                    )
                }

                LaunchedEffect(key1 = Unit, block = {
                    LastReadArticleController.setLastArticle(context, articleId)
                    HistoryController.insertArticle(articleId, context)

                    if (!viewModel.parsedArticleContent.isInitialized && fontSize != null) {
                        val element =
                            Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()!!
                                .child(0)
                                ?: Element("")

                        viewModel.parsedArticleContent.postValue(
                            parseChildElements(
                                element,
                                spanStyle,
                                onViewImageRequest
                            ).second
                        )
                        articleContentParsed = true
                    }
                })
                val density = LocalDensity.current

                Box(modifier = Modifier.padding(it)) {
                    AnimatedVisibility(
                        visible = articleContentParsed &&
                                fontSize != null &&
//                                (navigationTransition.currentState == EnterExitState.Visible ) &&
                                !(article.isCorporative == true && company == null) &&
                                revealArticleContent,
                        enter =
                            fadeIn(
                            animationSpec = tween(durationMillis = 200, easing = EaseOut)
                        ) +
//                                    scaleIn(
//                                        initialScale = 0.95f,
//                                        animationSpec = tween(
//                                            durationMillis = 200,
//                                            easing = EaseOutQuad
//                                        )
//                                    ) +
                                    slideInVertically(
                                        animationSpec = tween(
                                            durationMillis = 250,
                                            easing = EaseOut
                                        )
                                    ) { (density.density * 32f).roundToInt() }
                    ) {

                        SelectionContainer {
                            ArticleContent(
                                article = article,
                                onAuthorClicked = { onAuthorClick(article.author!!.alias) },
                                onHubClicked = onHubClick,
                                onCompanyClick = onCompanyClick,
                                onViewImageRequest = onViewImageRequest,
                                onArticleClick = onArticleClick,
                                fontSize = fontSize!!.sp,
                                lazyListState = lazyListState
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.garnegsoft.hubs.ui.screens.article

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.LastReadArticleController
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.api.history.HistoryController
import com.garnegsoft.hubs.api.tts.HubsTTSService
import com.garnegsoft.hubs.api.tts.LocalMediaController
import com.garnegsoft.hubs.api.tts.TTSBinder
import com.garnegsoft.hubs.api.tts.toArticleMetadata
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import kotlinx.coroutines.delay
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.ui.common.BaseMenuContainer
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.common.HubChip
import com.garnegsoft.hubs.ui.common.MenuItem
import com.garnegsoft.hubs.ui.common.PlayerDialog
import com.garnegsoft.hubs.ui.screens.article.tts.TtsTestDialog
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.concurrent.Executor
import java.util.concurrent.Executors
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
    var ttsMediaController by remember { mutableStateOf<MediaController?>(null) }
    var ttsBinder by remember { mutableStateOf<TTSBinder?>(null) }

    val activity = LocalActivity.current
    val userAuthenticated by AuthDataController.isAuthorizedFlow(context).collectAsState(false)
    val fontSizePreference by collectPreferenceAsState(HubsDataStore.Settings.ArticleScreen.FontSize)
    var fontSize by rememberSaveable { mutableStateOf<Float?>(null) }

    val viewModel = viewModel<ArticleScreenViewModel>(viewModelStoreOwner)
    val article by viewModel.article.observeAsState()
    val company by viewModel.company.observeAsState()

    var revealArticleContent by rememberSaveable { mutableStateOf(false) }

    var firstVisibleItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var firstVisibleItemOffset by rememberSaveable { mutableIntStateOf(0) }

    val lazyListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(firstVisibleItemIndex, firstVisibleItemOffset)
    }

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

    var showTtsDialog by remember { mutableStateOf(false) }
//    TtsTestDialog(showTtsDialog, {showTtsDialog = false}, binder = ttsBinder, mediaController = LocalMediaController.current)

    val mediaController = LocalMediaController.current
    mediaController?.let {
        PlayerDialog(
            show = showTtsDialog,
            onDismissRequest = { showTtsDialog = false },
            mediaController = mediaController,
            onTitleClick = {},
            onAuthorClick = { onAuthorClick(mediaController.mediaMetadata.toArticleMetadata().author)},
            article = article,
            onCurrentPlayingClick = { onArticleClick(mediaController.mediaMetadata.toArticleMetadata().articleId) }
        )
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
                    var showMoreMenu by remember { mutableStateOf(false) }

                    val showMoreTransition = updateTransition(showMoreMenu)
                    val showMoreMenuAlphaAnimated by showMoreTransition.animateFloat(
                        { tween(durationMillis = 150) }
                    ) {
                        if (it) 1f else 0f
                    }
                    val showMoreMenuScaleAnimated by showMoreTransition.animateFloat(
                        { tween(durationMillis = 150) }
                    ) {
                        if (it) 1f else 0.9f
                    }
                    val showMoreMenuOffsetAnimated by showMoreTransition.animateDp(
                        { tween(durationMillis = 200) }
                    ) {
                        if (it) 0.dp else (-2).dp
                    }

                    Box {

                        IconButton(
                            onClick = { showMoreMenu = true }
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "more")
                        }
                        if (showMoreMenu || showMoreTransition.currentState) {
                            Popup(
                                properties = PopupProperties(
                                    focusable = true,
                                    dismissOnBackPress = true,
                                    dismissOnClickOutside = true
                                ),
                                popupPositionProvider = object : PopupPositionProvider {
                                    override fun calculatePosition(
                                        anchorBounds: IntRect,
                                        windowSize: IntSize,
                                        layoutDirection: LayoutDirection,
                                        popupContentSize: IntSize
                                    ): IntOffset {
                                        return IntOffset(x = anchorBounds.right, y = anchorBounds.bottom)
                                    }

                                },
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                BaseMenuContainer(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            alpha = showMoreMenuAlphaAnimated
//                                            translationX = (((1f - showMoreMenuScaleAnimated) * size.width))
                                            translationY = showMoreMenuOffsetAnimated.toPx() - (((1f - showMoreMenuScaleAnimated) * size.height))
                                            scaleY = showMoreMenuScaleAnimated
//                                            scaleX = showMoreMenuScaleAnimated
                                        }
                                ) {
                                    MenuItem(
                                        title = "Поделиться",
                                        icon = {
                                            Icon(Icons.Outlined.Share, contentDescription = "")
                                        },
                                        onClick = {
                                            if (article != null) context.startActivity(shareIntent)
                                            showMoreMenu = false
                                        }
                                    )
                                    if (articleSaved) {
                                        MenuItem(
                                            title = "Удалить из загруженных",
                                            icon = {
                                                Icon(Icons.Outlined.Delete, contentDescription = "")
                                            },
                                            onClick = {
                                                viewModel.deleteSavedArticle(
                                                    id = articleId,
                                                    context = context
                                                )
                                                showMoreMenu = false

                                            }
                                        )
                                    } else {
                                        MenuItem(
                                            title = "Загрузить",
                                            icon = {
                                                Icon(painterResource(R.drawable.download), contentDescription = "")
                                            },
                                            onClick = {
                                                viewModel.saveArticle(id = articleId, context = context)
                                                showMoreMenu = false
                                            }
                                        )
                                    }

                                    MenuItem(
                                        title = "Послушать",
                                        icon = {
                                            Icon(painterResource(id = R.drawable.headphones), contentDescription = "")
                                        },
                                        onClick = {
                                            showTtsDialog = true
                                            showMoreMenu = false
                                        }
                                    )

                                }
                            }
                        }
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

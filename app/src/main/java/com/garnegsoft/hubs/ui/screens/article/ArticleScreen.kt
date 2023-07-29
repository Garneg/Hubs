package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.lastReadDataStore
import com.garnegsoft.hubs.settingsDataStoreFlowWithDefault
import com.garnegsoft.hubs.settingsDataStoreFlow
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.user.HubChip
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import kotlin.math.abs



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(
    articleId: Int,
    onBackButtonClicked: () -> Unit,
    onCommentsClicked: () -> Unit,
    onAuthorClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
    onViewImageRequest: (url: String) -> Unit,
    onArticleClick: (id: Int) -> Unit,
    viewModelStoreOwner: ViewModelStoreOwner,
    isOffline: Boolean = false
) {
    val context = LocalContext.current
    val fontSize by context.settingsDataStoreFlow(HubsDataStore.Settings.Keys.ArticleScreen.FontSize).collectAsState(
        initial = MaterialTheme.typography.body1.fontSize.value
    )
    val viewModel = viewModel<ArticleScreenViewModel>(viewModelStoreOwner)
    val article by viewModel.article.observeAsState()
    val offlineArticle by viewModel.offlineArticle.observeAsState()

    LaunchedEffect(key1 = Unit, block = {
        if (!viewModel.article.isInitialized) {
            if (isOffline) {
                viewModel.loadArticleFromLocalDatabase(articleId, context)
            } else {
                viewModel.loadArticle(articleId)
            }
        }
        if (!viewModel.mostReadingArticles.isInitialized){
            viewModel.loadMostReading()
        }
    })

    val scrollState = rememberScrollState()

    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val shareIntent = remember(article?.title, offlineArticle?.title) {
        val sendIntent = Intent(Intent.ACTION_SEND)

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${article?.title ?: offlineArticle?.title} — https://habr.com/p/${articleId}/"
        )
        sendIntent.setType("text/plain")
        Intent.createChooser(sendIntent, null)
    }
    val articleSaved by viewModel.articleExists(LocalContext.current, articleId).collectAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Публикация") },
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { onBackButtonClicked() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    if (articleSaved){
                        IconButton(
                            onClick = { viewModel.deleteSavedArticle(id = articleId, context = context) },
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
                        enabled = article != null || offlineArticle != null
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "")
                    }
                })
        },
        backgroundColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else MaterialTheme.colors.background,
        bottomBar = {
            article?.let { article ->
                BottomAppBar(
                    elevation = 0.dp,
                    backgroundColor = MaterialTheme.colors.surface,
                    modifier = Modifier
                        .height(60.dp)
                ) {
                    var showVotesCounter by remember {
                        mutableStateOf(false)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                showVotesCounter = !showVotesCounter
                            }
                    ) {
                        val positionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                return IntOffset(
                                    anchorBounds.left,
                                    anchorBounds.top - popupContentSize.height - 10
                                )
                            }

                        }
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(key1 = showVotesCounter, block = {
                            if (showVotesCounter) {
                                visible = showVotesCounter
                            }
                        })
                        LaunchedEffect(key1 = visible, block = {
                            delay(150)
                            if (!visible) {
                                showVotesCounter = false
                            }

                        })
                        val offset by animateFloatAsState(
                            targetValue = if (visible) 0f else 8f,
                            animationSpec = tween(150)
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (visible) 1f else 0.0f,
                            animationSpec = tween(150)
                        )
                        if (showVotesCounter) {
                            Popup(
                                popupPositionProvider = positionProvider,
                                onDismissRequest = { visible = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .offset(0.dp, offset.dp)
                                        .alpha(alpha)
                                        .padding(2.dp)
                                        .shadow(1.5.dp, shape = RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colors.surface)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Всего голосов " +
                                                "${article.statistics.votesCountMinus + article.statistics.votesCountPlus}: " +
                                                "￪${article.statistics.votesCountPlus} и " +
                                                "￬${article.statistics.votesCountMinus}",
                                        color = statisticsColor
                                    )
                                }

                            }

                        }

                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(id = R.drawable.rating),
                            contentDescription = "",
                            tint = statisticsColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (article.statistics.score > 0)
                                "+" + article.statistics.score.toString()
                            else
                                article.statistics.score.toString(),
                            color = if (article.statistics.score > 0)
                                RatingPositive
                            else if (article.statistics.score < 0)
                                RatingNegative
                            else
                                statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()

                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(id = R.drawable.views_icon),
                            contentDescription = "",
                            tint = statisticsColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            formatLongNumbers(article.statistics.readingCount.toInt()),
                            color = statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }
                    var addedToBookmarks by rememberSaveable(article.relatedData?.bookmarked) {
                        mutableStateOf(article.relatedData?.bookmarked ?: false)
                    }
                    var addedToBookmarksCount by rememberSaveable(article.statistics.favoritesCount) {
                        mutableStateOf(article.statistics.favoritesCount)
                    }
                    val favoriteCoroutineScope = rememberCoroutineScope()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = article.relatedData != null
                            ) {
                                article.relatedData?.let {
                                    favoriteCoroutineScope.launch(Dispatchers.IO) {
                                        if (addedToBookmarks) {
                                            addedToBookmarks = false
                                            addedToBookmarksCount--
                                            addedToBookmarksCount =
                                                addedToBookmarksCount.coerceAtLeast(0)
                                            if (!ArticleController.removeFromBookmarks(article.id)) {
                                                addedToBookmarks = true
                                                addedToBookmarksCount++
                                                addedToBookmarksCount =
                                                    addedToBookmarksCount.coerceAtLeast(0)
                                            }

                                        } else {
                                            addedToBookmarks = true
                                            addedToBookmarksCount++
                                            if (!ArticleController.addToBookmarks(article.id)) {
                                                addedToBookmarks = false
                                                addedToBookmarksCount--
                                                addedToBookmarksCount =
                                                    addedToBookmarksCount.coerceAtLeast(0)
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter =
                            article.relatedData?.let {
                                if (addedToBookmarks)
                                    painterResource(id = R.drawable.bookmark_filled)
                                else
                                    null
                            } ?: painterResource(id = R.drawable.bookmark),
                            contentDescription = "",
                            tint = statisticsColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = addedToBookmarksCount.toString(),
                            color = statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onCommentsClicked)
                    ) {
                        BadgedBox(
                            modifier = Modifier.align(Alignment.Center),
                            badge = {
                                article.relatedData?.let {
                                    if (it.unreadComments > 0 && it.unreadComments < article.statistics.commentsCount) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(RatingPositive)
                                        )
                                    }
                                }
                            }) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    painter = painterResource(id = R.drawable.comments_icon),
                                    contentDescription = "",
                                    tint = statisticsColor

                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = formatLongNumbers(article.statistics.commentsCount),
                                    color = statisticsColor,
                                    fontWeight = FontWeight.W500
                                )
                            }
                        }
                    }
                }
                Divider()
            }
        }
    ) {
        if (isOffline) {
            offlineArticle?.let { article ->
                val flingSpec = rememberSplineBasedDecay<Float>()
                Row {
                    Column(
                        modifier = Modifier
                            .verticalScroll(
                                state = scrollState,
                                flingBehavior = object : FlingBehavior {
                                    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                                        if (abs(initialVelocity) <= 1f)
                                            return initialVelocity

                                        val performedInitialVelocity = initialVelocity * 1.2f

                                        var velocityLeft = performedInitialVelocity
                                        var lastValue = 0f
                                        AnimationState(
                                            initialValue = 0f,
                                            initialVelocity = performedInitialVelocity
                                        ).animateDecay(flingSpec) {
                                            val delta = value - lastValue
                                            val consumed = scrollBy(delta)
                                            lastValue = value
                                            velocityLeft = velocity

                                            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()

                                        }
                                        return velocityLeft

                                    }

                                }
                            )
                            .padding(bottom = 12.dp)
                            .padding(16.dp)
                    ) {
                        if (article.authorName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (article.authorAvatarUrl != null)
                                    AsyncImage(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
                                        model = article.authorAvatarUrl,
                                        contentDescription = ""
                                    )
                                else
                                    Icon(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .border(
                                                width = 2.dp,
                                                color = placeholderColor(article.authorName),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(
                                                Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(2.dp),
                                        painter = painterResource(id = R.drawable.user_avatar_placeholder),
                                        contentDescription = "",
                                        tint = placeholderColor(article.authorName)
                                    )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = article.authorName, fontWeight = FontWeight.W600,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onBackground
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            SelectionContainer() {
                                Text(
                                    text = article.title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.W700,
                                    color = MaterialTheme.colors.onBackground
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painter = painterResource(id = R.drawable.clock_icon),
                                    modifier = Modifier.size(14.dp),
                                    contentDescription = "",
                                    tint = statisticsColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${article.readingTime} мин",
                                    color = statisticsColor,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                if (article.isTranslation) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.translate),
                                        modifier = Modifier.size(14.dp),
                                        contentDescription = "",
                                        tint = statisticsColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Перевод",
                                        color = statisticsColor,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.offline),
                                        modifier = Modifier.size(14.dp),
                                        contentDescription = "",
                                        tint = statisticsColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "оффлайн режим",
                                        color = statisticsColor,
                                        fontWeight = FontWeight.W500,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            var hubsText by remember { mutableStateOf("") }

                            LaunchedEffect(key1 = Unit, block = {
                                if (hubsText == "") {
                                    hubsText = article.hubs.hubsList.joinToString(separator = ", ") {
                                        it.replace(" ", "\u00A0")
                                    }
                                }
                            })
                            // Hubs
                            Text(
                                text = hubsText, style = TextStyle(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.W500
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val elementsSettings = remember {
                                ElementSettings(
                                    fontSize = fontSize?.sp ?: 16.sp,
                                    lineHeight = 16.sp,
                                    fitScreenWidth = false
                                )
                            }
                            SelectionContainer() {
                                parseElement(
                                    element = Jsoup.parse(article.contentHtml),
                                    spanStyle = SpanStyle(
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = fontSize?.sp ?: MaterialTheme.typography.body1.fontSize,
                                        ),
                                    onViewImageRequest = onViewImageRequest
                                ).second?.let { it1 ->
                                    it1(
                                        SpanStyle(
                                            color = MaterialTheme.colors.onSurface,
                                            fontSize = MaterialTheme.typography.body1.fontSize
                                        ),
                                        elementsSettings
                                    )
                                }
                            }
                            // Tags
//                        Row(
//                            verticalAlignment = Alignment.Top,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        ) {
//                            Text(
//                                text = "Теги:",
//                                style = TextStyle(color = Color.Gray, fontWeight = FontWeight.W500)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//
//                            var tags = String()
//
//                            article.tags.forEach { tags += "$it, " }
//                            if (tags.length > 0) tags = tags.dropLast(2)
//                            Text(
//                                text = tags,
//                                style = TextStyle(
//                                    color = Color.Gray,
//                                    fontWeight = FontWeight.W500
//                                )
//                            )
//                        }
                            Divider(modifier = Modifier.padding(vertical = 24.dp))
                            // Hubs
                            TitledColumn(
                                title = "Хабы",
                                titleStyle = MaterialTheme.typography.subtitle2.copy(
                                    color = MaterialTheme.colors.onBackground.copy(
                                        0.5f
                                    )
                                )
                            ) {
//                            HubsRow(
//                                hubs = article.hubs,
//                                onHubClicked = onHubClicked,
//                                onCompanyClicked = onCompanyClick
//                            )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    article.hubs.hubsList.forEach {
                                        HubChip(it) { }
                                    }
                                }
                            }


                        }
                    }
                    ScrollBar(scrollState = scrollState)
                }
            }
        } else {
            article?.let { article ->
                LaunchedEffect(key1 = Unit, block = {
                    context.lastReadDataStore.edit {
                        it[HubsDataStore.LastRead.Keys.LastArticleRead] = articleId
                    }
                })
                Box(modifier = Modifier.padding(it)) {
                    ArticleContent(
                        article = article,
                        onAuthorClicked = { onAuthorClicked(article.author!!.alias) },
                        onHubClicked = onHubClicked,
                        onCompanyClick = onCompanyClick,
                        onViewImageRequest = onViewImageRequest,
                        onArticleClick = onArticleClick
                    )
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
        }

    }
}

@Composable
fun ScrollBar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val targetAlpha = if (scrollState.isScrollInProgress) 1f else 0f
    val duration = if (scrollState.isScrollInProgress) 150 else 1000

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    var scrollbarWidth = with(LocalDensity.current) { 3.dp.toPx() }
    val scrollbarColor =
        if (MaterialTheme.colors.isLight) Color(0x59_000000) else Color(0x59_FFFFFF)
    Box(modifier = modifier
        .fillMaxHeight()
        .width(6.dp)
        .drawBehind {

            var scrollbarheight = size.height / scrollState.maxValue * size.height
            var place = size.height - scrollbarheight
            var offsetheight =
                scrollState.value.toFloat() / scrollState.maxValue.toFloat() * place
            drawRoundRect(
                color = scrollbarColor,
                size = Size(size.width - scrollbarWidth, scrollbarheight),
                topLeft = Offset(x = -scrollbarWidth / 2, y = offsetheight),
                cornerRadius = CornerRadius(40.dp.toPx(), 40.dp.toPx()),
                alpha = alpha
            )

        })

}



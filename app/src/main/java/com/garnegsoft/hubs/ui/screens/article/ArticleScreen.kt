package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Warning
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.AsyncGifImage
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.*


class ArticleScreenViewModel : ViewModel() {
    private var _article = MutableLiveData<Article?>()
    val article: LiveData<Article?> get() = _article

    fun loadArticle(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ArticleController.get("articles/$id")?.let {
                _article.postValue(it)
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(
    articleId: Int,
    onBackButtonClicked: () -> Unit,
    onCommentsClicked: () -> Unit,
    onAuthorClicked: (alias: String) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
) {
    val viewModel = viewModel<ArticleScreenViewModel>()
    val article by viewModel.article.observeAsState()

    LaunchedEffect(key1 = Unit, block = {
        if (!viewModel.article.isInitialized)
            viewModel.loadArticle(articleId)
    })

    val scrollState = rememberScrollState()
    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val shareIntent = remember(article?.title) {
        val sendIntent = Intent(Intent.ACTION_SEND)

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${article?.title} — https://habr.com/p/${articleId}/"
        )
        sendIntent.setType("text/plain")
        Intent.createChooser(sendIntent, null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(55.dp),
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { onBackButtonClicked() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                title = {
                    Text(text = "Публикация")
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = { context.startActivity(shareIntent) },
                        enabled = article != null
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "")
                    }
                })
        },
        backgroundColor = if (MaterialTheme.colors.isLight == true) MaterialTheme.colors.surface else MaterialTheme.colors.background,
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
                        if (showVotesCounter) {
                            Popup(
                                popupPositionProvider = positionProvider,
                                onDismissRequest = { showVotesCounter = false }
                            ) {
                                Box(
                                    modifier = Modifier
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
                            article.statistics.readingCount,
                            color = statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }
                    var addedToBookmarks by rememberSaveable(article.relatedData?.bookmarked) {
                        mutableStateOf(article.relatedData?.bookmarked ?: false)
                    }
                    var addedToBookmarksCount by rememberSaveable(article.statistics.favoritesCount) {
                        mutableStateOf(article.statistics.favoritesCount.toInt())
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
                            addedToBookmarksCount.toString(),
                            color = statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(onClick = onCommentsClicked)
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(id = R.drawable.comments_icon),
                            contentDescription = "",
                            tint = statisticsColor

                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = article.statistics.commentsCount,
                            color = statisticsColor,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
                Divider()
            }
        }
    ) {
        article?.let { article ->
            Row(
                Modifier.padding(
                    top = it.calculateTopPadding(),
                    bottom = it.calculateBottomPadding()
                )
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(bottom = 12.dp)
                        .padding(16.dp)
                ) {
                    if (article.editorVersion == EditorVersion.FirstVersion) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colors.error.copy(alpha = 0.75f))
                                .padding(8.dp), verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = "",
                                tint = MaterialTheme.colors.onError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Эта статья написана с помощью первой версии редактора, некоторые элементы могут отображаться некорректно",
                                color = MaterialTheme.colors.onError
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    if (article.author != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = { onAuthorClicked(article.author.alias) }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (article.author.avatarUrl != null)
                                AsyncImage(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    model = article.author.avatarUrl,
                                    contentDescription = ""
                                )
                            else
                                Icon(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .border(
                                            width = 2.dp,
                                            color = placeholderColor(article.author.alias),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                                        .padding(2.dp),
                                    painter = painterResource(id = R.drawable.user_avatar_placeholder),
                                    contentDescription = "",
                                    tint = placeholderColor(article.author.alias)
                                )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = article.author.alias, fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                article.timePublished, color = Color.Gray,
                                fontSize = 12.sp, fontWeight = FontWeight.W400
                            )
                        }
                    }
                    if (article.postType == PostType.Megaproject && article.metadata != null) {
                        AsyncImage(article.metadata.mainImageUrl, "")
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(
                    ) {
                        SelectionContainer() {
                            Text(
                                text = article.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W800,
                                color = MaterialTheme.colors.onBackground
                            )

                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (article.complexity != PostComplexity.None) {
                                Icon(
                                    modifier = Modifier.size(height = 10.dp, width = 20.dp),
                                    painter = painterResource(id = R.drawable.speedmeter_hard),
                                    contentDescription = "",
                                    tint = when (article.complexity) {
                                        PostComplexity.Low -> Color(0xFF4CBE51)
                                        PostComplexity.Medium -> Color(0xFFEEBC25)
                                        PostComplexity.High -> Color(0xFFEB3B2E)
                                        else -> Color.Red
                                    }
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = when (article.complexity) {
                                        PostComplexity.Low -> "Простой"
                                        PostComplexity.Medium -> "Средний"
                                        PostComplexity.High -> "Сложный"
                                        else -> ""
                                    },
                                    color = when (article.complexity) {
                                        PostComplexity.Low -> Color(0xFF4CBE51)
                                        PostComplexity.Medium -> Color(0xFFEEBC25)
                                        PostComplexity.High -> Color(0xFFEB3B2E)
                                        else -> Color.Red
                                    },
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp

                                )

                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.clock_icon),
                                modifier = Modifier.size(14.dp),
                                contentDescription = "",
                                tint = statisticsColor
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${article.readingTime} мин",
                                color = statisticsColor,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        FlowRow() {
                            article.hubs.forEach {
                                val hubTitle =
                                    (if (it.isProfiled) it.title + "*" else it.title) + ", "

                                Text(
                                    modifier = Modifier.clickable {
                                        if (it.isCorporative)
                                            onCompanyClick(it.alias)
                                        else
                                            onHubClicked(it.alias)
                                    },
                                    text = hubTitle,
                                    style = TextStyle(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.W500
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionContainer() {
                            parseElement(
                                Jsoup.parse(article.contentHtml),
                                SpanStyle(
                                    color = MaterialTheme.colors.onSurface,
                                    fontSize = MaterialTheme.typography.body1.fontSize
                                )
                            ).second?.let { it1 ->
                                it1(
                                    SpanStyle(
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = MaterialTheme.typography.body1.fontSize
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Tags
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Теги:",
                                style = TextStyle(color = Color.Gray, fontWeight = FontWeight.W500)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            var tags = String()

                            article.tags.forEach { tags += "$it, " }
                            if (tags.length > 0) tags = tags.dropLast(2)
                            Text(
                                text = tags,
                                style = TextStyle(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.W500
                                )
                            )
                        }

                        // Hubs
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "Хабы:",
                                style = TextStyle(color = Color.Gray, fontWeight = FontWeight.W500)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            FlowRow() {
                                article.hubs.forEach {
                                    val hubTitle =
                                        (if (it.isProfiled) it.title + "*" else it.title) + ", "

                                    Text(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable { onHubClicked(it.alias) },
                                        text = hubTitle,
                                        style = TextStyle(
                                            color = Color.Gray,
                                            fontWeight = FontWeight.W500
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                ScrollBar(scrollState = scrollState)
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ){ CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }


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
    Box(modifier = modifier
        .fillMaxHeight()
        .width(6.dp)
        .drawBehind {

            var scrollbarheight = size.height / scrollState.maxValue * size.height
            var place = size.height - scrollbarheight
            var offsetheight =
                scrollState.value.toFloat() / scrollState.maxValue.toFloat() * place
            drawRoundRect(
                color = Color(0x59_000000),
                size = Size(size.width - scrollbarWidth, scrollbarheight),
                topLeft = Offset(x = -scrollbarWidth / 2, y = offsetheight),
                cornerRadius = CornerRadius(40.dp.toPx(), 40.dp.toPx()),
                alpha = alpha
            )

        })

}



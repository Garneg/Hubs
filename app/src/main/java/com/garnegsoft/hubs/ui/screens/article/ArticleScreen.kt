package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.article.offline.HubsList
import com.garnegsoft.hubs.api.article.offline.OfflineArticle
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.article.offline.offlineArticlesDatabase
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.lastReadDataStore
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.user.HubChip
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import kotlin.math.abs


class ArticleScreenViewModel() : ViewModel() {
    private var _article = MutableLiveData<Article?>()
    val article: LiveData<Article?> get() = _article

    private var _offlineArticle = MutableLiveData<OfflineArticle?>()
    val offlineArticle: LiveData<OfflineArticle?> get() = _offlineArticle

    fun loadArticle(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ArticleController.get("articles/$id")?.let {
                _article.postValue(it)

            }
        }
    }

    fun loadArticleFromLocalDatabase(id: Int, context: Context) {
        viewModelScope.launch(Dispatchers.IO){
            val dao = context.offlineArticlesDatabase.articlesDao()
            if (dao.exists(id)) {
                _offlineArticle.postValue(dao._getArticleById(id))

            } else {
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Статья не найдена в скачанных\nПопробуйте скачать ее заново", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveArticle(id: Int, context: Context){
        viewModelScope.launch(Dispatchers.IO){
            val dao = context.offlineArticlesDatabase.articlesDao()
            ArticleController.getSnippet(id)?.let {
                dao.insertSnippet(
                    OfflineArticleSnippet(
                        articleId = it.id,
                        authorName = it.author?.alias,
                        authorAvatarUrl = it.author?.avatarUrl,
                        timePublished = "",
                        title = it.title,
                        readingTime = it.readingTime,
                        isTranslation = it.isTranslation,
                        textSnippet = it.textSnippet,
                        hubs = HubsList(it.hubs?.map { if (it.isProfiled) it.title + "*" else it.title } ?: emptyList()),
                        thumbnailUrl = it.imageUrl
                    )
                )
            }

            ArticleController.get(id)?.let {
                dao.insert(
                    OfflineArticle(
                        articleId = it.id,
                        authorName = it.author?.alias,
                        authorAvatarUrl = it.author?.avatarUrl,
                        timePublished = "",
                        title = it.title,
                        readingTime = it.readingTime,
                        isTranslation = it.translationData.isTranslation,
                        hubs = HubsList(it.hubs.map { if (it.isProfiled) it.title + "*" else it.title }),
                        contentHtml = it.contentHtml
                    )
                )
            }
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Статья скачана!", Toast.LENGTH_SHORT).show()
            }
            Log.e("offlineArticle", "loading done")
        }
    }

    fun deleteSavedArticle(id: Int, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val dao = context.offlineArticlesDatabase.articlesDao()
            dao.delete(id)
            dao.deleteSnippet(id)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Статья удалена!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun articleExists(context: Context, articleId: Int): Flow<Boolean> {
        return context.offlineArticlesDatabase.articlesDao().existsFlow(articleId)
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
    onViewImageRequest: (url: String) -> Unit,
    isOffline: Boolean = false
) {
    val context = LocalContext.current

    val viewModel = viewModel<ArticleScreenViewModel>()
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
    })

    val scrollState = rememberScrollState()

    //TODO: save position of last read article
//    LaunchedEffect(key1 = Unit, block = {
//        launch(Dispatchers.IO) {
//            while (true) {
//                delay(2000)
//                if (!scrollState.isScrollInProgress && viewModel.article.isInitialized){
//                    context.lastReadDataStore.edit {
//                        it[HubsDataStore.LastRead.Keys.LastArticleReadPosition] = scrollState.value
//                    }
//                }
//            }
//        }
//
//    })

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
        if (isOffline){
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

                            SelectionContainer() {
                                parseElement(
                                    element = Jsoup.parse(article.contentHtml),
                                    spanStyle = SpanStyle(
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = MaterialTheme.typography.body1.fontSize,
                                        ),
                                    onViewImageRequest = onViewImageRequest
                                ).second?.let { it1 ->
                                    it1(
                                        SpanStyle(
                                            color = MaterialTheme.colors.onSurface,
                                            fontSize = MaterialTheme.typography.body1.fontSize
                                        )
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
                Row(
                    Modifier.padding(
                        top = it.calculateTopPadding(),
                        bottom = it.calculateBottomPadding()
                    )
                ) {
                    val flingSpec = rememberSplineBasedDecay<Float>()

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
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White),
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
                                            .background(
                                                Color.White,
                                                shape = RoundedCornerShape(8.dp)
                                            )
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
                            AsyncImage(
                                article.metadata.mainImageUrl,
                                "",
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                            )
                            Spacer(Modifier.height(8.dp))
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
                                verticalAlignment = Alignment.CenterVertically,
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
                                    Spacer(modifier = Modifier.width(4.dp))
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${article.readingTime} мин",
                                    color = statisticsColor,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                if (article.translationData.isTranslation) {
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
                                }

                            }
                            Spacer(Modifier.height(4.dp))

                            HubsRow(
                                hubs = article.hubs,
                                onHubClicked = onHubClicked,
                                onCompanyClicked = onCompanyClick
                            )

                            TranslationMessage(
                                modifier = Modifier.padding(vertical = 8.dp),
                                translationInfo = article.translationData
                            ) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(article.translationData.originUrl)
                                )
                                context.startActivity(intent)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            SelectionContainer() {
                                parseElement(
                                    Jsoup.parse(
                                        article.contentHtml
                                    ),
                                    SpanStyle(
                                        color = MaterialTheme.colors.onSurface,
                                        fontSize = MaterialTheme.typography.body1.fontSize,

                                        ),
                                    onViewImageRequest = onViewImageRequest

                                ).second?.let { it1 ->
                                    it1(
                                        SpanStyle(
                                            color = MaterialTheme.colors.onSurface,
                                            fontSize = MaterialTheme.typography.body1.fontSize
                                        )
                                    )
                                }
                            }
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
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    article.hubs.forEach {
                                        HubChip(
                                            if (it.isProfiled)
                                                it.title + "*"
                                            else
                                                it.title
                                        ) {
                                            if (it.isCorporative)
                                                onCompanyClick(it.alias)
                                            else
                                                onHubClicked(it.alias)
                                        }
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



package com.garnegsoft.hubs.ui.screens.article

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.api.utils.shimmerEffect
import com.garnegsoft.hubs.ui.common.HubChip
import com.garnegsoft.hubs.ui.theme.TranslationLabelColor
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleContent(
    article: Article,
    onAuthorClicked: () -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
    onArticleClick: (id: Int) -> Unit,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    lineHeight: TextUnit,
    onViewImageRequest: (url: String) -> Unit,
    lazyListState: LazyListState
) {
    val context = LocalContext.current

    val viewModel = viewModel<ArticleScreenViewModel>()
    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val mostReadingArticles by viewModel.mostReadingArticles.observeAsState()


    Box() {
        val contentNodes by viewModel.parsedArticleContent.observeAsState()

        val color = MaterialTheme.colors.onSurface
        val spanStyle = remember(fontSize, color) {
            SpanStyle(
                //color = color,
                fontFamily = fontFamily,
                fontSize = fontSize
            )
        }
        val elementsSettings = remember {
            ElementSettings(
                fontSize = fontSize,
                lineHeight = 16.sp,
                fitScreenWidth = false
            )
        }

        val updatedPolls by viewModel.updatedPolls.observeAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(16.dp)
        ) {
            if (article.editorVersion == EditorVersion.FirstVersion) {
                item {
                    DisableSelection {
                        Row(
                            modifier = Modifier
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
                }
            }

            if (article.postType == PostType.Megaproject && article.metadata != null) {
                item {
                    var imageLoaded by rememberSaveable {
                        mutableStateOf(false)
                    }
                    AsyncImage(
                        model = article.metadata.mainImageUrl,
                        contentDescription = "",
                        modifier = Modifier
							.fillMaxWidth()
							.clip(RoundedCornerShape(8.dp))
							.shimmerEffect(!imageLoaded),
                        onSuccess = { imageLoaded = true }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (article.isCorporative) {
                item {
                    DisableSelection {

                        val company by viewModel.company.observeAsState()

                        Row(
                            modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.clickable(onClick = { onCompanyClick(company!!.alias) }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            AsyncImage(
                                modifier = Modifier
									.size(38.dp)
									.clip(RoundedCornerShape(8.dp))
									.shimmerEffect(company == null)
									.background(Color.White),
                                model = company?.avatarUrl,
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
									.shimmerEffect(
                                        enabled = company == null,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                text = company?.title ?: "", fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                            Box(modifier = Modifier.size(38.dp))


                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (article.author != null && article.postType != PostType.Megaproject) {
                item {
                    DisableSelection {

                        Row(
                            modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.clickable(onClick = { onAuthorClicked() }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var avatarLoaded by rememberSaveable {
                                mutableStateOf(false)
                            }
                            AsyncImage(
                                modifier = Modifier
									.size(38.dp)
									.clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect(!avatarLoaded)
									.background(Color.White),
                                model = article.author.avatarUrl,
                                contentDescription = "",
                                onSuccess = {
                                    avatarLoaded = true
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = article.author.alias, fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onBackground
                            )
                            Spacer(modifier = Modifier.weight(1f))

                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            item {

                Text(
                    text = article.title,
                    fontSize = (fontSize.value + 4f).sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colors.onBackground,
                    fontFamily = fontFamily
                )

                DisableSelection {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Text(
                            text = article.timePublished,
                            color = MaterialTheme.colors.onBackground.copy(0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )

                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (article.complexity != PublicationComplexity.None) {
                            Icon(
                                modifier = Modifier.size(height = 10.dp, width = 20.dp),
                                painter = painterResource(id = R.drawable.speedmeter_hard),
                                contentDescription = "",
                                tint = when (article.complexity) {
                                    PublicationComplexity.Low -> Color(0xFF4CBE51)
                                    PublicationComplexity.Medium -> Color(0xFFEEBC25)
                                    PublicationComplexity.High -> Color(0xFFEB3B2E)
                                    else -> Color.Red
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (article.complexity) {
                                    PublicationComplexity.Low -> "Простой"
                                    PublicationComplexity.Medium -> "Средний"
                                    PublicationComplexity.High -> "Сложный"
                                    else -> ""
                                },
                                color = when (article.complexity) {
                                    PublicationComplexity.Low -> Color(0xFF4CBE51)
                                    PublicationComplexity.Medium -> Color(0xFFEEBC25)
                                    PublicationComplexity.High -> Color(0xFFEB3B2E)
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
                                painter = painterResource(id = R.drawable.translation),
                                modifier = Modifier.size(14.dp),
                                contentDescription = "",
                                tint = TranslationLabelColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Перевод",
                                color = TranslationLabelColor,
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
                }
            }
            contentNodes?.let {
                items(items = it) {
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = lineHeight)) {
                        it?.invoke(spanStyle, elementsSettings)
                    }
                }

                if (article.polls.size > 0) {
                    item {
                        Divider(modifier = Modifier.padding(vertical = 24.dp))
                        ArticleTitledColumn(title = if (article.polls.size > 1) "Опросы" else "Опрос") { }
                    }
                }
                itemsIndexed(items = article.polls) { index, originalPoll ->
                    val poll =
                        updatedPolls?.find { originalPoll.id == it.id } ?: originalPoll
                    Poll(
                        poll = poll,
                        onVote = { variants ->
                            viewModel.vote(poll.id, variants)
                        },
                        onPass = {})
                    if (article.polls.lastIndex != index) {
                        Spacer(Modifier.height(48.dp))
                    }
                }
                item {
                    DisableSelection {
                        Divider(modifier = Modifier.padding(vertical = 24.dp))
                        // Hubs
                        ArticleTitledColumn(title = "Хабы") {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                article.hubs.forEach {
                                    HubChip(
                                        title = if (it.isProfiled) it.title + "*" else it.title
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

                article.author?.let { author ->
                    item {
                        Divider(modifier = Modifier.padding(vertical = 24.dp))

                        DisableSelection {
                            ArticleTitledColumn(title = "Автор") {
                                ArticleAuthorElement(
                                    onClick = { onAuthorClicked() },
                                    userAvatarUrl = author.avatarUrl!!,
                                    fullName = author.fullname,
                                    alias = author.alias
                                )
                            }
                        }
                    }
                }


                item {
                    if (viewModel.mostReadingArticles.isInitialized) {
                        Divider(modifier = Modifier.padding(vertical = 24.dp))
                        ArticleTitledColumn(
                            title = "Читают сейчас",
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var readMoreMode by remember { mutableStateOf(ReadMoreMode.MostReading) }

//                        Row(
//                            modifier = Modifier.horizontalScroll(rememberScrollState()),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.MostReading,
//                                onClick = { readMoreMode = ReadMoreMode.MostReading }) {
//                                Text(text = "Читают сейчас")
//                            }
//                            if (article.isCorporative) {
//                                HubsFilterChip(
//                                    selected = readMoreMode == ReadMoreMode.Blog,
//                                    onClick = { readMoreMode = ReadMoreMode.Blog }) {
//                                    Text(text = "Блог")
//                                }
//                            }
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.News,
//                                onClick = { readMoreMode = ReadMoreMode.News }) {
//                                Text(text = "Новости")
//                            }
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.Similar,
//                                onClick = { readMoreMode = ReadMoreMode.Similar }) {
//                                Text(text = "Похожие")
//                            }
//
//                        }


                        }
                    }
                }
                mostReadingArticles?.let { list ->

                    itemsIndexed(list) { index, it ->
                        DisableSelection {
                            Column {
                                ArticleShort(
                                    article = it,
                                    onClick = {
                                        onArticleClick(it.id)
                                    }
                                )
                                if (index != list.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        ScrollBar(modifier = Modifier.align(Alignment.CenterEnd), lazyListState = lazyListState)

    }

}

enum class ReadMoreMode {
    MostReading,
    News,
    Similar,
    Blog
}

@Composable
fun ScrollBar(
    modifier: Modifier,
    lazyListState: LazyListState,
    color: Color = MaterialTheme.colors.onBackground.copy(0.25f)
) {
    val scrollBarAlpha by animateFloatAsState(
        targetValue = if (lazyListState.isScrollInProgress) 1f else 0f,
        tween(600)
    )
    val draw by remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount > 0 } }
    if (draw) {
        Canvas(modifier = modifier
			.width(4.dp)
			.fillMaxHeight(), onDraw = {
            val topLeft = Offset(
                0f,
                (this.size.height * lazyListState.firstVisibleItemIndex / lazyListState.layoutInfo.totalItemsCount)
            )
            val barHeight =
                this.size.height.roundToInt() / (lazyListState.layoutInfo.totalItemsCount / lazyListState.layoutInfo.visibleItemsInfo.size).toFloat()
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                alpha = if (lazyListState.isScrollInProgress) 1f else scrollBarAlpha,
                size = Size(width = 3f * density, height = barHeight),
                cornerRadius = CornerRadius(400f, 400f)
            )

        })
    }
}

//object : FlingBehavior {
//    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
//        if (abs(initialVelocity) <= 1f)
//            return initialVelocity
//
//        val performedInitialVelocity = initialVelocity * 1.2f
//
//        var velocityLeft = performedInitialVelocity
//        var lastValue = 0f
//        AnimationState(
//            initialValue = 0f,
//            initialVelocity = performedInitialVelocity
//        ).animateDecay(splineBasedDecay(Density(3f))) {
//            val delta = value - lastValue
//            val consumed = scrollBy(delta)
//            lastValue = value
//            velocityLeft = velocity
//
//            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
//
//        }
//        return velocityLeft
//
//    }
//
//}
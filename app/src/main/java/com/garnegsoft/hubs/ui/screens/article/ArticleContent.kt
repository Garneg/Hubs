package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.settingsDataStoreFlowWithDefault
import com.garnegsoft.hubs.settingsDataStoreFlow
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.user.HubChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleContent(
    article: Article,
    onAuthorClicked: () -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
    onArticleClick: (id: Int) -> Unit,
    onViewImageRequest: (url: String) -> Unit
) {
    val context = LocalContext.current

    val viewModel = viewModel<ArticleScreenViewModel>()
    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val mostReadingArticles by viewModel.mostReadingArticles.observeAsState()

    Row {
        val flingSpec = rememberSplineBasedDecay<Float>()
        val contentNodes by viewModel.parsedArticleContent.observeAsState()
        val fontSize by context.settingsDataStoreFlowWithDefault(HubsDataStore.Settings.Keys.ArticleScreen.FontSize, MaterialTheme.typography.body1.fontSize.value).collectAsState(
            initial = null
        )
        val lineHeightFactor by context.settingsDataStoreFlowWithDefault(HubsDataStore.Settings.Keys.ArticleScreen.LineHeightFactor, 1.5f).collectAsState(
            initial = null
        )
        val color = MaterialTheme.colors.onSurface
        val spanStyle = remember(fontSize, color) {
            SpanStyle(
                color = color,
                fontSize = fontSize?.sp ?: 16.sp
            )
        }
        val elementsSettings = remember {
            ElementSettings(
                fontSize = fontSize?.sp ?: 16.sp,
                lineHeight = 16.sp,
                fitScreenWidth = false
            )
        }

        val state = rememberLazyListState()
        val updatedPolls by viewModel.updatedPolls.observeAsState()

        LaunchedEffect(key1 = fontSize, block = {
            if (!viewModel.parsedArticleContent.isInitialized && fontSize != null) {
                val element =
                    Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()!!.child(0)
                        ?: Element("")
                viewModel.parsedArticleContent.postValue(parseChildElements(element, spanStyle, onViewImageRequest).second)
            }
        })
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            state = state,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (article.editorVersion == EditorVersion.FirstVersion) {
                item {
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
            if (article.author != null) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = { onAuthorClicked() }),
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
            }
            if (article.postType == PostType.Megaproject && article.metadata != null) {
                item {
                    AsyncImage(
                        article.metadata.mainImageUrl,
                        "",
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = article.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colors.onBackground
                )
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

            }
            contentNodes?.let {
                items(items = it) {
                    it?.invoke(spanStyle, elementsSettings)
                }
            }

            items(items = article.polls) {originalPoll ->
                val poll =
                    updatedPolls?.find { originalPoll.id == it.id} ?: originalPoll
                Poll(
                    poll = poll,
                    onVote = { variants ->
                        viewModel.vote(poll.id, variants)
                    },
                    onPass = {})
            }
            item {
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


            item {
                if (viewModel.mostReadingArticles.isInitialized) {
                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                    TitledColumn(
                        title = "Читают сейчас",
                        titleStyle = MaterialTheme.typography.subtitle2.copy(
                            color = MaterialTheme.colors.onBackground.copy(
                                0.5f
                            )
                        ),
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
            mostReadingArticles?.let {
                items(it) {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onArticleClick(it.id) }
                        .padding(bottom = 8.dp)
                        .padding(8.dp)
                    ) {
                        ArticleShort(article = it)
                    }
                }
            }

        }
        val density = LocalDensity.current
        Canvas(modifier = Modifier
            .width(4.dp)
            .fillMaxHeight(), onDraw = {
            val topLeft = Offset(
                0f,
                (this.size.height * state.firstVisibleItemIndex / state.layoutInfo.totalItemsCount)
            )
            val barHeight =
                this.size.height.roundToInt() / (state.layoutInfo.totalItemsCount / state.layoutInfo.visibleItemsInfo.size).toFloat()
            drawRoundRect(
                color = Color.LightGray,
                topLeft = topLeft,
                size = Size(width = 3f * density.density, height = barHeight),
                cornerRadius = CornerRadius(400f, 400f)
            )

        })
    }

}

enum class ReadMoreMode {
    MostReading,
    News,
    Similar,
    Blog
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
//        ).animateDecay(flingSpec) {
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
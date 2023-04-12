package com.garnegsoft.hubs.ui.screens.article

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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    var article = MutableLiveData<Article>()

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(
    article: Article,
    onBackButtonClicked: () -> Unit,
    onCommentsClicked: () -> Unit,
    onAuthorClicked: () -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
) {
    val scrollState = rememberScrollState()
    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    val shareIntent = remember {
        val sendIntent = Intent(Intent.ACTION_SEND)

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${article.title} — https://habr.com/p/${article.id}/"
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
                        onClick = { context.startActivity(shareIntent) }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "")
                    }
                })
        },
        backgroundColor = if (MaterialTheme.colors.isLight == true) Color(0xFFF9F9F9) else MaterialTheme.colors.background,
        bottomBar = {

            BottomAppBar(
                elevation = 0.dp,
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier
                    .height(60.dp)
                    .drawBehind {
                        drawLine(
                            color = Color.LightGray, start = Offset(0f, 0f),
                            end = Offset(size.width, 0f), strokeWidth = 2f
                        )
                    }
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
        }
    ) {
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
                            .clickable(onClick = onAuthorClicked),
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
                            fontSize = 14.sp
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
                            fontWeight = FontWeight.W800
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
                            val hubTitle = (if (it.isProfiled) it.title + "*" else it.title) + ", "

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
                            it1(SpanStyle(
                                color = MaterialTheme.colors.onSurface,
                                fontSize = MaterialTheme.typography.body1.fontSize
                            ))
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
                                    modifier = Modifier.clip(
                                        RoundedCornerShape(4.dp)).clickable { onHubClicked(it.alias) },
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

val STRONG_FONT_WEIGHT = FontWeight.W600
val HEADER_FONT_WEIGHT = FontWeight.W700

fun parseElement(
    element: Element,
    spanStyle: SpanStyle
): Pair<AnnotatedString?, (@Composable (SpanStyle) -> Unit)?> {
    var isBlock = element.isHabrBlock()
    var resultAnnotatedString: AnnotatedString = buildAnnotatedString { }
    var ChildrenSpanStyle = spanStyle

    // Applying Inline elements style
    when (element.tagName()) {
        "del" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(
            textDecoration = TextDecoration.combine(
                listOf(
                    ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                    TextDecoration.LineThrough
                )
            )
        )
        "b" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontWeight = STRONG_FONT_WEIGHT)
        "strong" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontWeight = STRONG_FONT_WEIGHT)
        "i" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontStyle = FontStyle.Italic)
        "em" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(fontStyle = FontStyle.Italic)
            if (element.hasClass("searched-item")) {
                ChildrenSpanStyle = ChildrenSpanStyle.copy(background = Color(101, 238, 255, 76))
            }
        }
        "code" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontFamily = FontFamily.Monospace,
                background = Color(138, 156, 165, 20)
            )
        }
        "u" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                textDecoration = TextDecoration.combine(
                    listOf(
                        ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                        TextDecoration.Underline
                    )
                )
            )
        }
        "s" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                textDecoration = TextDecoration.combine(
                    listOf(
                        ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                        TextDecoration.LineThrough
                    )
                )
            )
        }
        "sup" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                baselineShift = BaselineShift.Superscript,
                fontSize = (ChildrenSpanStyle.fontSize.value - 4).coerceAtLeast(1f).sp
            )
        }
        "sub" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                baselineShift = BaselineShift.Subscript,
                fontSize = (ChildrenSpanStyle.fontSize.value - 4).coerceAtLeast(1f).sp
            )
        }

        "br" -> {
            return buildAnnotatedString { append("\n") } to null
        }
        "a" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(color = Color(88, 132, 185, 255))
            if (element.hasClass("user_link")) {
                resultAnnotatedString = buildAnnotatedString {
                    withStyle(ChildrenSpanStyle) { append("@") }
                }
            }
        }
        "h1" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 4f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h2" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 3f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h3" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 2f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h4" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 2f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h5" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 1f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h6" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "figcaption" -> {
            ChildrenSpanStyle =
                ChildrenSpanStyle.copy(
                    color = Color.Gray,
                    fontSize = (ChildrenSpanStyle.fontSize.value - 4).coerceAtLeast(4f).sp
                )

        }
        "img" -> {
            if (element.attr("inline") == "true") {
                resultAnnotatedString = buildAnnotatedString {
                    appendInlineContent("inlineImage_")
                }
            }
        }
        "summary" -> return buildAnnotatedString { } to null
    }

    // Child elements parsing and styling
    var childrenElementsResult: ArrayList<Pair<AnnotatedString?, (@Composable (SpanStyle) -> Unit)?>> =
        ArrayList()
    element.children().forEach {
        childrenElementsResult.add(parseElement(it, ChildrenSpanStyle))
    }
    var mainComposable: (@Composable (SpanStyle) -> Unit)? = null

    var childrenComposables: ArrayList<@Composable (SpanStyle) -> Unit> = ArrayList()


    // Text parsing and styling + validating children element
    var currentText = buildAnnotatedString { }
    var childElementsIndex = 0

    element.childNodes().forEach { thisNode ->
        if (thisNode is TextNode) {
            if (!thisNode.isBlank)
                currentText +=
                    buildAnnotatedString {
                        withStyle(ChildrenSpanStyle) {
                            append(
                                if (thisNode.previousSibling() == null ||
                                    thisNode.previousSibling() is Element &&
                                    (thisNode.previousSibling() as Element)?.tagName() == "br"
                                )
                                    thisNode.text().trimStart()
                                else
                                    thisNode.text()
                            )
                        }
                    }
        }
        if (thisNode is Element) {

            if (childrenElementsResult[childElementsIndex].first != null)
                currentText += childrenElementsResult[childElementsIndex].first!!


            if (childrenElementsResult[childElementsIndex].second != null) {
                if (currentText.isNotEmpty() && thisNode.previousElementSibling() != null
                    && thisNode.previousElementSibling()!!.tagName() != "pre"
                ) {
                    var thisElementCurrentText = currentText
                    childrenComposables.add {
                        //Text(text = thisElementCurrentText)
                        var context = LocalContext.current
                        ClickableText(
                            text = thisElementCurrentText,
                            onClick = {
                                thisElementCurrentText.getStringAnnotations(it, it)
                                    .find { it.tag == "url" }
                                    ?.let {
                                        if (it.item.startsWith("http")) {
                                            Log.e(
                                                "URL Clicked",
                                                it.item
                                            )
                                            var intent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                                            context.startActivity(intent)
                                        }
                                    }
                            })
                    }
                }
                childrenComposables.add(childrenElementsResult[childElementsIndex].second!!)
                currentText = buildAnnotatedString { }
            }

            if (thisNode.previousSibling() != null && thisNode.previousSibling() is TextNode) {

            }

            // if node is block element, break the currentText annotated string and place Text() Composable
            childElementsIndex++
        }


    }
    if (!currentText.text.isBlank() && !isBlock) {
        if (element.tagName() == "a") {
            resultAnnotatedString += buildAnnotatedString {
                var urlAnnotationId = pushStringAnnotation("url", element.attr("href"))
                append(currentText)
                pop(urlAnnotationId)
            }
        } else
            resultAnnotatedString += currentText

    }
    if (!currentText.text.isBlank() && isBlock)
        childrenComposables.add {
            //Text(text = currentText)
            val context = LocalContext.current
            ClickableText(
                text = currentText,
                onClick = {
                    currentText.getStringAnnotations(it, it).find { it.tag == "url" }?.let {
                        if (it.item.startsWith("http")) {
                            Log.e(
                                "URL Clicked",
                                it.item
                            )
                            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                            context.startActivity(intent)
                        }
                    }
                })
        }

    // Fetching composable
    mainComposable = when (element.tagName()) {
        "h2" -> { localSpanStyle ->
            Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
                childrenComposables.forEach { it(localSpanStyle) }
            }
        }
        "h3" -> { localSpanStyle ->
            Column(Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                childrenComposables.forEach { it(localSpanStyle) }
            }
        }
        "h4" -> { localSpanStyle ->
            Column(Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                childrenComposables.forEach { it(localSpanStyle) }
            }
        }
        "h5" -> { localSpanStyle ->
            Column(Modifier.padding(top = 4.dp, bottom = 3.dp)) {
                childrenComposables.forEach { it(localSpanStyle) }
            }
        }
        "p" -> if (element.html().isNotEmpty()) { localSpanStyle ->
            Column(Modifier.padding(bottom = 16.dp)) {
                childrenComposables.forEach {
                    it(localSpanStyle)
                }
            }
        }
        else
            null
        "figcaption" -> if (element.text().isNotEmpty())
            { localSpanStyle ->
                Column(Modifier.padding(bottom = 12.dp)) {
                    childrenComposables.forEach { it(localSpanStyle) }
                }
            }
        else null
        "img" -> { it: SpanStyle ->
            AsyncGifImage(
                model = if (element.hasAttr("data-src")) {
                    element.attr("data-src")
                } else {
                    element.attr("src")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        "div" -> if (element.hasClass("tm-iframe_temp"))
            { localSpanStyle ->

                AndroidView(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp)),
                    factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true
                            settings.databaseEnabled = true
                            isFocusable = true
                            isLongClickable = true
                            loadUrl(element.attr("data-src"))
                        }
                    })
            }
        else
            { localSpanStyle ->
                Column() {
                    //Text(text = element.ownText())
                    childrenComposables.forEach {
                        it(localSpanStyle)
                    }
                }
            }

        "code" -> if (element.parent() != null && element.parent()!!
                .tagName() == "pre"
        ) { localSpanStyle ->
            Box(Modifier.padding(bottom = 4.dp)) {
                Code(
                    code = element.text(),
                    language = LanguagesMap.getOrElse(
                        element.attr("class"),
                        { element.attr("class") })
                )
            }
            resultAnnotatedString = buildAnnotatedString { }
        } else
            null


        "ul" ->
            if (element.parent() != null && element.parent()!!.tagName() == "li")
                { localSpanStyle ->
                    TextList(
                        modifier = Modifier.padding(bottom = 8.dp),
                        items = childrenComposables,
                        spanStyle = localSpanStyle,
                        ordered = false,
                        nested = true
                    )
                }
            else
                { localSpanStyle ->
                    TextList(
                        modifier = Modifier.padding(bottom = 8.dp),
                        items = childrenComposables, spanStyle = localSpanStyle, ordered = false
                    )
                }

        "ol" -> if (element.hasAttr("start"))
            { localSpanStyle ->
                TextList(
                    modifier = Modifier.padding(bottom = 8.dp),
                    items = childrenComposables,
                    spanStyle = localSpanStyle,
                    ordered = true,
                    startNumber = element.attr("start").toIntOrNull() ?: 1
                )
            }
        else
            { localSpanStyle ->
                TextList(
                    modifier = Modifier.padding(bottom = 8.dp),
                    items = childrenComposables, spanStyle = localSpanStyle, ordered = true
                )
            }

        "blockquote" -> { localSpanStyle ->
            val quoteWidth = with(LocalDensity.current) { 4.dp.toPx() }
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                val blockQuoteColor = if (MaterialTheme.colors.isLight) SecondaryColor else MaterialTheme.colors.onBackground
                Column(modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = blockQuoteColor,
                            size = Size(quoteWidth, size.height),
                            cornerRadius = CornerRadius(quoteWidth / 2, quoteWidth / 2)
                        )

                    }
                    .padding(start = 12.dp)) {
                    childrenComposables.forEach { it(localSpanStyle.copy(fontStyle = FontStyle.Italic)) }
                }
            }

        }

        "hr" -> { localSpanStyle ->
            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )

        }

        "details" -> { localSpanStyle ->
            var spoilerCaption = element.getElementsByTag("summary").first()?.text() ?: "Спойлер"
            var showDetails by rememberSaveable { mutableStateOf(false) }
            Surface(
                color = Color(0x65EBEBEB),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Column(
                    modifier = Modifier
                        .animateContentSize()

                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDetails = !showDetails }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            tint = Color(0xFF5587A3),
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(
                                    if (!showDetails) {
                                        -90f
                                    } else {
                                        0f
                                    }
                                ),
                            imageVector = Icons.Outlined.ArrowDropDown, contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = spoilerCaption, color = Color(0xFF5587A3))
                    }
                    if (showDetails) {
                        Divider()
                        Column(
                            modifier = Modifier.padding(
                                start = 12.dp,
                                end = 12.dp,
                                bottom = 8.dp,
                                top = 8.dp
                            )
                        ) {
                            childrenComposables.forEach { it(localSpanStyle) }
                        }
                    }
                }
            }

        }

        else -> if (childrenComposables.size == 0) {
            null
        } else {
            { localSpanStyle ->
                Column() {
                    childrenComposables.forEach { it(localSpanStyle) }
                }
            }
        }
    }
    return resultAnnotatedString to mainComposable
}


val LanguagesMap = mapOf<String, String>(
    "" to "Язык неизвестен",
    "plaintext" to "Текст",
    "1c" to "1C",
    "assembly" to "Assembly",
    "kotlin" to "Kotlin",
    "java" to "Java",
    "javascript" to "JavaScript",
    "json" to "JSON",
    "html" to "HTML",
    "r" to "R",
    "ruby" to "Ruby",
    "rust" to "Rust",
    "bash" to "BASH",
    "cpp" to "C++",
    "cs" to "C#",
    "css" to "CSS",
    "cmake" to "CMake",
    "python" to "Python",
    "php" to "PHP",
    "perl" to "Perl",
    "swift" to "Swift",
    "sql" to "SQL",
    "scala" to "Scala",
    "markdown" to "Markdown",
    "objectivec" to "Objective C",
    "xml" to "XML",
    "go" to "Go",
    "typescript" to "TypeScript",
    "yaml" to "YAML",
    "dart" to "Dart",
    "lua" to "Lua",
    "lisp" to "Lisp",
    "vhdl" to "VHDL",
    "fs" to "F#"
)


// may be redundant
fun Element.isHabrBlock(): Boolean {
    val blocks = arrayListOf(
        "h1", "h2", "h3", "h4", "h5", "h6",
        "p", "div", "img", "table", "iframe",
        "li", "ul", "ol", "figcaption", "blockquote",
        "hr"
    )

    blocks.forEach {
        if (tagName() == it) return true
    }
    return false
}

@Composable
fun TextList(
    modifier: Modifier = Modifier,
    items: List<@Composable (SpanStyle) -> Unit>,
    spanStyle: SpanStyle,
    ordered: Boolean,
    nested: Boolean = false,
    startNumber: Int = 1
) {
    var itemNumber = startNumber
    Column(modifier = modifier) {
        items.forEach {

            Row() {
                DisableSelection {
                    if (ordered) {
                        Text(buildAnnotatedString { withStyle(spanStyle) { append("$itemNumber.") } })
                    } else
                        if (nested) {
                            Text(text = "◦", fontSize = spanStyle.fontSize)
                        } else {
                            Text(text = "•", fontSize = spanStyle.fontSize)
                        }
                }
                Spacer(modifier = Modifier.width(4.dp))
                it(spanStyle)
            }
            itemNumber++
        }
    }
}


@Composable
fun Code(code: String, language: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .border(0.4.dp, MaterialTheme.colors.onBackground.copy(0.1f), shape = RoundedCornerShape(4.dp))
    ) {
        Surface(
            color = MaterialTheme.colors.onBackground.copy(0.05f),
            border = BorderStroke(.4.dp, MaterialTheme.colors.onBackground.copy(0.1f)),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = language,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.W600,
                fontFamily = FontFamily.SansSerif
            )
        }
        Surface(
            color = MaterialTheme.colors.onBackground.copy(0.06f),
            border = BorderStroke(.4.dp, MaterialTheme.colors.onBackground.copy(0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row() {
                Surface(
                    color = MaterialTheme.colors.onBackground.copy(0.02f),
                    border = BorderStroke(.4.dp, MaterialTheme.colors.onBackground.copy(0.1f))
                ) {
                    Column(Modifier.padding(8.dp)) {
                        var linesIndicator = String()
                        for (i in 1..code.count { it == "\n"[0] } + 1) {
                            linesIndicator += "$i\n"
                        }
                        linesIndicator = linesIndicator.take(linesIndicator.length - 1)

                        DisableSelection {
                            Text(
                                text = linesIndicator, fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = code,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.widthIn(0.dp, 20000.dp)
                    )
                }
            }
        }
    }
}

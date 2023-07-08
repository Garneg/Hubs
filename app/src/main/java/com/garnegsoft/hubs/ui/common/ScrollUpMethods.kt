package com.garnegsoft.hubs.ui.common

import ArticleController
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.ui.screens.article.parseChildElements
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements


sealed interface ScrollUpMethods {
    companion object {
        suspend fun scrollLazyList(lazyListState: LazyListState){
            lazyListState.scrollToItem(
                0,
                lazyListState.firstVisibleItemScrollOffset
            )
            lazyListState.animateScrollToItem(0)
        }

        suspend fun scrollNormalList(scrollState: ScrollState) {
            scrollState.scrollTo(0)
        }
    }
}

@Preview
@Composable
fun LazyListArticleTest() {
    HubsTheme {

        var article: Article? by remember { mutableStateOf(null) }
        var contentNodes: List<(@Composable (SpanStyle) -> Unit)?> by remember { mutableStateOf(emptyList()) }
        val spanStyle = SpanStyle(
            color = MaterialTheme.colors.onSurface,
            fontSize = MaterialTheme.typography.body1.fontSize
        )
        val context = LocalContext.current
        var elements: Elements? by remember { mutableStateOf(null) }
        val state = rememberLazyListState()

        LaunchedEffect(key1 = Unit, block = {
            launch(Dispatchers.IO) {
                HabrApi.initialize(context)
                article = ArticleController.get(708328)
                elements = Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()!!.child(0).children()
                contentNodes = parseChildElements(Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()!!.child(0), spanStyle).second
                withContext(Dispatchers.Main) {
                    state.animateScrollToItem(3+elements!!.indexOfFirst { it.hasClass("anchor") && it.attr("name") == "Nash-opyt-vnedrenija-KMM-v-sushhestvujushhie-proekty" })
                }
            }
        })

        SelectionContainer {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    CircularProgressIndicator()
                }
                item {
                    Column {
                        Text(text = "${elements?.size}_____${contentNodes.size}")
                    }
                }
                itemsIndexed(contentNodes) {index, it ->
//                    Column {
//                        Text(text = "$index) ${elements!![index].tagName()}", maxLines = 1)
//
//                    }
                    it?.invoke(spanStyle)

                }
            }
        }
    }
}
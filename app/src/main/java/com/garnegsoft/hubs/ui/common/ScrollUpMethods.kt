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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements


sealed interface ScrollUpMethods {
    companion object {
        suspend fun scrollLazyList(lazyListState: LazyListState){
            if (lazyListState.firstVisibleItemIndex > 3) {
                lazyListState.scrollToItem(
                    2,
                    lazyListState.firstVisibleItemScrollOffset
                )
            }
            lazyListState.animateScrollToItem(0)
        }

        suspend fun scrollNormalList(scrollState: ScrollState) {
            scrollState.animateScrollTo(0)
        }
    }
}



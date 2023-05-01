package com.garnegsoft.hubs.ui.screens.comments

import ArticleController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.parseElement
import com.garnegsoft.hubs.ui.theme.PrimaryColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


class CommentsScreenViewModel : ViewModel() {
    var parentPostSnippet = MutableLiveData<ArticleSnippet>()
    var comments = MutableLiveData<ArrayList<Comment>>()


}


@Composable
fun CommentsScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    parentPostId: Int,
    commentId: Int?,
    showArticleSnippet: Boolean = true,
    onBackClicked: () -> Unit,
    onArticleClicked: () -> Unit,
    onUserClicked: (alias: String) -> Unit,
) {
    var viewModel = viewModel<CommentsScreenViewModel>(viewModelStoreOwner)

    val comments = viewModel.comments.observeAsState().value
    val articleSnippet = viewModel.parentPostSnippet.observeAsState().value

    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        launch(Dispatchers.IO) {
            viewModel.comments.postValue(CommentsListController.getComments("articles/$parentPostId/comments"))
            viewModel.parentPostSnippet.postValue(ArticleController.getSnippet("articles/$parentPostId"))
        }
    }

    LaunchedEffect(key1 = comments, block = {
        commentId?.let { commId ->
            if (viewModel.comments.isInitialized){
                comments?.indexOf(comments.find { it.id == commId })?.let {
                    if (it > -1)
                        lazyListState.animateScrollToItem(it)
                }
            }
        }
    })

    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                title = { Text("Комментарии") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.padding(it),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showArticleSnippet && articleSnippet != null) {
                item {
                    ArticleCard(
                        article = articleSnippet,
                        onClick = onArticleClicked,
                        style = defaultArticleCardStyle().copy(
                            showImage = false,
                            showTextSnippet = false
                        ),
                        onAuthorClick = { onUserClicked(articleSnippet.author!!.alias) },
                        onCommentsClick = {}
                    )
                }
            }
            if (comments != null) {
                items(
                    count = comments.size,
                    key = { comments[it].id },
                ) {
                    val comment = comments[it]
                    CommentItem(
                        modifier = Modifier
                            .padding(
                                start = 16.dp.times(comment.level.coerceAtMost(5))
                            ),
                        comment = comment,
                        onAuthorClick = {
                            onUserClicked(comment.author.alias)
                        },
                        highlight = comment.id == commentId
                    ) {
                        Column {
                            comment.let {
                                ((parseElement(it.message, SpanStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colors.onSurface)).second)?.let { it1 -> it1(SpanStyle(color = MaterialTheme.colors.onSurface)) })
                            }

                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

    }
}



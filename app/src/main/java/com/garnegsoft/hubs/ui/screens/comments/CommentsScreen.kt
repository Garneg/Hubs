package com.garnegsoft.hubs.ui.screens.comments

import ArticleController
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.ArticleComments
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.parseElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CommentsScreenViewModel : ViewModel() {
    var parentPostSnippet = MutableLiveData<ArticleSnippet>()
    var commentsData = MutableLiveData<ArticleComments?>()

    fun comment(text: String, postId: Int, parentCommentId: Int? = null){
        viewModelScope.launch(Dispatchers.IO) {
            val newAccess = CommentsListController.sendComment(
                articleId = postId,
                text = text,
                parentCommentId = parentCommentId
            )

            CommentsListController.getComments(postId)?.let {
                commentsData.postValue(newAccess?.let { it1 -> ArticleComments(it.comments, it1) })
            }
        }



    }
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

    val commentsData = viewModel.commentsData.observeAsState().value
    val articleSnippet = viewModel.parentPostSnippet.observeAsState().value

    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = Unit) {
        launch(Dispatchers.IO) {
            if (showArticleSnippet)
                viewModel.parentPostSnippet.postValue(ArticleController.getSnippet("articles/$parentPostId"))
            viewModel.commentsData.postValue(CommentsListController.getComments("articles/$parentPostId/comments"))
        }
    }

    LaunchedEffect(key1 = commentsData, block = {
        commentId?.let { commId ->
            if (viewModel.commentsData.isInitialized){
                commentsData?.comments?.indexOf(commentsData.comments.find { it.id == commId })?.let {
                    if (it > -1)
                        if (showArticleSnippet)
                            lazyListState.animateScrollToItem(it + 1)
                        else
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
        Column(modifier = Modifier
            .padding(it)
            .imePadding()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (articleSnippet != null) {
                    item {
                        ArticleCard(
                            article = articleSnippet,
                            onClick = onArticleClicked,
                            style = defaultArticleCardStyle().copy(
                                showImage = false,
                                showTextSnippet = false,
                                addToBookmarksButtonEnabled = articleSnippet.relatedData != null
                            ),
                            onAuthorClick = { onUserClicked(articleSnippet.author!!.alias) },
                            onCommentsClick = {}
                        )
                    }
                }
                if (commentsData != null) {
                    items(
                        count = commentsData.comments.size,
                        key = { commentsData.comments[it].id },
                    ) {
                        val comment = commentsData.comments[it]
                        val context = LocalContext.current
                        CommentItem(
                            modifier = Modifier
                                .padding(
                                    start = 16.dp.times(comment.level.coerceAtMost(5))
                                ),
                            comment = comment,
                            onAuthorClick = {
                                onUserClicked(comment.author.alias)
                            },
                            highlight = comment.id == commentId,
                            onShare = {
                                val intent = Intent(Intent.ACTION_SEND)
                                intent.putExtra(Intent.EXTRA_TEXT, "https://habr.com/p/${parentPostId}/comments/#comment_${comment.id}")
                                intent.setType("text/plain")
                                context.startActivity(Intent.createChooser(intent, null))
                            }
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
            if (commentsData?.commentAccess?.canComment == true) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    var commentTextFieldValue by remember { mutableStateOf(TextFieldValue()) }
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Выскажите своё мнение [Markdown]",
                                style = MaterialTheme.typography.body1
                            )
                        },
                        value = commentTextFieldValue,
                        onValueChange = {
                            commentTextFieldValue = it

                        }
                    )
                    IconButton(onClick = {
                        viewModel.comment(commentTextFieldValue.text, parentPostId) }
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "send comment")
                    }
                }

            }

        }


    }
}




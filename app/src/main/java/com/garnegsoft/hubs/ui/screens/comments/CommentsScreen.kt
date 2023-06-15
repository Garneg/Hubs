package com.garnegsoft.hubs.ui.screens.comments

import ArticleController
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.ArticleComments
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.parseElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import kotlin.math.roundToInt


class CommentsScreenViewModel : ViewModel() {
    var parentPostSnippet = MutableLiveData<ArticleSnippet>()
    var commentsData = MutableLiveData<ArticleComments?>()

    fun comment(text: String, postId: Int, parentCommentId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAccess = CommentsListController.sendComment(
                articleId = postId,
                text = text,
                parentCommentId = parentCommentId
            )

            CommentsListController.getComments(postId)?.let {
                commentsData.postValue(newAccess?.let { it1 -> ArticleComments(it.comments, it1) }
                    ?: it)
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

    var commentsById = remember(commentsData?.comments) {

        val map = hashMapOf<Int, Comment>()
        if (commentsData?.comments != null) {
            commentsData?.comments?.forEach {
                map.put(it.id, it)
            }
        }
        map
    }

    LaunchedEffect(key1 = Unit) {
        if (!viewModel.commentsData.isInitialized) {
            launch(Dispatchers.IO) {
                if (showArticleSnippet)
                    viewModel.parentPostSnippet.postValue(ArticleController.getSnippet("articles/$parentPostId"))
                viewModel.commentsData.postValue(CommentsListController.getComments("articles/$parentPostId/comments"))
            }
        }
    }

    LaunchedEffect(key1 = commentsData, block = {
        commentId?.let { commId ->
            if (viewModel.commentsData.isInitialized) {
                commentsData?.comments?.indexOf(commentsData.comments.find { it.id == commId })
                    ?.let {
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
        var parentCommentId by rememberSaveable {
            mutableStateOf(0)
        }
        val showArticleHeader by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
        val commentTextFieldFocusRequester = remember { FocusRequester() }
        val randomCoroutineScope = rememberCoroutineScope()
        var articleHeaderOffset by remember { mutableStateOf(0f) }
        Box {
            Column(
                modifier = Modifier
                    .padding(it)
                    .imePadding()
            ) {
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
                            items = commentsData.comments,
                            key = { it.id }
                        ) {
                            val comment = it
                            val context = LocalContext.current

                            CommentItem(
                                modifier = Modifier
                                    .padding(
                                        start = 20.dp.times(comment.level.coerceAtMost(5))
                                    ),
                                comment = comment,
                                onAuthorClick = {
                                    onUserClicked(comment.author.alias)
                                },
                                highlight = comment.id == commentId,
                                parentComment = commentsById.get(comment.parentCommentId),
                                showReplyButton = viewModel.commentsData.value?.commentAccess?.canComment
                                    ?: false,
                                onShare = {
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.putExtra(
                                        Intent.EXTRA_TEXT,
                                        "https://habr.com/p/${parentPostId}/comments/#comment_${comment.id}"
                                    )
                                    intent.setType("text/plain")
                                    context.startActivity(Intent.createChooser(intent, null))
                                },
                                onReplyClick = {
                                    commentTextFieldFocusRequester.requestFocus()
                                    parentCommentId = comment.id
                                },
                                onParentCommentSnippetClick = {
                                    randomCoroutineScope.launch {
                                        lazyListState.animateScrollToItem(
                                            index = commentsData.comments.indexOfFirst { it.id == comment.parentCommentId } + 1,
                                            scrollOffset = -articleHeaderOffset.roundToInt()
                                        )

                                    }
                                }
                            ) {

                                Column {
                                    comment.let {
                                        SelectionContainer {
                                            ((parseElement(
                                                it.message, SpanStyle(
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colors.onSurface
                                                )
                                            ).second)?.let { it1 -> it1(SpanStyle(color = MaterialTheme.colors.onSurface)) })
                                        }
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
                    Column(modifier = Modifier) {
                        AnimatedVisibility(
                            visible = parentCommentId > 0,
                            enter = expandVertically(expandFrom = Alignment.Bottom),
                            exit = shrinkVertically(shrinkTowards = Alignment.Bottom)

                        ) {
                            Column() {

                                val comment =
                                    viewModel.commentsData.value?.comments?.find { it.id == parentCommentId }
                                Divider()
                                val coroutineScope = rememberCoroutineScope()
                                Row(modifier = Modifier
                                    .clickable {
                                        val index =
                                            viewModel.commentsData.value?.comments?.indexOf(
                                                comment
                                            )
                                                ?: 0
                                        coroutineScope.launch {
                                            lazyListState.animateScrollToItem(
                                                index + 1
                                            )
                                        }
                                    }
                                    .background(MaterialTheme.colors.surface)
                                    .padding(4.dp)
                                    .padding(start = 4.dp)
                                    .height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Spacer(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colors.secondary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = comment?.author?.alias ?: "",
                                            fontWeight = FontWeight.W500,
                                            color = MaterialTheme.colors.primary.copy(0.9f)
                                        )
                                        val text = comment?.message ?: ""
                                        Text(
                                            maxLines = 1,
                                            text = Jsoup.parse(text).text(),
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.body2,
                                            color = MaterialTheme.colors.onSurface.copy(0.6f)
                                        )

                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { parentCommentId = 0 }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "",
                                            tint = MaterialTheme.colors.secondary
                                        )

                                    }
                                }
                            }
                        }
                        Divider()
                        Row(
                            verticalAlignment = Alignment.Bottom, modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surface)
                                .padding(4.dp)
                        ) {
                            var commentTextFieldValue by remember { mutableStateOf(TextFieldValue()) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                BasicTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .heightIn(max = 140.dp)
                                        .focusRequester(commentTextFieldFocusRequester),
                                    value = commentTextFieldValue,
                                    onValueChange = {
                                        commentTextFieldValue = it

                                    },
                                    cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                                    textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface)
                                )
                                if (commentTextFieldValue.text.isEmpty()) {
                                    Row(modifier = Modifier.align(Alignment.CenterStart)) {
                                        Text(
                                            text = "Комментарий",
                                            color = MaterialTheme.colors.onSurface.copy(0.4f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            tint = MaterialTheme.colors.onSurface.copy(0.4f),
                                            painter = painterResource(id = R.drawable.markdown),
                                            contentDescription = ""
                                        )
                                    }

                                }
                            }

                            IconButton(
                                enabled = commentTextFieldValue.text.isNotBlank(),
                                onClick = {
                                    viewModel.comment(
                                        text = commentTextFieldValue.text,
                                        postId = parentPostId,
                                        parentCommentId = if (parentCommentId > 0) parentCommentId else null
                                    )
                                    commentTextFieldValue = TextFieldValue()
                                    parentCommentId = 0
                                    commentTextFieldFocusRequester.freeFocus()
                                }
                            ) {
                                val iconTint by animateColorAsState(
                                    targetValue = if (commentTextFieldValue.text.isNotBlank()) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(
                                        0.4f
                                    )
                                )
                                Icon(
                                    tint = iconTint,
                                    painter = painterResource(id = R.drawable.send),
                                    contentDescription = "send comment"
                                )
                            }
                        }
                    }
                }

            }
            if (showArticleHeader) {
                articleSnippet?.let {
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    randomCoroutineScope.launch {
                                        lazyListState.animateScrollToItem(0)
                                    }
                                }
                                .onGloballyPositioned {
                                    articleHeaderOffset = it.boundsInRoot().height
                                }
                                .background(MaterialTheme.colors.surface)
                                .fillMaxWidth()
//                    .height(50.dp)
                                .height(IntrinsicSize.Min)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            it.imageUrl?.let {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp)),
                                    model = articleSnippet?.imageUrl, contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(

                            ) {
                                it.author?.run {
                                    Text(
                                        text = alias,
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.W500,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = articleSnippet?.title!!,
                                    style = MaterialTheme.typography.body2,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Divider(modifier = Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }

}




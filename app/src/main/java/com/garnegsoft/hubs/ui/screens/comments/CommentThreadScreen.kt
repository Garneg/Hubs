package com.garnegsoft.hubs.ui.screens.comments

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.data.comment.Comment
import com.garnegsoft.hubs.data.comment.CommentsCollection
import com.garnegsoft.hubs.data.comment.list.CommentsListController
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.parseElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


class CommentThreadScreenViewModel : ViewModel() {
	val comments = MutableLiveData<CommentsCollection>()
	
	fun comment(text: String, postId: Int, threadId: Int, parentCommentId: Int? = null) {
		viewModelScope.launch(Dispatchers.IO) {
			val newAccess = CommentsListController.sendComment(
				articleId = postId,
				text = text,
				parentCommentId = parentCommentId
			)
			
			CommentsListController.getThread(postId, threadId)?.let {
				comments.postValue(newAccess?.let { it1 ->
					CommentsCollection(
						it.comments,
						it1,
						pinnedComments = it.pinnedComments
					)
				}
					?: it)
			}
		}
	}
}

@Composable
fun CommentsThreadScreen(
	articleId: Int,
	threadId: Int,
	highlight: Int = 0,
	onAuthor: (alias: String) -> Unit,
	onImageClick: (url: String) -> Unit,
	onBack: () -> Unit,
) {
	
	val viewModel = viewModel<CommentThreadScreenViewModel>()
	val elementsSettings = remember {
		ElementSettings(
			fontSize = 16.sp,
			lineHeight = 16.sp,
			fitScreenWidth = false
		)
	}
	val comments by viewModel.comments.observeAsState()
	
	LaunchedEffect(key1 = Unit, block = {
		launch(Dispatchers.IO) {
			if (!viewModel.comments.isInitialized) {
				CommentsListController.getThread(articleId, threadId)?.let {
					viewModel.comments.postValue(it)
				}
			}
		}
	})
	
	Scaffold(
		modifier = Modifier.imePadding(),
		topBar = {
			TopAppBar(
				title = {
					Text(text = "Ветка комментария")
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							null
						)
					}
				})
		}
	) {
		val commentTextFieldFocusRequester = remember {
			FocusRequester()
		}
		val context = LocalContext.current
		var parentComment: Comment? by remember {
			mutableStateOf(null)
		}
		Column {
			val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 1, initialFirstVisibleItemScrollOffset = -200)
			LazyColumn(
				modifier = Modifier
					.padding(it)
					.weight(1f),
				contentPadding = PaddingValues(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				state = lazyListState
			) {
				comments?.let { collection ->
					items(
						items = collection.comments.toList()
					) {
						if (it.deleted){
							DeletedCommentItem(modifier = Modifier.padding(start = 20.dp.times(it.level.coerceAtMost(5))))
						}
						else {
							CommentItem(
								modifier = Modifier
									.padding(
										start = 20.dp.times(it.level.coerceAtMost(5))
									),
								comment = it,
								highlight = false,
								showReplyButton = collection.commentAccess.canComment,
								onAuthorClick = { onAuthor(it.author.alias) },
								onShare = {
									val intent = Intent(Intent.ACTION_SEND)
									intent.putExtra(
										Intent.EXTRA_TEXT,
										"https://habr.com/p/${articleId}/comments/#comment_${it.id}"
									)
									intent.setType("text/plain")
									context.startActivity(Intent.createChooser(intent, null))
								},
								onReplyClick = {
									parentComment = it
								}
							) {
								Column {
									it.let {
										SelectionContainer {
											((parseElement(
												it.message, SpanStyle(
													fontSize = 16.sp,
													color = MaterialTheme.colors.onSurface
												),
												onViewImageRequest = onImageClick
											).second)?.let { it1 ->
												it1(
													SpanStyle(
														fontSize = 16.sp,
														color = MaterialTheme.colors.onSurface
													),
													elementsSettings
												)
											})
										}
									}
									
								}
							}
						}
					}
				}
			}
			if (comments?.commentAccess?.canComment == true) {
				Column(modifier = Modifier) {
					AnimatedVisibility(
						visible = if (parentComment != null) true else false,
						enter = expandVertically(expandFrom = Alignment.Bottom),
						exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
					) {
						val comment = parentComment
						Column {
							Divider()
							val coroutineScope = rememberCoroutineScope()
							Row(modifier = Modifier
								.clickable {
									val index = comments?.comments?.indexOf(parentComment) ?: 0
									coroutineScope.launch {
										lazyListState.animateScrollToItem(index)
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
								IconButton(onClick = { parentComment = null }) {
									Icon(
										imageVector = Icons.Outlined.Close,
										contentDescription = "",
										tint = MaterialTheme.colors.secondary
									)
									
								}
							}
						}
					}
				}
				Divider()
				EnterCommentTextField(
					focusRequester = commentTextFieldFocusRequester,
					onSend = {
						viewModel.comment(
							text = it,
							postId = articleId,
							threadId = threadId,
							parentCommentId = parentComment?.id ?: threadId
						)
						parentComment = null
						commentTextFieldFocusRequester.freeFocus()
					})
			}
		}
		
	}
}
package com.garnegsoft.hubs.ui.screens.comments

import ArticleController
import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.comment.CommentsCollection
import com.garnegsoft.hubs.api.comment.Threads
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.parseElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import kotlin.math.roundToInt


class CommentsScreenViewModel : ViewModel() {
	var parentPostSnippet = MutableLiveData<ArticleSnippet>()
	var threadsData = MutableLiveData<Threads?>()
	val commentsData = MutableLiveData<CommentsCollection>()
	
	fun comment(text: String, postId: Int, parentCommentId: Int? = null) {
		viewModelScope.launch(Dispatchers.IO) {
			val newAccess = CommentsListController.sendComment(
				articleId = postId,
				text = text,
				parentCommentId = parentCommentId
			)
			
			CommentsListController.getThreads(postId)?.let {
				threadsData.postValue(newAccess?.let { it1 ->
					Threads(
						it.threads,
						it1
					)
				}
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
	onThreadClick: (threadId: Int) -> Unit,
	onUserClicked: (alias: String) -> Unit,
	onImageClick: (imageUrl: String) -> Unit
) {
	var viewModel = viewModel<CommentsScreenViewModel>(viewModelStoreOwner)
	
	val threadsData = viewModel.threadsData.observeAsState().value
	val commentsData by viewModel.commentsData.observeAsState()
	val articleSnippet = viewModel.parentPostSnippet.observeAsState().value
	
	val lazyListState = rememberLazyListState()
	
	val context = LocalContext.current
	
	val commentsDisplayMode by HubsDataStore.Settings
		.getValueFlow(context, HubsDataStore.Settings.CommentsDisplayMode)
		.collectAsState(initial = null)
	
	commentsDisplayMode?.let {
		val mode = HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.values()[it]
		LaunchedEffect(key1 = Unit) {
			if (
				(mode == HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Threads &&
					!viewModel.threadsData.isInitialized)
				||
				(mode == HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Default
					&&
					!viewModel.commentsData.isInitialized)
			) {
				launch(Dispatchers.IO) {
					viewModel.parentPostSnippet.postValue(ArticleController.getSnippet(parentPostId))
					if (mode == HubsDataStore.Settings.CommentsDisplayMode.CommentsDisplayModes.Default) {
						CommentsListController.getComments(parentPostId)?.let {
							viewModel.commentsData.postValue(it)
						}
					} else {
						CommentsListController.getThreads(parentPostId)?.let {
							viewModel.threadsData.postValue(it)
						}
						
					}
				}
			}
		}
		
		var doScrollToComment by rememberSaveable {
			mutableStateOf(true)
		}
		val itemsCountIndicator by remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount > 2 } }
		LaunchedEffect(key1 = commentsData, key2 = itemsCountIndicator, block = {
			commentId?.let { commId ->
				if (viewModel.commentsData.isInitialized && lazyListState.layoutInfo.totalItemsCount > 2 && doScrollToComment) {
					commentsData?.comments?.indexOf(commentsData!!.comments.find { it.id == commId })
						?.let {
							if (it > -1)
								if (showArticleSnippet)
									lazyListState.scrollToItem(it + 1)
								else
									lazyListState.scrollToItem(it)
							
							doScrollToComment = false
							
						}
				}
			}
		})
		val coroutineScope = rememberCoroutineScope()
		
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
			var answeringComment: Comment? by remember {
				mutableStateOf(null)
			}
			val showArticleHeader by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
			val commentTextFieldFocusRequester = remember { FocusRequester() }
			val randomCoroutineScope = rememberCoroutineScope()
			var articleHeaderOffset by remember { mutableStateOf(0f) }
			val elementsSettings = remember {
				ElementSettings(
					fontSize = 16.sp,
					lineHeight = 16.sp,
					fitScreenWidth = false
				)
			}
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
									onCommentsClick = { }
								)
							}
						}
						
						if (threadsData != null) {
							itemsIndexed(
								items = threadsData.threads,
								key = { index, it -> it.root.id }
							) { index, it ->
								val context = LocalContext.current
								
								Column(horizontalAlignment = Alignment.End) {
									
									CommentItem(
										comment = it.root,
										onAuthorClick = {
											onUserClicked(it.root.author.alias)
										},
										highlight = it.root.id == commentId,
										showReplyButton = false,
										onShare = {
											val intent = Intent(Intent.ACTION_SEND)
											intent.putExtra(
												Intent.EXTRA_TEXT,
												"https://habr.com/p/${parentPostId}/comments/#comment_${it.root.id}"
											)
											intent.setType("text/plain")
											context.startActivity(
												Intent.createChooser(
													intent,
													null
												)
											)
										},
										onReplyClick = { }
									) {
										Column {
											it.root.let {
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
									if (it.threadChildrenCommentsCount > 0) {
										Spacer(modifier = Modifier.height(2.dp))
										OutlinedButton(
											onClick = { onThreadClick(it.root.id) },
											shape = RoundedCornerShape(26.dp),
											contentPadding = PaddingValues(
												vertical = 12.dp,
												horizontal = 16.dp
											)
										) {
											Text(text = "Ответы (${it.threadChildrenCommentsCount})")
											Spacer(modifier = Modifier.width(4.dp))
											Icon(
												modifier = Modifier.size(18.dp),
												imageVector = Icons.Default.ArrowForward,
												contentDescription = null
											)
										}
									}
								}
								
								
							}
						} else if (commentsData != null) {
							itemsIndexed(
								items = commentsData!!.comments,
								key = { index, it -> it.id }
							) { index, it ->
								
								Column(horizontalAlignment = Alignment.End) {
									val parentComment = remember {
										commentsData!!.comments.firstOrNull { com -> com.id == it.parentCommentId }
									}
									val parentCommentIndex = remember {
										parentComment?.let {
											return@remember commentsData!!.comments.indexOf(it)
										} ?: 0
									}
									CommentItem(
										modifier = Modifier
											.padding(start = 20.dp * it.level.coerceAtMost(5)),
										comment = it,
										onAuthorClick = { onUserClicked(it.author.alias) },
										parentComment = parentComment,
										highlight = it.id == commentId,
										showReplyButton = commentsData!!.commentAccess.canComment,
										onShare = {
											val intent = Intent(Intent.ACTION_SEND)
											intent.putExtra(
												Intent.EXTRA_TEXT,
												"https://habr.com/p/${parentPostId}/comments/#comment_${it.id}"
											)
											intent.setType("text/plain")
											context.startActivity(
												Intent.createChooser(
													intent,
													null
												)
											)
										},
										onReplyClick = {
											answeringComment = it
										},
										onParentCommentSnippetClick = {
											coroutineScope.launch(Dispatchers.Main) {
												lazyListState.animateScrollToItem(parentCommentIndex + 1,
													-articleHeaderOffset.roundToInt()
												)
											}
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
					
					if (threadsData?.commentAccess?.canComment == true || commentsData?.commentAccess?.canComment == true) {
						AnimatedVisibility(
							visible = if (answeringComment != null) true else false,
							enter = expandVertically(expandFrom = Alignment.Bottom),
							exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
						) {
							val comment = answeringComment
							Column {
								Divider()
								
								Row(modifier = Modifier
									.clickable {
										val index =
											commentsData?.comments?.indexOf(answeringComment) ?: 0
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
									IconButton(onClick = { answeringComment = null }) {
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
						EnterCommentTextField(
							focusRequester = commentTextFieldFocusRequester,
							onSend = {
								viewModel.comment(
									text = it,
									postId = parentPostId,
									parentCommentId = answeringComment?.id
								)
								commentTextFieldFocusRequester.freeFocus()
							})
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
}




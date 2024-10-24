package com.garnegsoft.hubs.ui.screens.comments

import ArticleController
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.comment.CommentsCollection
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import com.garnegsoft.hubs.api.utils.animateShortScrollToItem
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCard
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.parseChildElements
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


class CommentsScreenViewModel : ViewModel() {
	var parentPostSnippet = MutableLiveData<ArticleSnippet>()
	val commentsData = MutableLiveData<CommentsCollection>()
	
	fun comment(text: String, postId: Int, parentCommentId: Int? = null) {
		viewModelScope.launch(Dispatchers.IO) {
			val newAccess = CommentsListController.sendComment(
				articleId = postId,
				text = text,
				parentCommentId = parentCommentId
			)
			
			CommentsListController.getComments(postId)?.let {
				commentsData.postValue(newAccess?.let { it1 ->
					CommentsCollection(it.comments, it.commentAccess, it.pinnedComments)
				} ?: it)
			}
		}
	}
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentsScreen(
	viewModelStoreOwner: ViewModelStoreOwner,
	parentPostId: Int,
	commentId: Int?,
	showArticleSnippet: Boolean = true,
	onBackClicked: () -> Unit,
	onArticleClicked: () -> Unit,
	onUserClicked: (alias: String) -> Unit,
	onImageClick: (imageUrl: String) -> Unit
) {
	var viewModel = viewModel<CommentsScreenViewModel>(viewModelStoreOwner)
	
	val commentsData by viewModel.commentsData.observeAsState()
	val articleSnippet = viewModel.parentPostSnippet.observeAsState().value
	val lazyListState = rememberLazyListState()
	
	val screenState =
		rememberCommentsScreenNavigationState(commentsList = commentsData, lazyListState = lazyListState)
	
	
	val context = LocalContext.current
	
	var returnToCommentIndex by remember { mutableStateOf<Int?>(null) }
	
	LaunchedEffect(key1 = Unit) {
		if (!viewModel.commentsData.isInitialized) {
			launch(Dispatchers.IO) {
				viewModel.parentPostSnippet.postValue(ArticleController.getSnippet(parentPostId))
				
				CommentsListController.getComments(parentPostId)?.let {
					viewModel.commentsData.postValue(it)
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
	var answeringComment: Comment? by remember {
		mutableStateOf(null)
	}
	val commentTextFieldFocusRequester = remember { FocusRequester() }
	var articleHeaderOffset by remember { mutableStateOf(0) }
	
	/**
	 * lazy list items count that should be skipped when navigating between comments
	 * (like article snippet, pinned comments)
	 */
	var itemOffsetCount by remember(commentsData) {
		mutableStateOf(
			1 + (commentsData?.pinnedComments?.size ?: 0)
		)
	}
	
	
	
	Scaffold(
		modifier = Modifier.imePadding(),
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
		},
		floatingActionButton = {
			returnToCommentIndex?.let { index ->
				
				
				FloatingActionButton(
					modifier = Modifier.sizeIn(maxWidth = 52.dp, maxHeight = 52.dp),
					onClick = {
						coroutineScope.launch {
							lazyListState.animateScrollToItem(
								index + itemOffsetCount,
								(-articleHeaderOffset).toInt()
							)
							returnToCommentIndex = null
						}
					},
					content = {
						Icon(
							imageVector = Icons.Sharp.KeyboardArrowDown,
							contentDescription = null
						)
					},
					elevation = FloatingActionButtonDefaults.elevation(4.dp, 0.dp)
				)
				
			}
			
		},
		bottomBar = {
			Column {
				if (commentsData?.commentAccess?.canComment == true) {
					AnimatedVisibility(
						visible = answeringComment != null,
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
		}
	) {
		
		val showArticleHeader by remember { derivedStateOf { lazyListState.firstVisibleItemIndex > 0 } }
		val elementsSettings = remember {
			ElementSettings(
				fontSize = 16.sp,
				lineHeight = 16.sp,
				fitScreenWidth = false
			)
		}
		
		LaunchedEffect(
			key1 = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } },
			block = {
				returnToCommentIndex?.let {
					if (lazyListState.firstVisibleItemIndex >= it + itemOffsetCount) {
						returnToCommentIndex = null
					}
				}
			})
		
		Box(modifier = Modifier.padding(it).fillMaxSize()) {
			Column(
				modifier = Modifier
					
					.imePadding()
			) {
				val articleCardStyle =
					ArticleCardStyle.defaultArticleCardStyle()?.copy(
						showImage = false,
						showTextSnippet = false,
						bookmarksButtonAllowedBeEnabled = articleSnippet?.relatedData != null
					)
				
				val ratingIconPainter = painterResource(id = R.drawable.rating)
				val replyIconPainter = painterResource(id = R.drawable.reply)
				
				LazyColumn(
					state = lazyListState,
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(8.dp),
					verticalArrangement = Arrangement.spacedBy(0.dp)
				) {
					
					if (articleSnippet != null) {
						item {
							
							articleCardStyle?.let {
								ArticleCard(
									article = articleSnippet,
									onClick = onArticleClicked,
									style = it,
									onAuthorClick = { onUserClicked(articleSnippet.author!!.alias) },
									onCommentsClick = { }
								)
								Spacer(modifier = Modifier.height(8.dp))
							}
							
						}
					}
					if (commentsData != null) {
						itemsIndexed(
							items = commentsData!!.pinnedComments,
						) { index, commentId ->
							val comment = commentsData!!.comments.find { it.id == commentId }!!
							
							CommentItem(
								comment = comment,
								onAuthorClick = { onUserClicked(comment.author.alias) },
								highlight = false,
								showReplyButton = commentsData!!.commentAccess.canComment,
								onShare = {
									val intent = Intent(Intent.ACTION_SEND)
									intent.putExtra(
										Intent.EXTRA_TEXT,
										"https://habr.com/p/${parentPostId}/comments/#comment_${comment.id}"
									)
									intent.setType("text/plain")
									context.startActivity(
										Intent.createChooser(
											intent,
											null
										)
									)
								},
								onReplyClick = {},
								isPinned = true,
								onGoToPinnedComment = {
									coroutineScope.launch {
										lazyListState.animateScrollToItem(
											commentsData!!.comments.indexOf(
												comment
											) + itemOffsetCount, (-articleHeaderOffset).toInt()
										)
									}
								},
								
							) {
								Column {
									it.let {
										SelectionContainer {
											((parseChildElements(
												Jsoup.parse(comment.message).body(), SpanStyle(
													fontSize = 16.sp,
													color = MaterialTheme.colors.onSurface
												),
												onViewImageRequest = onImageClick
											).second)?.let { it1 ->
												it1.forEach {
													it?.invoke(
														SpanStyle(
															fontSize = 16.sp,
															color = MaterialTheme.colors.onSurface
														),
														elementsSettings
													)
												}
												
											})
										}
									}
								}
							}
							Spacer(modifier = Modifier.height(8.dp))
						}
						
						itemsIndexed(
							items = commentsData!!.comments,
							key = { index, it -> it.id }
						) { index, it ->
							if (!screenState.collapsedComments.contains(it.id)) {
								if (screenState.collapsedCommentsParents.contains(it.id)){
									CollapsedThreadHeaderComment(
										modifier = Modifier
											.padding(start = 20.dp * it.level.coerceAtMost(5))
											.padding(bottom = 8.dp),
										onExpandClick = { screenState.expandThread(it.id) },
										comment = it
									)
								} else {
									Column(horizontalAlignment = Alignment.End) {
										val parentComment = remember {
											commentsData!!.comments.firstOrNull { com -> com.id == it.parentCommentId }
										}
										val parentCommentIndex = remember {
											parentComment?.let {
												return@remember commentsData!!.comments.indexOf(it)
											} ?: 0
										}
										if (it.deleted) {
											DeletedCommentItem(
												modifier = Modifier.padding(
													start = 20.dp * it.level.coerceAtMost(
														5
													)
												)
											)
										} else {
											var showMenu by remember { mutableStateOf(false) }
											CommentItem(
												modifier = Modifier
													.padding(start = 20.dp * it.level.coerceAtMost(5)),
												comment = it,
												onAuthorClick = { onUserClicked(it.author.alias) },
												parentComment = parentComment,
												highlight = it.id == commentId,
												showReplyButton = commentsData!!.commentAccess.canComment,
												menu = {
													if (showMenu) {
														CommentItemMenu(
															onCollapseCommentClick = {
																screenState.collapseComment(it.id)
																showMenu = false
															},
															onCollapseThreadClick = {
																screenState.collapseThread(
																	it.id
																)
																showMenu = false
															},
															onDismiss = { showMenu = false })
													}
												},
												onMenuButtonClick = { showMenu = true },
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
													commentTextFieldFocusRequester.requestFocus()
												},
												onParentCommentSnippetClick = {
													coroutineScope.launch(Dispatchers.Main) {
														it.parentCommentId?.let {
															screenState.scrollToComment(
																it
															)
														}
													}
												},
												ratingIconPainter = ratingIconPainter,
												replyIconPainter = replyIconPainter
											) {
												Column {
													it.let {
														SelectionContainer {
															((parseChildElements(
																Jsoup.parse(it.message).body(),
																SpanStyle(
																	fontSize = 16.sp,
																	color = MaterialTheme.colors.onSurface
																),
																onViewImageRequest = onImageClick
															).second)?.let { it1 ->
																it1.forEach {
																	it?.invoke(
																		SpanStyle(
																			fontSize = 16.sp,
																			color = MaterialTheme.colors.onSurface
																		),
																		elementsSettings
																	)
																}
																
															})
														}
													}
												}
											}
										}
										Spacer(modifier = Modifier.height(if (index != commentsData!!.comments.lastIndex) 8.dp else 0.dp))
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
			val articleHeaderOffsetAnimation by animateFloatAsState(targetValue =
			if (showArticleHeader) 0f else 1f)
			articleSnippet?.let {
				Layout(
					content = {
						Box {
							Row(
								modifier = Modifier
									.clickable {
										coroutineScope.launch {
											lazyListState.animateShortScrollToItem(0)
										}
									}
									.background(MaterialTheme.colors.surface)
									.fillMaxWidth()
//                    .height(50.dp)
									.height(IntrinsicSize.Min)
									.padding(8.dp),
								verticalAlignment = Alignment.CenterVertically
							) {
								
								
								Column(
									modifier = Modifier.weight(1f)
								) {
									Text(
										text = articleSnippet.author?.alias ?: "",
										style = MaterialTheme.typography.body2,
										fontWeight = FontWeight.W500,
										maxLines = 1,
										overflow = TextOverflow.Ellipsis
									)
									
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
				) { measurables, constraints ->
					val placeables = measurables.map { it.measure(constraints) }
					articleHeaderOffset = placeables.first().height
					screenState.setScrollBaseOffset(-placeables.first().height)
					layout(constraints.maxWidth, constraints.maxHeight) {
						placeables.first().placeRelative(0, (-placeables.first().height * articleHeaderOffsetAnimation).toInt())
					}
				}
				
			}
			screenState.newCommentsNavigationControlState?.let {
				Box(modifier = Modifier.align(Alignment.BottomCenter)
					.pointerInput(Unit){}
					.padding(start = 16.dp, top = 16.dp, end = 16.dp)
					.padding(bottom = if (commentsData?.commentAccess?.canComment == true) 16.dp else 48.dp)
				) {
					
						NewCommentsControl(state = screenState.newCommentsNavigationControlState!!)
					
				}
			}
			
		}
	}
	
}




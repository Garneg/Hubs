package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.data.CollapsingContent
import com.garnegsoft.hubs.data.PostType
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.data.dataStore.LastReadArticleController
import com.garnegsoft.hubs.data.history.HistoryController
import com.garnegsoft.hubs.data.rememberCollapsingContentState
import com.garnegsoft.hubs.data.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.HubsCircularProgressIndicator
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.*


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleScreen(
	articleId: Int,
	onBackButtonClicked: () -> Unit,
	onCommentsClicked: () -> Unit,
	onAuthorClicked: (alias: String) -> Unit,
	onHubClicked: (alias: String) -> Unit,
	onCompanyClick: (alias: String) -> Unit,
	onViewImageRequest: (url: String) -> Unit,
	onArticleClick: (id: Int) -> Unit,
	viewModelStoreOwner: ViewModelStoreOwner
) {
	val context = LocalContext.current
	val fontSize by HubsDataStore.Settings
		.getValueFlow(context, HubsDataStore.Settings.ArticleScreen.FontSize)
		.collectAsState(
			initial = null
		)
	val viewModel = viewModel<ArticleScreenViewModel>(viewModelStoreOwner)
	val article by viewModel.article.observeAsState()
	
	LaunchedEffect(key1 = Unit, block = {
		if (!viewModel.article.isInitialized) {
			viewModel.loadArticle(articleId)
		}
		if (!viewModel.mostReadingArticles.isInitialized) {
			viewModel.loadMostReading()
		}
	})
	
	val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
	val shareIntent = remember(article?.title) {
		val sendIntent = Intent(Intent.ACTION_SEND)
		
		sendIntent.putExtra(
			Intent.EXTRA_TEXT,
			"${article?.title ?: ""} — https://habr.com/p/${articleId}/"
		)
		sendIntent.setType("text/plain")
		Intent.createChooser(sendIntent, null)
	}
	val articleSaved by viewModel.articleExists(LocalContext.current, articleId)
		.collectAsState(false)
	var actionsVisible by rememberSaveable {
		mutableStateOf(false)
	}
	LaunchedEffect(key1 = actionsVisible, block = {
		if (!actionsVisible) {
			actionsVisible = true
		}
	})
	val navIconAnimatedValue by animateFloatAsState(
		targetValue = if (actionsVisible) 1f else 0f,
		animationSpec = tween()
	)
	val firstActionButtonAnimatedValue by animateFloatAsState(
		targetValue = if (actionsVisible) 1f else 0f,
		animationSpec = tween(delayMillis = 0)
	)
	val secondActionButtonAnimatedValue by animateFloatAsState(
		targetValue = if (actionsVisible) 1f else 0f,
		animationSpec = tween(delayMillis = 0)
	)
	val collapsingContentState = rememberCollapsingContentState()
	CollapsingContent(
		state = collapsingContentState,
		collapsingContent = {
			Box {
				TopAppBar(
					title = { },
					elevation = 0.dp,
					navigationIcon = {
						IconButton(
							modifier = Modifier
								.alpha(navIconAnimatedValue)
								.offset {
									IntOffset(
										(-48 * density + (48 * density * navIconAnimatedValue)).toInt(),
										0
									)
								},
							onClick = { onBackButtonClicked() }
						) {
							Icon(
								imageVector = Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = "",
								tint = MaterialTheme.colors.onPrimary
							)
						}
					},
					actions = {
						Box(
							modifier = Modifier
								.offset {
									IntOffset(
										(48 * density - (48 * density * firstActionButtonAnimatedValue)).toInt(),
										0
									)
								}
								.alpha(firstActionButtonAnimatedValue)
						) {
							if (articleSaved) {
								IconButton(
									onClick = {
										viewModel.deleteSavedArticle(
											id = articleId,
											context = context
										)
									},
									enabled = true
								) {
									Icon(
										imageVector = Icons.Outlined.Delete,
										contentDescription = "",
										tint = MaterialTheme.colors.onPrimary
									)
								}
							} else {
								IconButton(
									onClick = {
										viewModel.saveArticle(
											id = articleId,
											context = context
										)
									},
									enabled = true
								) {
									Icon(
										painterResource(id = R.drawable.download),
										contentDescription = "",
										tint = MaterialTheme.colors.onPrimary
									)
								}
							}
							
							
						}
						val shareIconColorAlpha by animateFloatAsState(targetValue = if (article != null) 1f else 0.5f)
						IconButton(
							modifier = Modifier
								.offset {
									IntOffset(
										(48 * density - (48 * density * secondActionButtonAnimatedValue)).toInt(),
										0
									)
								}
								.alpha(secondActionButtonAnimatedValue),
							onClick = { context.startActivity(shareIntent) },
							enabled = article != null
						) {
							Icon(
								Icons.Outlined.Share,
								contentDescription = "",
								tint = MaterialTheme.colors.onPrimary.copy(shareIconColorAlpha)
							)
						}
					})
				Divider(modifier = Modifier.align(Alignment.BottomCenter))
			}
		}
	) {
		Scaffold(
			backgroundColor = if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else MaterialTheme.colors.background,
			bottomBar = {
				AnimatedVisibility(
					visible = article != null,
					enter = slideIn { fullSize -> IntOffset(0, fullSize.height) }
				) {
					article?.let { article ->
						
						BottomAppBar(
							elevation = 0.dp,
							backgroundColor = MaterialTheme.colors.surface,
							modifier = Modifier
								.height(60.dp)
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
								
								VotesCountIndicator(
									show = showVotesCounter,
									stats = article.statistics,
									color = statisticsColor
								) {
									showVotesCounter = false
								}
								Icon(
									modifier = Modifier.size(18.dp),
									painter = painterResource(id = R.drawable.rating),
									contentDescription = "",
									tint = statisticsColor
								)
								Spacer(modifier = Modifier.width(1.dp))
								Text(
									if (article.statistics.score > 0)
										"+" + article.statistics.score.toString()
									else
										article.statistics.score.toString(),
									color = if (article.statistics.score > 0)
										RatingPositiveColor
									else if (article.statistics.score < 0)
										RatingNegativeColor
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
									formatLongNumbers(article.statistics.readingCount.toInt()),
									color = statisticsColor,
									fontWeight = FontWeight.W500
								)
							}
							var addedToBookmarks by rememberSaveable(article.relatedData?.bookmarked) {
								mutableStateOf(article.relatedData?.bookmarked ?: false)
							}
							var addedToBookmarksCount by rememberSaveable(article.statistics.bookmarksCount) {
								mutableStateOf(article.statistics.bookmarksCount)
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
													if (!ArticleController.removeFromBookmarks(
															article.id,
															article.postType == PostType.News
														)
													) {
														addedToBookmarks = true
														addedToBookmarksCount++
														addedToBookmarksCount =
															addedToBookmarksCount.coerceAtLeast(0)
													}
													
												} else {
													addedToBookmarks = true
													addedToBookmarksCount++
													if (!ArticleController.addToBookmarks(
															article.id,
															article.postType == PostType.News
														)
													) {
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
									text = addedToBookmarksCount.toString(),
									color = statisticsColor,
									fontWeight = FontWeight.W500
								)
							}
							Spacer(Modifier.width(4.dp))
							Box(
								modifier = Modifier
									.weight(1f)
									.fillMaxHeight()
									.clickable(onClick = onCommentsClicked)
							) {
								BadgedBox(
									modifier = Modifier.align(Alignment.Center),
									badge = {
										article.relatedData?.let {
											if (it.unreadComments > 0 && it.unreadComments < article.statistics.commentsCount) {
												Box(
													modifier = Modifier
														.size(8.dp)
														.clip(CircleShape)
														.background(RatingPositiveColor)
												)
											}
										}
									}) {
									Row(
										modifier = Modifier.padding(horizontal = 8.dp),
										verticalAlignment = Alignment.CenterVertically,
										horizontalArrangement = Arrangement.Center,
									) {
										Icon(
											modifier = Modifier.size(18.dp),
											painter = painterResource(id = R.drawable.comments_icon),
											contentDescription = "",
											tint = statisticsColor
										
										)
										Spacer(Modifier.width(4.dp))
										Text(
											text = formatLongNumbers(article.statistics.commentsCount),
											color = statisticsColor,
											fontWeight = FontWeight.W500
										)
									}
								}
							}
						}
						Divider()
					}
				}
			}
		) {
			
			article?.let { article ->
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
				var nodeParsed by rememberSaveable {
					mutableStateOf(false)
				}
				LaunchedEffect(key1 = Unit, block = {
					LastReadArticleController.setLastArticle(context, articleId)
					withContext(Dispatchers.IO) {
						HistoryController.insertArticle(articleId, context)
					}
					if (!viewModel.parsedArticleContent.isInitialized && fontSize != null) {
						val element =
							Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()!!
								.child(0)
								?: Element("")
						
						viewModel.parsedArticleContent.postValue(
							parseChildElements(
								element,
								spanStyle,
								onViewImageRequest
							).second
						)
						nodeParsed = true
					}
				})
				
				Box(modifier = Modifier.padding(it)) {
					if (nodeParsed && fontSize != null) {
						SelectionContainer {
							ArticleContent(
								article = article,
								onAuthorClicked = { onAuthorClicked(article.author!!.alias) },
								onHubClicked = onHubClicked,
								onCompanyClick = onCompanyClick,
								onViewImageRequest = onViewImageRequest,
								onArticleClick = onArticleClick
							)
						}
					}
				}
			} ?: Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) { HubsCircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
			
			if (collapsingContentState.isContentHidden.value) {
				Box(modifier = Modifier
					.fillMaxWidth()
					.height(35.dp)
					.drawBehind {
						drawRect(Brush.verticalGradient(listOf(Color.White, Color.Transparent)))
					})
			}
		}
	}
}

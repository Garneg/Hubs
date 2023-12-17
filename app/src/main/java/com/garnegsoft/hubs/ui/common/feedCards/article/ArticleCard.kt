package com.garnegsoft.hubs.ui.common.feedCards.article

import ArticleController
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.HubSubscribedColor
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
	article: ArticleSnippet,
	onClick: () -> Unit,
	onAuthorClick: () -> Unit,
	onCommentsClick: () -> Unit,
	style: ArticleCardStyle
) {
	
	Column(
		modifier = Modifier
			.clip(style.cardShape)
			.clickable(
				onClick = onClick
			)
			.background(style.backgroundColor)
	) {
		val authorInteractionSource = remember { MutableInteractionSource() }
		//Author and published time
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.clickable(
					interactionSource = authorInteractionSource,
					indication = null,
					onClick = onAuthorClick
				)
				.absolutePadding(
					left = style.innerPadding,
					top = style.innerPadding,
					right = style.innerPadding
				)
		) {
			Row(
				modifier = Modifier
					.height(style.authorAvatarSize)
					.weight(1f)
			) {
				article.author?.let {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.clip(style.innerElementsShape)
							.clickable(
								onClick = onAuthorClick
							)
					) {
						
						AsyncImage(
							modifier = Modifier
								.size(style.authorAvatarSize)
								.clip(style.innerElementsShape)
								.background(Color.White),
							model = it.avatarUrl,
							contentDescription = "avatar",
							onState = { })
						
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							modifier = Modifier.weight(1f),
							text = it.alias,
							style = style.authorTextStyle,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)
					}
					
				}
			}
			
			Spacer(modifier = Modifier.width(style.innerPadding))
			
			// Published time
			Text(
				text = article.timePublished,
				maxLines = 1,
				style = style.publishedTimeTextStyle
			)
		}
		
		Spacer(modifier = Modifier.height(style.innerPadding / 2))
		// Title
		Text(
			modifier = Modifier.padding(horizontal = style.innerPadding),
			text = article.title,
			style = style.titleTextStyle
		)
		Spacer(modifier = Modifier.height(0.dp))
		Row(
			modifier = Modifier.padding(horizontal = style.innerPadding, vertical = 2.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (article.complexity != PostComplexity.None) {
				val postComplexityColor = remember {
					when (article.complexity) {
						PostComplexity.Low -> Color(0xFF4CBE51)
						PostComplexity.Medium -> Color(0xFFEEBC25)
						PostComplexity.High -> Color(0xFFEB3B2E)
						else -> style.statisticsColor
					}
				}
				Icon(
					modifier = Modifier.size(height = 10.dp, width = 20.dp),
					painter = painterResource(id = R.drawable.speedmeter_hard),
					contentDescription = "",
					tint = postComplexityColor
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = when (article.complexity) {
						PostComplexity.Low -> "Простой"
						PostComplexity.Medium -> "Средний"
						PostComplexity.High -> "Сложный"
						else -> ""
					},
					color = postComplexityColor,
					fontWeight = FontWeight.W500,
					fontSize = 14.sp
				
				)
				
				Spacer(modifier = Modifier.width(12.dp))
			}
			Icon(
				modifier = Modifier.size(14.dp),
				painter = painterResource(id = R.drawable.clock_icon),
				contentDescription = "",
				tint = style.statisticsColor
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = remember { "${article.readingTime} мин" },
				color = style.statisticsColor,
				fontWeight = FontWeight.W500,
				fontSize = 14.sp
			)
			if (article.isTranslation) {
				Spacer(modifier = Modifier.width(12.dp))
				Icon(
					modifier = Modifier.size(14.dp),
					painter = painterResource(id = R.drawable.translate),
					contentDescription = "",
					tint = style.statisticsColor
				)
				Spacer(modifier = Modifier.width(2.dp))
				Text(
					text = "Перевод",
					color = style.statisticsColor,
					fontSize = 14.sp,
					fontWeight = FontWeight.W500
				)
			}
		}
		
		var hubsText by remember { mutableStateOf(buildAnnotatedString { }) }
		
		LaunchedEffect(key1 = Unit, block = {
			if (hubsText.text == "") {
				hubsText = buildAnnotatedString {
					article.hubs!!.forEachIndexed { index, it ->
						val textFunc = if (it.isProfiled) {
							{ append((it.title + "*").replace(" ", "\u00A0")) }
						} else {
							{ append(it.title.replace(" ", "\u00A0")) }
						}
						if (it.relatedData != null && it.relatedData.isSubscribed) {
							withStyle(SpanStyle(color = HubSubscribedColor)) {
								textFunc()
							}
						} else {
							textFunc()
						}
						if (index < article.hubs.size - 1) {
							append(", ")
						}
					}
				}
			}
		})
		// Hubs
		if (style.showHubsList)
			Text(
				modifier = Modifier.padding(horizontal = style.innerPadding),
				text = hubsText, style = style.hubsTextStyle
			)
		
		
		// Snippet
		if (style.showTextSnippet) {
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				modifier = Modifier.padding(horizontal = style.innerPadding),
				text = article.textSnippet,
				maxLines = style.snippetMaxLines,
				overflow = TextOverflow.Ellipsis,
				style = style.snippetTextStyle
			)
		}
		// Image to draw attention (a.k.a. KDPV)
		if (style.showImage && !article.imageUrl.isNullOrBlank()) {
			Spacer(modifier = Modifier.height(6.dp))
			AsyncImage(
				modifier = Modifier
					.padding(horizontal = style.innerPadding)
					.fillMaxWidth()
					.clip(style.innerElementsShape)
					.aspectRatio(1.8f)
					.background(MaterialTheme.colors.onSurface.copy(0.1f)),
				model = ImageRequest.Builder(LocalContext.current)
					.crossfade(true)
					.data(article.imageUrl).build(),
				contentScale = ContentScale.Crop,
				contentDescription = "",
			)
			
		}
		
		//Stats
		Row(
			modifier = Modifier
				.padding(horizontal = style.innerPadding)
				.height(38.dp + style.innerPadding * 2)
				.fillMaxWidth()
				.clip(style.innerElementsShape),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		) {
			
			//Rating
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.weight(1f)
					.padding(vertical = style.innerPadding),
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					painter = painterResource(id = R.drawable.rating),
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = style.statisticsColor
				)
				
				Spacer(modifier = Modifier.width(4.dp))
				if (article.statistics.score > 0) {
					Text(
						text = '+' + article.statistics.score.toString(),
						style = style.statisticsTextStyle,
						color = RatingPositiveColor
					)
				} else
					if (article.statistics.score < 0) {
						Text(
							text = article.statistics.score.toString(),
							style = style.statisticsTextStyle,
							color = RatingNegativeColor
						)
					} else {
						Text(
							text = article.statistics.score.toString(),
							style = style.statisticsTextStyle
						)
					}
			}
			
			//Views
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.weight(1f)
					.padding(vertical = style.innerPadding),
				horizontalArrangement = Arrangement.Center
			
			) {
				Icon(
					painter = painterResource(id = R.drawable.views_icon),
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = style.statisticsColor
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = formatLongNumbers(article.statistics.readingCount),
					style = style.statisticsTextStyle
				)
			}
			val addToBookmarksInteractionSource = remember { MutableInteractionSource() }
			val bookmarksCoroutineScope = rememberCoroutineScope()
			
			var addedToBookmarks by rememberSaveable(article.relatedData?.bookmarked) {
				mutableStateOf(article.relatedData?.bookmarked ?: false)
			}
			var addedToBookmarksCount by rememberSaveable(article.relatedData?.bookmarked) {
				mutableStateOf(article.statistics.bookmarksCount)
			}
			
			val bookmarkButtonClickLambda: () -> Unit = remember {
				{
					article.relatedData?.let {
						bookmarksCoroutineScope.launch(Dispatchers.IO) {
							if (addedToBookmarks) {
								addedToBookmarks = false
								addedToBookmarksCount--
								addedToBookmarksCount =
									addedToBookmarksCount.coerceAtLeast(0)
								if (!ArticleController.removeFromBookmarks(
										article.id,
										article.type == PostType.News
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
										article.type == PostType.News
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
			}
			var showPopup by rememberSaveable {
				mutableStateOf(false)
			}
			var bounds by remember {
				mutableStateOf(IntSize.Zero)
			}
			
			val hapticFeedback = LocalHapticFeedback.current
			val addToBookmarksButtonEnabled =
				remember { style.bookmarksButtonAllowedBeEnabled && article.relatedData != null }
			//Added to bookmarks
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.padding(vertical = style.innerPadding)
					.weight(1f)
					.fillMaxHeight()
					.clip(style.innerElementsShape)
					.combinedClickable(
						onClick = bookmarkButtonClickLambda,
						onLongClick = {
							showPopup = true
							hapticFeedback.performHapticFeedback(
								HapticFeedbackType.LongPress
							)
						},
						enabled = addToBookmarksButtonEnabled,
					)
					.onGloballyPositioned {
						bounds = it.size
					},
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					painter = article.relatedData?.let {
						if (addedToBookmarks)
							painterResource(id = R.drawable.bookmark_filled)
						else
							null
					} ?: painterResource(id = R.drawable.bookmark),
					contentDescription = null,
					modifier = Modifier.size(18.dp),
					tint = style.statisticsColor
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = addedToBookmarksCount.toString(),
					style = style.statisticsTextStyle
				)
				val context = LocalContext.current
				val coroutineScope = rememberCoroutineScope()
				SaveArticlePopup(
					show = showPopup,
					bounds = bounds,
					cardStyle = style,
					onSaveClick = {
						OfflineArticlesController.downloadArticle(article.id, context)
						showPopup = false
					},
					onDeleteClick = {
						OfflineArticlesController.deleteArticle(article.id, context)
						showPopup = false
					},
					onDismissRequest = { showPopup = false },
					articleId = article.id
				)
				
			}
			
			val commentsInteractionSource = remember { MutableInteractionSource() }
			//Comments
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center,
				modifier = Modifier
					.weight(1f)
					.clickable(
						interactionSource = commentsInteractionSource,
						indication = null,
						enabled = style.commentsButtonEnabled,
						onClick = onCommentsClick
					)
					.padding(vertical = style.innerPadding)
					.fillMaxHeight()
					.absolutePadding(4.dp)
					.clip(style.innerElementsShape)
					.clickable(
						enabled = style.commentsButtonEnabled,
						onClick = onCommentsClick
					)
			) {
				
				BadgedBox(
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
						
					}
				) {
					Row(
						modifier = Modifier.padding(horizontal = 8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							painter = painterResource(id = R.drawable.comments_icon),
							contentDescription = null,
							modifier = Modifier.size(18.dp),
							tint = style.statisticsColor
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = formatLongNumbers(article.statistics.commentsCount),
							style = style.statisticsTextStyle,
							overflow = TextOverflow.Clip,
							maxLines = 1
						)
					}
					
				}
				
				
			}
		}
	}
}

package com.garnegsoft.hubs.ui.common

import ArticleController
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesDao
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesDatabase
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Style of the [ArticleCard]
 */
@Immutable
data class ArticleCardStyle(
	val innerPadding: Dp = 16.dp,
	val innerElementsShape: Shape = RoundedCornerShape(10.dp),
	val cardShape: Shape = RoundedCornerShape(26.dp),
	
	val showImage: Boolean = true,
	
	val showTextSnippet: Boolean = true,
	
	val showHubsList: Boolean = true,
	
	val commentsButtonEnabled: Boolean = true,
	
	val addToBookmarksButtonEnabled: Boolean = false,
	
	val backgroundColor: Color = Color.White,
	
	val textColor: Color = Color.Black,
	
	val authorAvatarSize: Dp = 34.dp,
	
	val snippetMaxLines: Int = 4,
	
	val rippleColor: Color = textColor,
	
	val imageLoadingIndicatorColor: Color = SecondaryColor,
	
	val titleTextStyle: TextStyle = TextStyle(
		color = textColor,
		fontSize = 20.sp,
		fontWeight = FontWeight.W700,
	),
	
	val snippetTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.75f),
		fontSize = 16.sp,
		fontWeight = FontWeight.W400,
		lineHeight = 16.sp.times(1.25f)
	),
	
	val authorTextStyle: TextStyle = TextStyle(
		color = textColor,
		fontSize = 14.sp,
		fontWeight = FontWeight.W600
	),
	
	val publishedTimeTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.5f),
		fontSize = 12.sp,
		fontWeight = FontWeight.W400
	),
	
	/**
	 * Text style of statistics row, note that text color for score indicator won't apply if it is non-zero value (will be red or green)
	 */
	val statisticsColor: Color = textColor.copy(alpha = 0.5f),
	
	val statisticsTextStyle: TextStyle = TextStyle(
		color = statisticsColor,
		fontSize = 15.sp,
		fontWeight = FontWeight.W400
	),
	
	val hubsTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.5f),
		fontSize = 12.sp,
		fontWeight = FontWeight.W600
	)

)

@Composable
@ReadOnlyComposable
fun defaultArticleCardStyle(): ArticleCardStyle {
	return ArticleCardStyle(
		backgroundColor = MaterialTheme.colors.surface,
		textColor = MaterialTheme.colors.onSurface,
		statisticsColor = MaterialTheme.colors.onSurface
			.copy(
				alpha = if (MaterialTheme.colors.isLight) {
					0.75f
				} else {
					0.5f
				}
			
			),
	)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
	article: ArticleSnippet,
	onClick: () -> Unit,
	onAuthorClick: () -> Unit,
	onCommentsClick: () -> Unit,
	style: ArticleCardStyle = defaultArticleCardStyle().copy(addToBookmarksButtonEnabled = article.relatedData != null)
) {
	
	val ripple = rememberRipple(color = style.rippleColor)
	Column(
		modifier = Modifier
			.clip(style.cardShape)
			
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = ripple, onClick = onClick
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
								interactionSource = authorInteractionSource,
								indication = ripple,
								onClick = onAuthorClick
							)
					) {
						if (it.avatarUrl.isNullOrBlank()) {
							Icon(
								modifier = Modifier
									.size(style.authorAvatarSize)
									.clip(style.innerElementsShape)
									.background(Color.White)
									.border(
										BorderStroke(
											2.dp,
											placeholderColorLegacy(article.author.alias)
										),
										shape = style.innerElementsShape
									)
									.padding(2.dp),
								painter = painterResource(id = R.drawable.user_avatar_placeholder),
								contentDescription = "",
								tint = placeholderColorLegacy(article.author.alias)
							)
						} else {
							AsyncImage(
								modifier = Modifier
									.size(style.authorAvatarSize)
									.clip(style.innerElementsShape)
									.background(Color.White),
								model = it.avatarUrl,
								contentDescription = "avatar",
								onState = { })
						}
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							it.alias,
							style = style.authorTextStyle,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)
						Spacer(modifier = Modifier.width(4.dp))
					}
				}
			}
			
			
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
			modifier = Modifier.padding(horizontal = style.innerPadding),
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
				text = "${article.readingTime} мин",
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
		
		var hubsText by remember { mutableStateOf("") }
		
		LaunchedEffect(key1 = Unit, block = {
			if (hubsText == "") {
				hubsText = article.hubs!!.joinToString(separator = ", ") {
					if (it.isProfiled)
						(it.title + "*").replace(" ", "\u00A0")
					else
						it.title.replace(" ", "\u00A0")
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
		if (style.showTextSnippet)
			Text(
				modifier = Modifier.padding(horizontal = style.innerPadding),
				text = article.textSnippet,
				maxLines = style.snippetMaxLines,
				overflow = TextOverflow.Ellipsis,
				style = style.snippetTextStyle
			)
		
		// Image to draw attention (a.k.a. KDPV)
		if (style.showImage && !article.imageUrl.isNullOrBlank()) {
			Spacer(modifier = Modifier.height(4.dp))
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
						color = RatingPositive
					)
				} else
					if (article.statistics.score < 0) {
						Text(
							text = article.statistics.score.toString(),
							style = style.statisticsTextStyle,
							color = RatingNegative
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
								if (!ArticleController.removeFromBookmarks(article.id, article.type == PostType.News)) {
									addedToBookmarks = true
									addedToBookmarksCount++
									addedToBookmarksCount =
										addedToBookmarksCount.coerceAtLeast(0)
								}
								
							} else {
								addedToBookmarks = true
								addedToBookmarksCount++
								if (!ArticleController.addToBookmarks(article.id, article.type == PostType.News)) {
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
			//Added to bookmarks
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.clickable(
						onClick = bookmarkButtonClickLambda,
						enabled = style.addToBookmarksButtonEnabled,
						interactionSource = addToBookmarksInteractionSource,
						indication = null
					)
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
						enabled = style.addToBookmarksButtonEnabled,
						interactionSource = addToBookmarksInteractionSource,
						indication = ripple,
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
						coroutineScope.launch(Dispatchers.IO) {
							val downloaded = OfflineArticlesController.downloadArticle(article.id, context)
							if (downloaded) {
								withContext(Dispatchers.Main) {
									Toast.makeText(context, "Статья скачана!", Toast.LENGTH_SHORT).show()
								}
							}
						}
						showPopup = false
					},
					onDeleteClick = {
						coroutineScope.launch(Dispatchers.IO) {
							val deleted = OfflineArticlesController.deleteArticle(article.id, context)
							if (deleted) {
								withContext(Dispatchers.Main) {
									Toast.makeText(context, "Статья удалена!", Toast.LENGTH_SHORT)
										.show()
								}
							}
						}
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
						interactionSource = commentsInteractionSource,
						ripple,
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
										.background(RatingPositive)
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

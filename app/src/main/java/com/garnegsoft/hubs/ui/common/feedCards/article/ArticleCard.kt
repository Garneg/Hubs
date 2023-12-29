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
import com.garnegsoft.hubs.api.PublicationComplexity
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
			if (article.complexity != PublicationComplexity.None) {
				val publicationComplexityColor = remember {
					when (article.complexity) {
						PublicationComplexity.Low -> Color(0xFF4CBE51)
						PublicationComplexity.Medium -> Color(0xFFEEBC25)
						PublicationComplexity.High -> Color(0xFFEB3B2E)
						else -> style.statisticsColor
					}
				}
				Icon(
					modifier = Modifier.size(height = 10.dp, width = 20.dp),
					painter = painterResource(id = R.drawable.speedmeter_hard),
					contentDescription = "",
					tint = publicationComplexityColor
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = when (article.complexity) {
						PublicationComplexity.Low -> "Простой"
						PublicationComplexity.Medium -> "Средний"
						PublicationComplexity.High -> "Сложный"
						else -> ""
					},
					color = publicationComplexityColor,
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
		var addedToBookmarks by rememberSaveable {
			mutableStateOf(article.relatedData?.bookmarked ?: false)
		}
		var addedToBookmarksCount by rememberSaveable(addedToBookmarks) {
			mutableStateOf(article.statistics.bookmarksCount)
		}
		val bookmarksCoroutineScope = rememberCoroutineScope()
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
		
		
		val context = LocalContext.current
		var showPopup by rememberSaveable {
			mutableStateOf(false)
		}
		
		val hapticFeedback = LocalHapticFeedback.current
		
		ArticleStats(
			statistics = article.statistics,
			addedToBookmarks = article.relatedData?.bookmarked ?: false,
			onAddToBookmarksClicked = bookmarkButtonClickLambda,
			onCommentsClick = onCommentsClick,
			unreadCommentsCount = article.relatedData?.let { if (it.unreadComments < article.statistics.commentsCount) it.unreadComments else 0 } ?: 0,
			saveArticlePopup = { bounds ->
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
			},
			onShowSavingPopup = {
				showPopup = true
				hapticFeedback.performHapticFeedback(
					HapticFeedbackType.LongPress
				)
			},
			bookmarksButtonEnabled = article.relatedData != null,
			style = style
		)
	}
}

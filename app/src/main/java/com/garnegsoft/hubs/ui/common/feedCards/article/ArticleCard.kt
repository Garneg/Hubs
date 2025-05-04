package com.garnegsoft.hubs.ui.common.feedCards.article

import ArticleController
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.ui.theme.TranslationLabelColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleCard(
	cardData: ArticleCardData,
	onClick: () -> Unit,
	onAuthorClick: () -> Unit,
	onCommentsClick: () -> Unit,
	configuration: ArticleCardConfiguration,
	toggleBookmark: (suspend (addToBookmarks: Boolean, articleId: Int) -> Boolean)? = null,
	ratingIconPainter: Painter = painterResource(id = R.drawable.rating),
	viewsIconPainter: Painter = painterResource(id = R.drawable.views_icon),
	bookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark),
	filledBookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark_filled),
	commentIconPainter: Painter = painterResource(id = R.drawable.comments_icon),
	complexityIconPainter: Painter = painterResource(id = R.drawable.speedmeter_hard),
	readingTimeIconPainter: Painter = painterResource(id = R.drawable.clock_icon),
	translationIconPainter: Painter = painterResource(id = R.drawable.translation)
) {
	
	Column(
		modifier = Modifier
			.clip(configuration.cardShape)
			.clickable(
				onClick = onClick
			)
			.background(configuration.backgroundColor)
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
					left = configuration.innerPadding,
					top = configuration.innerPadding,
					right = configuration.innerPadding
				)
		) {
			Row(
				modifier = Modifier
					.height(configuration.authorAvatarSize)
					.weight(1f)
			) {
				cardData.author?.let {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.clip(configuration.innerElementsShape)
							.clickable(
								onClick = onAuthorClick
							)
					) {
						
						AsyncImage(
							modifier = Modifier
								.size(configuration.authorAvatarSize)
								.clip(configuration.innerElementsShape)
								// I set shape here because it works better and white rounded corners are
								// less visible. Although, it's still noticeable :(
								.background(
									color = Color.White,
									shape = configuration.innerElementsShape
								),
							model = it.avatarUrl,
							contentDescription = "avatar",
							onState = { })
						
						Spacer(modifier = Modifier.width(8.dp))
						Text(
							modifier = Modifier.weight(1f),
							text = it.alias,
							style = configuration.authorTextStyle,
							overflow = TextOverflow.Ellipsis,
							maxLines = 1
						)
					}
					
				}
			}
			
			Spacer(modifier = Modifier.width(configuration.innerPadding))
			
			// Published time
			Text(
				text = cardData.articlePublicationDate,
				maxLines = 1,
				style = configuration.publishedTimeTextStyle
			)
		}
		
		Spacer(modifier = Modifier.height(configuration.innerPadding / 2))
		// Title
		Text(
			modifier = Modifier.padding(horizontal = configuration.innerPadding),
			text = cardData.articleTitle,
			style = configuration.titleTextStyle
		)
		Spacer(modifier = Modifier.height(0.dp))
		Row(
			modifier = Modifier.padding(horizontal = configuration.innerPadding, vertical = 2.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			if (cardData.articleComplexity != PublicationComplexity.None) {
				val publicationComplexityColor = remember {
					when (cardData.articleComplexity) {
						PublicationComplexity.Low -> Color(0xFF4CBE51)
						PublicationComplexity.Medium -> Color(0xFFEEBC25)
						PublicationComplexity.High -> Color(0xFFEB3B2E)
						else -> configuration.statisticsColor
					}
				}
				Icon(
					modifier = Modifier.size(height = 10.dp, width = 20.dp),
					painter = complexityIconPainter,
					contentDescription = "",
					tint = publicationComplexityColor
				)
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = when (cardData.articleComplexity) {
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
				painter = readingTimeIconPainter,
				contentDescription = "",
				tint = configuration.statisticsColor
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "${cardData.timeToRead} мин",
				color = configuration.statisticsColor,
				fontWeight = FontWeight.W500,
				fontSize = 14.sp
			)
			if (cardData.isArticleTranslation) {
				Spacer(modifier = Modifier.width(12.dp))
				Icon(
					modifier = Modifier.size(14.dp),
					painter = translationIconPainter,
					contentDescription = "",
					tint = TranslationLabelColor
				)
				Spacer(modifier = Modifier.width(2.dp))
				Text(
					text = "Перевод",
					color = TranslationLabelColor,
					fontSize = 14.sp,
					fontWeight = FontWeight.W500
				)
			}
		}


		// Hubs
		if (configuration.showHubsList)
			Text(
				modifier = Modifier.padding(horizontal = configuration.innerPadding),
				text = cardData.hubs, style = configuration.hubsTextStyle,
			)
		
		
		// Snippet
		if (configuration.showTextSnippet) {
			Spacer(modifier = Modifier.height(2.dp))
			Text(
				modifier = Modifier.padding(horizontal = configuration.innerPadding),
				text = cardData.articleTextSnippet,
				maxLines = configuration.snippetMaxLines,
				overflow = TextOverflow.Ellipsis,
				style = configuration.snippetTextStyle
			)
		}
		// Image to draw attention (a.k.a. KDPV)
		if (configuration.showImage && !cardData.articleImageUrl.isNullOrBlank()) {
			Spacer(modifier = Modifier.height(6.dp))
			AsyncImage(
				modifier = Modifier
					.padding(horizontal = configuration.innerPadding)
					.fillMaxWidth()
					.clip(configuration.innerElementsShape)
					.aspectRatio(1.8f)
					.background(MaterialTheme.colors.onSurface.copy(0.1f)),
				model = ImageRequest.Builder(LocalContext.current)
					.crossfade(true)
					.data(cardData.articleImageUrl).build(),
				contentScale = ContentScale.Crop,
				contentDescription = "",
			)
			
		}
		var addedToBookmarks by remember {
			mutableStateOf(cardData.articleBookmarked)
		}
		var addedToBookmarksCount by remember {
			mutableIntStateOf(cardData.articleStatistics.bookmarks)
		}
		val bookmarksCoroutineScope = rememberCoroutineScope()

		
		
		val context = LocalContext.current
		var showPopup by rememberSaveable {
			mutableStateOf(false)
		}
		
		val hapticFeedback = LocalHapticFeedback.current
		val bookmarkState = rememberBookmarkState(cardData.articleStatistics.bookmarks, cardData.articleBookmarked)

		ArticleStats(
			statistics = cardData.articleStatistics,
			addedToBookmarks = bookmarkState.bookmarked,
			bookmarksCount = bookmarkState.bookmarksCount,
			onAddToBookmarksClicked = {
				toggleBookmark?.let {
					bookmarksCoroutineScope.launch {
						bookmarkState.toggleBookmark(it, cardData.id)
					}
				}
				},
			onCommentsClick = onCommentsClick,
			saveArticlePopup = { bounds ->
				SaveArticlePopup(
					show = showPopup,
					bounds = bounds,
					cardStyle = configuration,
					onSaveClick = {
						OfflineArticlesController.downloadArticle(cardData.id, context)
						showPopup = false
					},
					onDeleteClick = {
						OfflineArticlesController.deleteArticle(cardData.id, context)
						showPopup = false
					},
					onDismissRequest = { showPopup = false },
					articleId = cardData.id
				)
			},
			onShowSavingPopup = {
				showPopup = true
				hapticFeedback.performHapticFeedback(
					HapticFeedbackType.LongPress
				)
			},
			bookmarksButtonEnabled = configuration.commentsButtonEnabled && !bookmarkState.throttleButton && toggleBookmark != null,
			style = configuration,
			ratingIconPainter = ratingIconPainter,
			viewsIconPainter = viewsIconPainter,
			bookmarkIconPainter = bookmarkIconPainter,
			filledBookmarkIconPainter = filledBookmarkIconPainter,
			commentIconPainter = commentIconPainter,
		)
	}
}

class BookmarkState(
	initialBookmarksCount: Int,
	bookmarked: Boolean
) {
	private val _bookmarksCount = mutableIntStateOf(initialBookmarksCount)
	val bookmarksCount: Int by _bookmarksCount

	private val _bookmarked = mutableStateOf(bookmarked)
	val bookmarked: Boolean by _bookmarked

	// Used to disable bookmark button, when user already pressed it, but server hasn't responded yet
	private val _throttleButton = mutableStateOf(false)
	val throttleButton: Boolean by _throttleButton

	var isRealBookmarked: Boolean = bookmarked

	/**
	 * @param lambda takes a function with arguments addToBookmarks (**true** - it should add article to bookmarks, **false** - remove),
	 * and articleId that indicates which article should be toggled. The function should return true on success and false if request failed
	 * @param articleId id of an article just to pass it down to [lambda]
	 */
	suspend fun toggleBookmark(lambda: suspend (addToBookmarks: Boolean, articleId: Int) -> Boolean, articleId: Int) {
		if (!throttleButton){
			if (isRealBookmarked) {
				_bookmarksCount.intValue--
				_bookmarksCount.intValue.coerceAtLeast(0)
			} else {
				_bookmarksCount.intValue++
			}
			_bookmarked.value = !isRealBookmarked
			_throttleButton.value = true
			if (lambda(!isRealBookmarked, articleId)){	// success case
				isRealBookmarked = !isRealBookmarked

			} else {									// failed case
				_bookmarked.value = isRealBookmarked
				if (isRealBookmarked) {
					_bookmarksCount.intValue++
				} else {
					_bookmarksCount.intValue--
					_bookmarksCount.intValue.coerceAtLeast(0)
				}
			}
			_throttleButton.value = false
		}
	}

	companion object{
		val Saver = listSaver<BookmarkState, Any>(
			save = { listOf(it.bookmarksCount, it.isRealBookmarked) },
			restore = { BookmarkState(it[0] as Int, it[1] as Boolean)}
		)
	}
}

@Composable
fun rememberBookmarkState(initialBookmarksCount: Int, bookmarked: Boolean): BookmarkState {
	return rememberSaveable(
		bookmarked,
		saver = BookmarkState.Saver
	) {
		BookmarkState(initialBookmarksCount, bookmarked)
	}
}

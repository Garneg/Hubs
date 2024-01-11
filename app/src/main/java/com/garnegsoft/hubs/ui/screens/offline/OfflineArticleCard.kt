package com.garnegsoft.hubs.ui.screens.offline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.AsyncGifImage
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle.Companion.defaultArticleCardStyle
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.TranslationLabelColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OfflineArticleCard(
	article: OfflineArticleSnippet,
	onClick: () -> Unit,
	onDelete: () -> Unit,
	style: ArticleCardStyle
) {
	var showDeleteButton by rememberSaveable { mutableStateOf(false) }
	val haptic = LocalHapticFeedback.current
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clip(style.cardShape)
			.background(style.backgroundColor)
			.combinedClickable(
				onLongClick = {
					if (!showDeleteButton) {
						showDeleteButton = true
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
					}
				},
				onClick = onClick
			)
			.padding(style.innerPadding)
	) {
		article.authorName?.let {
			Row(verticalAlignment = Alignment.CenterVertically) {
				if (article.authorAvatarUrl != null) {
					AsyncGifImage(
						modifier = Modifier
							.size(style.authorAvatarSize)
							.clip(style.innerElementsShape),
						model = article.authorAvatarUrl, contentDescription = ""
					)
				} else {
					Icon(
						modifier = Modifier
							.size(style.authorAvatarSize)
							.border(
								width = 2.dp,
								color = placeholderColorLegacy(it),
								shape = style.innerElementsShape
							)
							.background(Color.White, shape = style.innerElementsShape)
							.padding(2.dp),
						painter = painterResource(id = R.drawable.user_avatar_placeholder),
						contentDescription = "",
						tint = placeholderColorLegacy(it)
					)
				}
				Spacer(modifier = Modifier.width(4.dp))
				Text(text = it, style = style.authorTextStyle)
				Spacer(modifier = Modifier.weight(1f))
				Text(text = formatTime(article.timePublished), style = style.publishedTimeTextStyle)
			}
			Spacer(modifier = Modifier.height(8.dp))
		}
		Text(text = article.title, style = style.titleTextStyle)
		
		var hubsText by rememberSaveable { mutableStateOf("") }
		
		LaunchedEffect(key1 = Unit, block = {
			if (hubsText == "") {
				hubsText = article.hubs.hubsList.joinToString(separator = ", ") {
					it.replace(" ", "\u00A0")
				}
			}
		})
		Row(
			verticalAlignment = Alignment.CenterVertically,
		) {
			Icon(
				modifier = Modifier.size(14.dp),
				painter = painterResource(id = R.drawable.clock_icon),
				contentDescription = "",
				tint = style.statisticsColor
			)
			Spacer(modifier = Modifier.width(2.dp))
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
		Text(
			text = hubsText, style = style.hubsTextStyle
		)
		if (article.thumbnailUrl != null) {
			Spacer(modifier = Modifier.height(4.dp))
			AsyncGifImage(
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(16f / 9f)
					.clip(style.innerElementsShape)
					.background(
						if (MaterialTheme.colors.isLight)
							Color.Companion.Transparent
						else
							MaterialTheme.colors.onSurface.copy(0.2f)
					),
				model = article.thumbnailUrl,
				contentScale = ContentScale.Crop,
				contentDescription = ""
			)
			
		}
		
		AnimatedVisibility(
			visible = showDeleteButton,
			enter = fadeIn() + scaleIn()
		) {
			Column {
				Spacer(modifier = Modifier.height(style.innerPadding / 2))
				CompositionLocalProvider(
					LocalRippleTheme provides object : RippleTheme {
						@Composable
						override fun defaultColor(): Color {
							return RatingNegativeColor
						}
						
						@Composable
						override fun rippleAlpha(): RippleAlpha {
							return RippleAlpha(0.1f, 0.1f, 0.1f, 0.1f)
						}
						
					}
				) {
					TextButton(
						modifier = Modifier
							.fillMaxWidth()
							.height(48.dp)
							.clip(style.innerElementsShape),
						onClick = onDelete,
						colors = ButtonDefaults.textButtonColors(
							backgroundColor = RatingNegativeColor.copy(
								0.08f
							)
						)
					) {
						Text(text = "Удалить")
					}
				}
			}
		}
	}
}
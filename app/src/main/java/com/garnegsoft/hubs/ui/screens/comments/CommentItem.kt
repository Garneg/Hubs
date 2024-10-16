package com.garnegsoft.hubs.ui.screens.comments

import android.graphics.Paint.Align
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.utils.htmlBlocksToText
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.delay
import org.jsoup.Jsoup

@Composable
fun CommentItem(
	modifier: Modifier = Modifier,
	comment: Comment,
	parentComment: Comment? = null,
	highlight: Boolean,
	showReplyButton: Boolean,
	onAuthorClick: () -> Unit,
	onShare: () -> Unit,
	onReplyClick: () -> Unit,
	onParentCommentSnippetClick: (() -> Unit)? = null,
	isPinned: Boolean = false,
	onGoToPinnedComment: (() -> Unit)? = null,
	onMenuButtonClick: (() -> Unit)? = null,
	menu: (@Composable () -> Unit)? = null,
	ratingIconPainter: Painter = painterResource(id = R.drawable.rating),
	replyIconPainter: Painter = painterResource(id = R.drawable.reply),
	content: @Composable () -> Unit
) {
	val onSurfaceColor = MaterialTheme.colors.onSurface
	val commentFlagColor = remember {
		when {
			comment.inModeration -> Color(0x33DF2020)
			comment.isNew -> Color(0x33337EE7)
			comment.isUserAuthor -> Color(0x33ECC72B)
			comment.isArticleAuthor -> Color(0x336BEB40)
			
			else -> onSurfaceColor.copy(0f)
		}
	}
	Column(
		modifier = modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(26.dp))
			.background(MaterialTheme.colors.surface)
			.padding(16.dp)
	) {
		if (isPinned) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = "Закреплённый комментарий",
					fontSize = 14.sp,
					color = MaterialTheme.colors.onSurface.copy(0.5f)
				)
				Spacer(modifier = Modifier.width(4.dp))
				Icon(
					modifier = Modifier.size(15.dp),
					painter = painterResource(id = R.drawable.pin), contentDescription = null,
					tint = MaterialTheme.colors.onSurface.copy(0.5f))
				
			}
			Spacer(modifier = Modifier.height(8.dp))
		}
		parentComment?.let {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(IntrinsicSize.Max)
					.clip(RoundedCornerShape(2.dp))
					.clickable(
						onClick = { onParentCommentSnippetClick?.invoke() },
						enabled = onParentCommentSnippetClick != null
					)
			) {
				Spacer(
					modifier = Modifier
						.width(4.dp)
						.fillMaxHeight()
						.clip(CircleShape)
						.background(MaterialTheme.colors.secondary)
				)
				Spacer(modifier = Modifier.width(8.dp))
				if (it.deleted) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(vertical = 12.dp),
						contentAlignment = Alignment.CenterStart
					) {
						Text(
							text = "Удаленное сообщение",
							color = MaterialTheme.colors.onSurface.copy(0.5f)
						)
					}
				} else {
					Column {
						Text(
							text = it.author.alias,
							color = MaterialTheme.colors.secondary,
							fontWeight = FontWeight.W500
						)
						Text(
							text = remember { htmlBlocksToText(it.message) },
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
			}
			
			Spacer(modifier = Modifier.height(8.dp))
		}
		Row(verticalAlignment = Alignment.CenterVertically) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.weight(1f)
					.clip(RoundedCornerShape(10.dp))
					.clickable(onClick = onAuthorClick)
					.background(commentFlagColor)
					.border(
						width = 1.5.dp,
						color = if (highlight) commentFlagColor.copy(0.5f) else Color.Unspecified,
						shape = RoundedCornerShape(10.dp)
					)
			) {
				AsyncImage(
					modifier = Modifier
						.size(34.dp)
						.clip(RoundedCornerShape(10.dp)),
					model = comment.author.avatarUrl, contentDescription = "authorAvatar"
				)
				
				Spacer(modifier = Modifier.width(4.dp))
				Column {
					Text(text = comment.author.alias)
					Row(
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(text = comment.publishedTime, fontSize = 12.sp, color = Color.Gray)
						if (comment.edited)
							Text(text = " (изм.)", fontSize = 12.sp, color = Color.Gray)
					}
				}
				
			}
			if (menu != null && onMenuButtonClick != null) {
				Spacer(modifier = Modifier.width(4.dp))
				Box {
					Box(
						modifier = Modifier
							.size(34.dp)
							.clip(CircleShape)
							.clickable(onClick = onMenuButtonClick),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.Default.MoreVert,
							contentDescription = "Меню комментария"
						)
					}
					menu()
				}
			}
		}
		Spacer(modifier = Modifier.height(4.dp))
		
		content.invoke()
		
		Spacer(modifier = Modifier.height(4.dp))
		val statisticsColor =
			if (MaterialTheme.colors.isLight)
				MaterialTheme.colors.onSurface.copy(0.75f)
			else
				MaterialTheme.colors.onSurface.copy(0.5f)
		if (comment.score != null) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Row(
					modifier = Modifier.weight(1f),
					verticalAlignment = Alignment.CenterVertically,
				) {
					
					val densityFactor = LocalDensity.current.density
					
					val positionProvider = remember(densityFactor) {
						object : PopupPositionProvider {
							override fun calculatePosition(
								anchorBounds: IntRect,
								windowSize: IntSize,
								layoutDirection: LayoutDirection,
								popupContentSize: IntSize
							): IntOffset {
								return IntOffset(
									(anchorBounds.left - (24 * densityFactor)).toInt(),
									anchorBounds.top - popupContentSize.height + (8 * densityFactor).toInt()
								)
							}
							
						}
					}
					
					var showVotesCounter by remember { mutableStateOf(false) }
					var visible by remember { mutableStateOf(false) }
					LaunchedEffect(key1 = showVotesCounter, block = {
						if (showVotesCounter) {
							visible = showVotesCounter
						}
					})
					LaunchedEffect(key1 = visible, block = {
						delay(150)
						if (!visible) {
							showVotesCounter = false
						}
						
					})
					val offset by animateFloatAsState(
						targetValue = if (visible) 0f else 8f,
						animationSpec = tween(150)
					)
					val alpha by animateFloatAsState(
						targetValue = if (visible) 1f else 0.0f,
						animationSpec = tween(150)
					)
					
					Box() {
						IconButton(onClick = { showVotesCounter = !showVotesCounter }) {
							if (showVotesCounter) {
								Popup(
									properties = PopupProperties(focusable = true),
									popupPositionProvider = positionProvider,
									onDismissRequest = { visible = false }
								) {
									Surface(
										modifier = Modifier
											.offset(0.dp, offset.dp)
											.alpha(alpha)
											.padding(16.dp),
										shape = RoundedCornerShape(8.dp),
										color = MaterialTheme.colors.surface,
										elevation = 4.dp
									) {
										Box(
											modifier = Modifier.padding(8.dp)
										) {
											comment.votesCount?.let {
												val votesMinus =
													(comment.votesCount - comment.score) / 2
												val votesPlus = comment.votesCount - votesMinus
												
												Text(
													text = "Всего голосов " +
														"${comment.votesCount}: " +
														"￪${votesPlus} и " +
														"￬${votesMinus}",
													color = statisticsColor
												)
												
											}
										}
									}
									
									
								}
								
								
							}
							
							Row(verticalAlignment = Alignment.CenterVertically) {
								Icon(
									modifier = Modifier.size(18.dp),
									painter = ratingIconPainter,
									contentDescription = "",
									tint = statisticsColor
								)
								Spacer(modifier = Modifier.width(2.dp))
								Text(
									text = if (comment.score > 0) {
										"+"
									} else {
										""
									} + comment.score,
									color = when {
										comment.score > 0 -> RatingPositiveColor
										comment.score < 0 -> RatingNegativeColor
										else -> statisticsColor
									}
								)
							}
						}
						
					}
					
				}
				
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					
					IconButton(onClick = onShare) {
						Icon(
							modifier = Modifier.size(20.dp),
							imageVector = Icons.Outlined.Share,
							contentDescription = "",
							tint = statisticsColor
						)
					}
					
				}
				
				if (showReplyButton && !isPinned) {
					IconButton(onClick = onReplyClick) {
						Icon(
							painter = replyIconPainter,
							contentDescription = "",
							tint = statisticsColor
						)
					}
				}
				if (isPinned) {
					IconButton(onClick = onGoToPinnedComment!!) {
						Icon(
							modifier = Modifier.size(22.dp),
							imageVector = Icons.Filled.ArrowForward,
							contentDescription = "",
							tint = statisticsColor
						)
					}
				}
			}
			
		}
	}
	
}
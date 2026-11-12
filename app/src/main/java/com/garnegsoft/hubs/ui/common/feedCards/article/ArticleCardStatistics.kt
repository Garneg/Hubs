package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInQuint
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleStats(
    statistics: ArticleCardData.Statistics,
    addedToBookmarks: Boolean,
    bookmarksCount: Int,
    onAddToBookmarksClicked: () -> Unit,
    onCommentsClick: () -> Unit,
    bookmarksButtonEnabled: Boolean,
    onShowSavingPopup: () -> Unit,
    saveArticlePopup: @Composable (bounds: IntSize) -> Unit,
    style: ArticleCardConfiguration,
    ratingIconPainter: Painter = painterResource(id = R.drawable.rating),
    viewsIconPainter: Painter = painterResource(id = R.drawable.views_icon),
    bookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark),
    filledBookmarkIconPainter: Painter = painterResource(id = R.drawable.bookmark_filled),
    commentIconPainter: Painter = painterResource(id = R.drawable.comments_icon),
    showSavingPopup: Boolean,
) {
    //Stats
    Row(
        modifier = Modifier
            .padding(horizontal = style.innerPadding)
            .fillMaxWidth()
            .clip(style.innerElementsShape),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                painter = ratingIconPainter,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = style.statisticsColor
            )
            Spacer(modifier = Modifier.width(2.dp))
            if (statistics.rating > 0) {
                Text(
                    text = '+' + statistics.rating.toString(),
                    style = style.statisticsTextStyle,
                    color = RatingPositiveColor
                )
            } else
                if (statistics.rating < 0) {
                    Text(
                        text = statistics.rating.toString(),
                        style = style.statisticsTextStyle,
                        color = RatingNegativeColor
                    )
                } else {
                    Text(
                        text = statistics.rating.toString(),
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
                painter = viewsIconPainter,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = style.statisticsColor
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = statistics.views,
                style = style.statisticsTextStyle
            )
        }

        var bounds by remember {
            mutableStateOf(IntSize.Zero)
        }

        val hapticFeedback = LocalHapticFeedback.current
        //Added to bookmarks
        Box(
            modifier = Modifier
                .padding(vertical = style.innerPadding / 2f)
                .weight(1f)
        ) {
            val savingPopupIndicationAlpha by animateFloatAsState(if (showSavingPopup) 0.1f else 0f)
            val iconAnimation = remember { Animatable(0f) }
            val animationCoroutineScope = rememberCoroutineScope()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(style.innerElementsShape)
                    .drawBehind {
                        if (showSavingPopup) {
                            drawRect(color = style.statisticsColor.copy(savingPopupIndicationAlpha))
                        }
                    }
                    .combinedClickable(
                        onClick = {
                            onAddToBookmarksClicked()
                            animationCoroutineScope.launch {
                                iconAnimation.animateTo(
                                    -0.5f,
                                    animationSpec = tween(
                                        durationMillis = 130,
                                        easing = EaseOutQuint
                                    )
                                )
                                iconAnimation.animateTo(
                                    0f,
                                    animationSpec = tween(
                                        delayMillis = 30,
                                        durationMillis = 60,
                                        easing = EaseInQuint
                                    )
                                )
                            }
                        },
                        hapticFeedbackEnabled = false,
                        onLongClick = {

                            animationCoroutineScope.async {
                                iconAnimation.animateTo(
                                    1f,
                                    animationSpec = tween(
                                        durationMillis = 130,
                                        easing = EaseOutQuint
                                    )
                                )
                                onShowSavingPopup()
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.ContextClick
                                )
                                iconAnimation.animateTo(
                                    -0.3f,
                                    animationSpec = tween(
                                        delayMillis = 40,
                                        durationMillis = 120,
                                        easing = EaseInQuint
                                    )
                                )
                                iconAnimation.animateTo(
                                    0f,
                                    animationSpec = tween(
                                        delayMillis = 0,
                                        durationMillis = 50,
                                        easing = EaseInQuint
                                    )
                                )

                            }

                        },
                        enabled = style.bookmarksButtonAllowedBeEnabled && bookmarksButtonEnabled,
                    )
                    .onGloballyPositioned {
                        bounds = it.size
                    }
                    .padding(vertical = style.innerPadding * 0.8f),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter =
                        if (addedToBookmarks)
                            filledBookmarkIconPainter
                        else
                            bookmarkIconPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer {
                            translationY = -20 * iconAnimation.value
                            scaleY = 1f + (0.1f * iconAnimation.value)

                        },
                    tint = style.statisticsColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                AnimatedContent(
                    bookmarksCount,
                    transitionSpec = { if (targetState > initialState) {
                        slideInVertically { -it }.togetherWith(slideOutVertically { it } + fadeOut())
                    } else {
                        slideInVertically { it }.togetherWith(slideOutVertically { -it } + fadeOut())
                    }
                    }
                ) { it ->
                    Text(
                        text = it.toString(),
                        style = style.statisticsTextStyle
                    )
                }
            }
            saveArticlePopup(bounds)
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
                .padding(vertical = style.innerPadding / 2f)
                .clip(style.innerElementsShape)
                .clickable(
                    enabled = style.commentsButtonEnabled,
                    onClick = onCommentsClick
                )
                .padding(vertical = style.innerPadding * 0.75f)
        ) {

            BadgedBox(
                badge = {
                    if (statistics.hasUnreadComments) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RatingPositiveColor)
                        )
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = commentIconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = style.statisticsColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statistics.comments,
                        style = style.statisticsTextStyle,
                        overflow = TextOverflow.Clip,
                        maxLines = 1
                    )
                }

            }


        }
    }
}
package com.garnegsoft.hubs.ui.screens.article

import ArticleController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ArticleScreenBottomBar(
    modifier: Modifier = Modifier,
    article: Article,
    onCommentsClick: () -> Unit,
    enableBookmarkButton: Boolean
) {
    val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
    Row(
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(AppBarDefaults.ContentPadding)
            .heightIn(max = 60.dp),
    ) {
        var showVotesCounter by remember {
            mutableStateOf(false)
        }

        ArticleBottomBarButton(
            modifier = Modifier.weight(1f),
            onClick = {
                showVotesCounter = true
            }
        ) {
            VotesCountIndicator(
                show = showVotesCounter,
                data = remember { article.statistics.toVotesCountIndicatorData() },
                color = statisticsColor,
                onDismiss = {
                    showVotesCounter = false
                },
                popupOffset = DpOffset(-2.dp, -2.dp)
            )
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

        ArticleBottomBarButton(
            modifier = Modifier.weight(1f),
            onClick = {},
            enabled = false,

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
        val bookmarkCoroutineScope = rememberCoroutineScope()
        var buttonThrottle by remember { mutableStateOf(false) }

        ArticleBottomBarButton(
            modifier = Modifier.weight(1f),
            onClick = {
                buttonThrottle = true
                article.relatedData?.let {
                    bookmarkCoroutineScope.launch(Dispatchers.IO) {

                        if (addedToBookmarks) { // Remove from bookmarks
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
                            buttonThrottle = false
                        } else {  // Add to bookmarks
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
                            buttonThrottle = false
                        }
                    }
                }
            },
            enabled = enableBookmarkButton && !buttonThrottle
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

        ArticleBottomBarButton(
            modifier = Modifier.weight(1f),
            onClick = onCommentsClick,

            ) {
            BadgedBox(
//                modifier = Modifier.align(Alignment.Center),
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

@Composable
fun ArticleBottomBarButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = enabled
            )
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
                enabled = enabled
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}
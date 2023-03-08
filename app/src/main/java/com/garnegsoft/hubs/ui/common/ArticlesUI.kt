package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import com.garnegsoft.hubs.ui.theme.SecondaryColor


/**
 * Style of the [ArticleCard]
 */
//@Immutable
data class ArticleCardStyle(
    val innerPadding: PaddingValues = PaddingValues(16.dp),
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

    val statisticsIconsTintColor: Color = textColor,

    val rippleColor: Color = textColor,

    val imageLoadingIndicatorColor: Color = SecondaryColor,

    val titleTextStyle: TextStyle = TextStyle(
        color = textColor,
        fontSize = 20.sp,
        fontWeight = FontWeight.W700
    ),

    val snippetTextStyle: TextStyle = TextStyle(
        color = Color.DarkGray,
        fontSize = 16.sp,
        fontWeight = FontWeight.W400
    ),

    val authorTextStyle: TextStyle = TextStyle(
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.W600
    ),

    val publishedTimeTextStyle: TextStyle = TextStyle(
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.W400
    ),

    /**
     * Text style of statistics row, note that text color for score indicator won't apply if it is non-zero value (will be red or green)
     */
    val statisticsTextStyle: TextStyle = TextStyle(
        color = textColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.W400
    ),

    val hubsTextStyle: TextStyle = TextStyle(
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.W600
    )

)

@Composable
fun defaultArticleCardStyle() : ArticleCardStyle {
    return ArticleCardStyle(
        backgroundColor = MaterialTheme.colors.surface,
        textColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface)
    )
}

@Composable
fun ArticleCard(
    article: ArticleSnippet,
    onClick: () -> Unit = { },
    onAuthorClick: () -> Unit = { },
    onAddToBookmarksClick: () -> Unit = { },
    onCommentsClick: () -> Unit = { },
    style: ArticleCardStyle = defaultArticleCardStyle()
) {
    Column(
        modifier = Modifier
            .clip(style.cardShape)
            .clickable(
                interactionSource = remember { mutableStateOf(MutableInteractionSource()) }.value,
                indication = rememberRipple(color = style.rippleColor), onClick = onClick
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
                    onClick = onAuthorClick //rememberRipple()
                )
                .absolutePadding(
                    left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    top = style.innerPadding.calculateTopPadding(),
                    right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                )
        )
        {
            Row(
                modifier = Modifier
                    .weight(1f)
            ) {
                article.author?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(style.innerElementsShape)
                            .clickable(
                                interactionSource = authorInteractionSource,
                                indication = rememberRipple(color = style.rippleColor),
                                onClick = onAuthorClick
                            )
                    ) {
                        if (it.avatarUrl.isNullOrBlank()) {
                            Icon(
                                modifier = Modifier
                                    .size(style.authorAvatarSize)
                                    .clip(
                                        style.innerElementsShape
                                    )
                                    .background(Color.White)
                                    .border(
                                        BorderStroke(2.dp, placeholderColor(article.author.alias)),
                                        shape = style.innerElementsShape
                                    )
                                    .padding(2.dp),
                                painter = painterResource(id = R.drawable.user_avatar_placeholder),
                                contentDescription = "",
                                tint = placeholderColor(article.author.alias)
                            )
                        } else {
                            AsyncImage(
                                modifier = Modifier
                                    .size(style.authorAvatarSize)
                                    .clip(style.innerElementsShape),
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

        Spacer(modifier = Modifier.height(style.innerPadding.calculateTopPadding() / 2))
        // Title
        Text(
            modifier = Modifier.absolutePadding(
                left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
            ),
            text = article.title,
            style = style.titleTextStyle
        )
        Spacer(modifier = Modifier.height(0.dp))
        Row(
            modifier = Modifier.padding(
                horizontal = style.innerPadding.calculateStartPadding(
                    LayoutDirection.Ltr
                ),
                vertical = 0.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (article.complexity != PostComplexity.None) {
                Icon(
                    modifier = Modifier.size(height = 10.dp, width = 20.dp),
                    painter = painterResource(id = R.drawable.speedmeter_hard),
                    contentDescription = "",
                    tint = when (article.complexity){
                        PostComplexity.Low -> Color(0xFF4CBE51)
                        PostComplexity.Medium -> Color(0xFFEEBC25)
                        PostComplexity.High -> Color(0xFFEB3B2E)
                        else -> Color.Red
                    }
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = when (article.complexity) {
                        PostComplexity.Low -> "Простой"
                        PostComplexity.Medium -> "Средний"
                        PostComplexity.High -> "Сложный"
                        else -> ""
                    },
                    color = when (article.complexity){
                        PostComplexity.Low -> Color(0xFF4CBE51)
                        PostComplexity.Medium -> Color(0xFFEEBC25)
                        PostComplexity.High -> Color(0xFFEB3B2E)
                        else -> Color.Red
                    },
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp

                )

                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(
                painter = painterResource(id = R.drawable.clock_icon),
                modifier = Modifier.size(14.dp),
                contentDescription = "",
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${article.readingTime} мин",
                color = Color.DarkGray,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            )
        }
        // Hubs
        if (style.showHubsList)
            Text(
                modifier = Modifier.absolutePadding(
                    left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                ),
                text = article.hubs!!.joinToString(separator = ", ") {
                    if (it.isProfiled)
                        (it.title + "*").replace(" ", "\u00A0")
                    else
                        it.title.replace(" ", "\u00A0")
                }, style = style.hubsTextStyle
            )

        // Snippet
        if (style.showTextSnippet)
            Text(
                modifier = Modifier.absolutePadding(
                    left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                ),
                text = article.textSnippet,
                maxLines = style.snippetMaxLines,
                overflow = TextOverflow.Ellipsis,
                style = style.snippetTextStyle
            )

        // Image to draw attention (a.k.a. KDPV)
        if (style.showImage && !article.imageUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            var showLoadingIndication by remember { mutableStateOf(true) }
            AsyncImage(
                modifier = Modifier
                    .absolutePadding(
                        left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                        right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
                    )
                    .fillMaxWidth()
                    .clip(style.innerElementsShape)
                    .aspectRatio(16f / 9f)
                    ,
                model = article.imageUrl,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Success)
                        showLoadingIndication = false
                },
                contentDescription = ""
            )

//            if (showLoadingIndication)
//                Box(
//                    modifier = Modifier
//                        .absolutePadding(
//                            left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
//                            right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr)
//                        )
//                        .fillMaxWidth()
//                        .wrapContentSize()
//                        .aspectRatio(16f / 9f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator(color = style.imageLoadingIndicatorColor)
//                }
//
        }

        //Stats
        Row(
            modifier = Modifier
                .absolutePadding(
                    right = style.innerPadding.calculateRightPadding(LayoutDirection.Ltr),
                    left = style.innerPadding.calculateLeftPadding(LayoutDirection.Ltr)
                )
                .height(38.dp + style.innerPadding.calculateBottomPadding() * 2)
                .fillMaxWidth()
                .shadow(
                    0.dp,
                    style.innerElementsShape,
                    spotColor = Color.Black
                )
                .clip(style.innerElementsShape)
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            //Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .absolutePadding(
                        top = style.innerPadding.calculateTopPadding(),
                        bottom = style.innerPadding.calculateBottomPadding()
                    ),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rating),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.padding(2.dp))
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
                    .absolutePadding(
                        top = style.innerPadding.calculateTopPadding(),
                        bottom = style.innerPadding.calculateBottomPadding()
                    ),
                horizontalArrangement = Arrangement.Center

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.views_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = style.textColor
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = article.statistics.readingCount,
                    style = style.statisticsTextStyle
                )
            }
            val addToBookmarksInteractionSource by remember {
                mutableStateOf(
                    MutableInteractionSource()
                )
            }

            //Added to bookmarks
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        interactionSource = addToBookmarksInteractionSource,
                        indication = null,
                        enabled = style.addToBookmarksButtonEnabled,
                        onClick = onAddToBookmarksClick
                    )
                    .absolutePadding(
                        top = style.innerPadding.calculateTopPadding(),
                        bottom = style.innerPadding.calculateBottomPadding()
                    )
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(0.dp, 0.dp)
                    .clip(style.innerElementsShape)
                    .clickable(
                        addToBookmarksInteractionSource,
                        rememberRipple(color = style.rippleColor),
                        enabled = style.addToBookmarksButtonEnabled,
                        onClick = onAddToBookmarksClick
                    ),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_to_bookmarks),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = article.statistics.favoritesCount,
                    style = style.statisticsTextStyle
                )
            }

            val commentsInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }
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
                    .absolutePadding(
                        top = style.innerPadding.calculateTopPadding(),
                        bottom = style.innerPadding.calculateBottomPadding()
                    )

                    .fillMaxHeight()
                    .absolutePadding(4.dp)
                    .clip(style.innerElementsShape)
                    .clickable(
                        interactionSource = commentsInteractionSource,
                        rememberRipple(color = style.rippleColor),
                        enabled = style.commentsButtonEnabled,
                        onClick = onCommentsClick
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.comments_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)

                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = article.statistics.commentsCount,
                    style = style.statisticsTextStyle,
                    overflow = TextOverflow.Clip,
                    maxLines = 1
                )
            }
        }
    }
}

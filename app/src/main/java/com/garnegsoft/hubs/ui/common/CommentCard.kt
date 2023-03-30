package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.utils.placeholderColor


@Composable
fun CommentCard(
    comment: CommentSnippet,
    style: CommentCardStyle = CommentCardStyle(),
    onCommentClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onParentPostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(style.shape)
            .background(style.background)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onParentPostClick)
                .padding(
                    top = style.padding.calculateTopPadding(),
                    start = style.padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = style.padding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = style.padding
                        .calculateBottomPadding()
                        .div(2)
                )
        ) {
            Text(
                text = comment.parentPost.title,
                style = style.parentPostTextStyle
            )
        }
        Divider(
            modifier = Modifier
                .padding(horizontal = style.padding.calculateStartPadding(LayoutDirection.Ltr))
        )
        Column(
            modifier = Modifier
                .clickable(onClick = onCommentClick)
                .padding(
                    top = style.padding
                        .calculateTopPadding()
                        .div(2),
                    start = style.padding.calculateStartPadding(LayoutDirection.Ltr),
                    bottom = style.padding.calculateBottomPadding(),
                    end = style.padding.calculateEndPadding(LayoutDirection.Ltr)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(style.avatarSize)
                            .clip(style.avatarShape)
                            .clickable(onClick = onAuthorClick)
                    ) {
                        if (comment.author.avatarUrl != null) {
                            AsyncImage(
                                model = comment.author.avatarUrl,
                                modifier = Modifier
                                    .size(style.avatarSize)
                                    .clip(style.avatarShape),
                                contentDescription = ""
                            )
                        } else {
                            Icon(
                                modifier = Modifier
                                    .size(style.avatarSize)
                                    .border(
                                        width = 2.dp,
                                        color = placeholderColor(comment.author.alias),
                                        shape = style.avatarShape
                                    )
                                    .padding(2.dp),
                                painter = painterResource(id = R.drawable.user_avatar_placeholder),
                                contentDescription = "",
                                tint = placeholderColor(comment.author.alias)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = comment.author.alias,
                            style = style.authorAliasTextStyle
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }


                Text(text = comment.timePublished, style = style.publishedTimeTextStyle)
            }

            Spacer(modifier = Modifier.height(style.padding.calculateTopPadding().div(3)))
            Text(
                text = comment.text,
                style = style.messageTextStyle
            )
            Spacer(modifier = Modifier.height(style.padding.calculateBottomPadding()))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = R.drawable.rating),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(4.dp))
                comment.score.let {
                    Text(
                        text =
                        if (it > 0) {
                            "+"
                        } else {
                            ""
                        }
                                + it

                    )
                }
            }

        }

    }
}


data class CommentCardStyle(
    val background: Color = Color.White,
    val parentPostTextStyle: TextStyle = TextStyle(fontWeight = FontWeight.W700, fontSize = 18.sp),
    val shape: Shape = RoundedCornerShape(26.dp),
    val avatarShape: Shape = RoundedCornerShape(8.dp),
    val padding: PaddingValues = PaddingValues(18.dp),
    val avatarSize: Dp = 34.dp,
    val authorAliasTextStyle: TextStyle = TextStyle(fontWeight = FontWeight.W500),
    val messageTextStyle: TextStyle = TextStyle.Default,
    val publishedTimeTextStyle: TextStyle = TextStyle(
        color = Color.Gray,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp
    )

)
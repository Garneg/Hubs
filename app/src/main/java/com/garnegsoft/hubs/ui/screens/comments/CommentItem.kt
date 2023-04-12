package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive

@Composable
fun CommentItem(
    comment: Comment,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
    ) {
        Row(
            modifier =
            (if (comment.isArticleAuthor) {
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(8.dp)
                    )
                    .background(Color(0x536BEB40))
            }
            else {
                Modifier.clip(
                    RoundedCornerShape(8.dp)
                )
            })
        ) {
            if (comment.author.avatarUrl == null || comment.author.avatarUrl.isBlank()) {
                Icon(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(
                            BorderStroke(2.dp, color = placeholderColor(comment.author.alias)),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(2.dp),
                    painter = painterResource(id = R.drawable.user_avatar_placeholder),
                    contentDescription = "",
                    tint = placeholderColor(comment.author.alias)
                )
            } else {
                AsyncImage(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    model = comment.author.avatarUrl, contentDescription = "authorAvatar"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = comment.author?.alias ?: "")

                Text(text = comment.publishedTime, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        content.invoke()

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = R.drawable.rating), contentDescription = ""
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = if (comment.score > 0) {
                    "+"
                } else {
                    ""
                } + comment.score,
                color = when {
                    comment.score > 0 -> RatingPositive
                    comment.score < 0 -> RatingNegative
                    else -> Color.Unspecified
                }
            )
        }
    }

}
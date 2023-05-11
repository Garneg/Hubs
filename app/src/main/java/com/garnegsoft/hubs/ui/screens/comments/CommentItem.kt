package com.garnegsoft.hubs.ui.screens.comments

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.screens.article.parseElement
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive

@Composable
fun CommentItem(
    modifier: Modifier = Modifier,
    comment: Comment,
    highlight: Boolean,
    onAuthorClick: () -> Unit,
    onShare: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAuthorClick)
                .background(commentFlagColor)
                .border(
                    width = 1.5.dp,
                    color = if (highlight) commentFlagColor.copy(0.5f) else Color.Unspecified,
                    shape = RoundedCornerShape(10.dp)
                )
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
                        .clip(RoundedCornerShape(10.dp)),
                    model = comment.author.avatarUrl, contentDescription = "authorAvatar"
                )
            }

            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = comment.author?.alias ?: "")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = comment.publishedTime, fontSize = 12.sp, color = Color.Gray)
                    if (comment.edited)
                        Text(text = " (изменено)", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        content.invoke()

        Spacer(modifier = Modifier.height(4.dp))
        if (comment.score != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = onShare) {
                        Icon(modifier = Modifier.size(18.dp), imageVector = Icons.Outlined.Share, contentDescription = "")
                    }

                }
                
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painter = painterResource(id = R.drawable.reply), contentDescription = "")
                }
            }

        }
    }

}
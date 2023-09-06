package com.garnegsoft.hubs.ui.screens.article

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.theme.HubsTheme
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive

@Composable
fun ArticleShort(
    article: ArticleSnippet,
    onClick: () -> Unit
) {
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp)
            .padding(8.dp)
    ) {
        Text(
            text = article.title,
            color = MaterialTheme.colors.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.rating),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    )
                Spacer(modifier = Modifier.width(2.dp))
                if (article.statistics.score > 0) {
                    Text(
                        text = '+' + article.statistics.score.toString(),
                        color = RatingPositive,
                    )
                } else {
                    if (article.statistics.score < 0) {
                        Text(
                            text = article.statistics.score.toString(),
                            color = RatingNegative
                        )
                    } else {
                        Text(
                            text = article.statistics.score.toString(),
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.views_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formatLongNumbers(article.statistics.readingCount),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.comments_icon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formatLongNumbers(article.statistics.commentsCount),
                    overflow = TextOverflow.Clip,
                    maxLines = 1,
                )
            }

        }
    }
}
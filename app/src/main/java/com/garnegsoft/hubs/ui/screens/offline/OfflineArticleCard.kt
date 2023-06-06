package com.garnegsoft.hubs.ui.screens.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.common.ArticleCardStyle
import com.garnegsoft.hubs.ui.common.defaultArticleCardStyle

@Composable
fun OfflineArticleCard(
    article: OfflineArticleSnippet,
    onClick: () -> Unit,
    style: ArticleCardStyle = defaultArticleCardStyle()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(style.cardShape)
            .background(style.backgroundColor)
            .clickable(onClick = onClick)
            .padding(style.innerPadding)
    ) {
        article.authorName?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (article.authorAvatarUrl != null) {
                    AsyncImage(
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
                                color = placeholderColor(it),
                                shape = style.innerElementsShape
                            )
                            .background(Color.White, shape = style.innerElementsShape)
                            .padding(2.dp),
                        painter = painterResource(id = R.drawable.user_avatar_placeholder),
                        contentDescription = "",
                        tint = placeholderColor(it)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = it, style = style.authorTextStyle)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(text = article.title, style = style.titleTextStyle)

        var hubsText by remember { mutableStateOf("") }

        LaunchedEffect(key1 = Unit, block = {
            if (hubsText == "") {
                hubsText = article.hubs.hubsList.joinToString(separator = ", ") {
                    it.replace(" ", "\u00A0")
                }
            }
        })
        Row(
            modifier = Modifier.padding(horizontal = style.innerPadding),
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
                    tint = style.statisticsColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Перевод",
                    color = style.statisticsColor,
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
            AsyncImage(
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

    }
}
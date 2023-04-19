package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.article.Article

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HubsRow(
    hubs: List<Article.Hub>,
    onHubClicked: (alias: String) -> Unit
) {
    FlowRow() {
        hubs.forEach {
            val hubTitle =
                (if (it.isProfiled) it.title + "*" else it.title) + ", "

            Text(
                modifier = Modifier

                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onHubClicked(it.alias) }
                    .padding(horizontal = 2.dp),
                text = hubTitle,
                style = TextStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.W500
                )
            )
        }
    }

}
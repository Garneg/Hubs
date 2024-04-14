package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.data.article.Article
import com.garnegsoft.hubs.ui.theme.HubSubscribedColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HubsRow(
    hubs: List<Article.Hub>,
    onHubClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        
        hubs.forEachIndexed() { index, it ->
            val hubTitle = remember {
                (if (it.isProfiled) it.title + "*" else it.title) + if (index != hubs.lastIndex) ", " else ""
            }

            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .clickable { if (it.isCorporative) onCompanyClicked(it.alias) else onHubClicked(it.alias) },
                text = hubTitle,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = if (it.relatedData?.isSubscribed == true) HubSubscribedColor else Color.Gray,
                    fontWeight = FontWeight.W500
                )
            )
        }
    }

}
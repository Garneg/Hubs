package com.garnegsoft.hubs.ui.common.feedCards.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@Composable
fun defaultHubCardStyle(): HubCardStyle {
    return HubCardStyle(
        backgroundColor = MaterialTheme.colors.surface,
        textColor = contentColorFor(backgroundColor = MaterialTheme.colors.surface),
    )
}

@Composable
fun HubCard(
	hub: HubSnippet,
	style: HubCardStyle = defaultHubCardStyle(),
	onClick: () -> Unit,
	indicator: @Composable (hub: HubSnippet) -> Unit = {
        Text(
            text = String.format("%.1f", hub.statistics.rating).replace(',', '.'),
            fontWeight = FontWeight.W400,
            color = DefaultRatingIndicatorColor
        )
    }
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            //.height(IntrinsicSize.Max)
            .clip(style.shape)
            .background(style.backgroundColor)
            .clickable(onClick = onClick)
            .padding(style.innerPadding)
    ) {
        Row(
            verticalAlignment = if (style.showDescription) Alignment.Top else Alignment.CenterVertically
        ) {
            AsyncImage(
                model = hub.avatarUrl,
                contentDescription = "",
                modifier = Modifier
                    .size(style.avatarSize)
                    .clip(style.avatarShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.width(style.innerPadding))
            val onBackgroundColor = MaterialTheme.colors.onBackground
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text =
                    remember { buildAnnotatedString {
                        append(hub.title)
                        if (hub.isProfiled) {
                            withStyle(
                                SpanStyle(color = onBackgroundColor.copy(0.5f))
                            ) {
                                append("\u00A0*")
                            }
                        }
                    }},
                    style = style.titleTextStyle,
                    modifier = Modifier.wrapContentHeight(
                        Alignment.Top
                    )
                )
                if (style.showDescription)
                    Text(
                        text = hub.description,
                        style = style.descriptionTextStyle,
                        maxLines = style.descriptionMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
            }
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                indicator(hub)
            }

        }
    }
}


@Immutable
data class HubCardStyle(
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val titleTextStyle: TextStyle = TextStyle(
        color = textColor,
        fontWeight = FontWeight.W700,
        fontSize = 20.sp
    ),
    val descriptionTextStyle: TextStyle = TextStyle(
        color = textColor.copy(alpha = 0.5f),
        fontSize = 14.sp
    ),
    val avatarSize: Dp = 40.dp,
    val avatarShape: Shape = RoundedCornerShape(10.dp),
    val shape: Shape = RoundedCornerShape(26.dp),
    val innerPadding: Dp = 16.dp,
    val descriptionMaxLines: Int = 1,
    val showDescription: Boolean = false
)
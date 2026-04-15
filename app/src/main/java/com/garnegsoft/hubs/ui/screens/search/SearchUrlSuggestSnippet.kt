package com.garnegsoft.hubs.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.utils.SearchUrlHandler
import com.garnegsoft.hubs.api.utils.shimmerEffect
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.delay


@Composable
fun ClipboardLinkSnippet(

    data: ClipboardLinkSnippetData?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {


        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClick = onClick,
                    enabled = data != null
                )
                .background(MaterialTheme.colors.surface)
                .padding(8.dp)
                .height(IntrinsicSize.Min)
        ) {
            var showShimmer by remember {
                mutableStateOf(true)
            }
            AsyncImage(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    //.fillMaxHeight()
                    .size(64.dp)
                    .aspectRatio(1f)
                    .shimmerEffect(showShimmer),
                model = data?.imageUrl,
                contentScale = ContentScale.Crop,
                contentDescription = null,
                onSuccess = {
                    showShimmer = false
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
//            Text(
//                modifier = Modifier.align(alignment = Alignment.End),
//                style = TextStyle(
//                    color = MaterialTheme.colors.onSurface.copy(0.5f),
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.W500
//                ),
//
//                text = ""
//            )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                        .shimmerEffect(data == null, shape = RoundedCornerShape(8.dp)),
                ) {


                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = data?.title ?: "",
                        style = TextStyle(
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W600
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    modifier = Modifier
                        .widthIn(min = 100.dp)
                        .shimmerEffect(
                            enabled = data == null,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    text = data?.let {  when (data.type) {
                        SearchUrlHandler.UrlDataType.Article -> "Статья"
                        SearchUrlHandler.UrlDataType.User -> "Пользователь"
                        SearchUrlHandler.UrlDataType.Hub -> "Хаб"
                        SearchUrlHandler.UrlDataType.Company -> "Компания"
                        else -> ""
                    }} ?: "",
                    style = TextStyle(
                        color = MaterialTheme.colors.onSurface.copy(0.5f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    ),
                )
            }

        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.secondary)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Скопированная ссылка",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W500),
                color = MaterialTheme.colors.onSecondary
            )
            Icon(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(16.dp),
                painter = painterResource(R.drawable.link),
                tint = MaterialTheme.colors.onSecondary,
                contentDescription = null
            )
        }
    }
}

data class ClipboardLinkSnippetData(
    val title: String,
    val type: SearchUrlHandler.UrlDataType,
    val imageUrl: String?
)

@Preview
@Composable
private fun randomprev() {
    HubsTheme {
        var data by remember {
            mutableStateOf<ClipboardLinkSnippetData?>(null)
        }
        ClipboardLinkSnippet(
            modifier = Modifier.padding(16.dp),
            data = data,
            onClick = {}
        )
        LaunchedEffect(Unit) {
            delay(5000)
            data = ClipboardLinkSnippetData(
                "I got the information about render process in android",
                type = SearchUrlHandler.UrlDataType.Article,
                imageUrl = "https://habrastorage.org/r/w1560/getpro/habr/upload_files/b73/663/304/b73663304708ddf6818347dc1bcf565f.jpg"
            )
        }
    }

}
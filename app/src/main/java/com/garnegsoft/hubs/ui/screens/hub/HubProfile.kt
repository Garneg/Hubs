package com.garnegsoft.hubs.ui.screens.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.hub.Hub
import com.garnegsoft.hubs.api.hub.HubController
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun HubProfile(hub: Hub) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colors.surface)
                .padding(12.dp),
        ) {

            Row(
                horizontalArrangement = Arrangement.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    model = hub.avatarUrl,
                    contentDescription = ""
                )

            }
            Text(
                text = if (hub.isProfiled) hub.title + '*' else hub.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
                text = hub.description,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = hub.statistics.rating.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.Magenta
                    )
                    Text(text = "Рейтинг", color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
                }
            }
            hub.relatedData?.let{
                var subscribed by rememberSaveable {
                    mutableStateOf(it.isSubscribed)
                }
                val subscriptionCoroutineScope = rememberCoroutineScope()
                Box(modifier = Modifier
                    .padding(8.dp)
                    .height(45.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (subscribed) Color(0xFF4CB025) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(10.dp),
                        color = if (subscribed) Color.Transparent else Color(0xFF4CB025)
                    )
                    .clickable {
                        subscriptionCoroutineScope.launch(Dispatchers.IO) {
                            subscribed = !subscribed
                            subscribed = HubController.subscription(hub.alias)
                        }
                    }
                ){
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = if (subscribed) "Вы подписаны" else "Подписаться",
                        color = if (subscribed) Color.White else Color(0xFF4CB025)
                    )
                }
            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colors.surface)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Статистика", fontSize = 20.sp, fontWeight = FontWeight.W500)
            }
            Divider(modifier = Modifier.padding(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Постов", modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.weight(1f),
                    text = hub.statistics.postsCount.toString(),
                    textAlign = TextAlign.Right
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Авторов", modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.weight(1f),
                    text = hub.statistics.authorsCount.toString(),
                    textAlign = TextAlign.Right
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Подписчиков")
                Text(
                    modifier = Modifier.weight(1f),
                    text = hub.statistics.subscribersCount.toString(),
                    textAlign = TextAlign.Right
                )
            }
        }


    }

}
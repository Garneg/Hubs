package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.user.User
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal fun UserProfile(
    user: User,
    isAppUser: Boolean,
    onUserLogout: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (user.avatarUrl != null) {
                        AsyncImage(
                            model = user.avatarUrl,
                            modifier = Modifier
                                .size(75.dp)
                                .align(Alignment.Center)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            contentDescription = ""
                        )
                    } else {
                        Icon(
                            modifier = Modifier
                                .size(75.dp)
                                .border(
                                    width = 4.dp,
                                    color = placeholderColor(user.alias),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .align(Alignment.Center)
                                .padding(5.dp),
                            painter = painterResource(id = R.drawable.user_avatar_placeholder),
                            contentDescription = "",
                            tint = placeholderColor(user.alias)
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (user.fullname != null)
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "${user.fullname}\n@${user.alias}",
                            fontWeight = FontWeight.W700,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )
                    else
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "@${user.alias}",
                            fontWeight = FontWeight.W700,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )
                }
                if (user.isReadonly)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Read Only",
                            fontWeight = FontWeight.W400,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                if (user.speciality != null)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 8.dp,
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = user.speciality,
                            fontWeight = FontWeight.W500,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
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
                            text = user.score.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W600,
                            color =
                            if (user.score > 0)
                                RatingPositive
                            else
                                if (user.score == 0)
                                    Color.Black
                                else
                                    RatingNegative
                        )
                        Text(text = "Карма", color = Color.Gray)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.rating.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W600,
                            color = Color.Magenta
                        )
                        Text(text = "Рейтинг", color = Color.Gray)
                    }
                }
                if (!isAppUser) {
                    user.relatedData?.let {
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
                                    subscribed = UserController.subscription(user.alias)
                                }
                            }
                        ) {
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = if (subscribed) "Вы подписаны" else "Подписаться",
                                color = if (subscribed) Color.White else Color(0xFF4CB025)
                            )
                        }
                    }
                }

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Информация", fontSize = 20.sp, fontWeight = FontWeight.W500)
                }
                Divider(modifier = Modifier.padding(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Место в рейтинге", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = if (user.ratingPosition == null) "Не участвует" else user.ratingPosition.toString() + "-й",
                        textAlign = TextAlign.End
                    )
                }
                if (user.location != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Откуда", modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = user.location,
                            textAlign = TextAlign.End
                        )
                    }
                }

                if (user.birthday != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Дата рождения", modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = user.birthday,
                            textAlign = TextAlign.End
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Дата регистрации", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = user.registrationDate,
                        textAlign = TextAlign.End
                    )
                }
                if (user.lastActivityDate != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Активность")
                        Text(
                            modifier = Modifier.weight(1f),
                            text = user.lastActivityDate,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
        if (isAppUser){
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp).clickable(onClick = onUserLogout!!),
                elevation = 0.dp,
                shape = RoundedCornerShape(26.dp),
                backgroundColor = MaterialTheme.colors.surface,
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Выйти",
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

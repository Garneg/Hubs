package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive

@Composable
internal fun UserProfilePage(
    user: User
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
                        textAlign = TextAlign.Right
                    )
                }
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                Text(text = "Откуда")
//                Text(
//                    modifier = Modifier.align(Alignment.CenterEnd),
//                    text = "Москва, Россия"
//                )
//            }
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
                            textAlign = TextAlign.Right
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
                        textAlign = TextAlign.Right
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
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

    }
}

package com.garnegsoft.hubs.ui.screens.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.theme.RatingNegative
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserProfile(
    user: User,
    isAppUser: Boolean,
    onUserLogout: (() -> Unit)? = null,
    onHubClick: (alias: String) -> Unit,
    viewModel: UserScreenViewModel
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
                    .background(MaterialTheme.colors.surface)
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
                                .size(65.dp)
                                .align(Alignment.Center)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                                .background(Color.White),
                            contentDescription = ""
                        )
                    } else {
                        Icon(
                            modifier = Modifier
                                .size(65.dp)
                                .background(Color.White, shape = RoundedCornerShape(12.dp))
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
                            color = MaterialTheme.colors.onSurface.copy(0.2f),
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
                            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled),
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
                                    MaterialTheme.colors.onSurface
                                else
                                    RatingNegative
                        )
                        Text(
                            text = "Карма",
                            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                        )
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
                        Text(
                            text = "Рейтинг",
                            color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                        )
                    }
                }
                if (!isAppUser && !user.isReadonly) {
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
            val note by viewModel.note.observeAsState()
            if (!isAppUser && note?.text != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(MaterialTheme.colors.surface)
                        .padding(8.dp)
                ) {
                    BasicTitledColumn(
                        title = {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "Заметка", style = MaterialTheme.typography.subtitle1
                            )
                        },
                        divider = {
            //                        Divider()
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    bottom = 12.dp
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    MaterialTheme.colors.onSurface.copy(0.05f)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                color = MaterialTheme.colors.onSurface.copy(0.75f),
                                text = note?.text ?: "")
                        }
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
                BasicTitledColumn(title = {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = "Информация", style = MaterialTheme.typography.subtitle1
                    )
                }, divider = {
//                    Divider()
                }) {
                    Column(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 12.dp,
                            bottom = 12.dp,
                            top = 4.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TitledColumn(
                            title = "Место в рейтинге"
                        ) {
                            Text(
                                text = if (user.ratingPosition == null) "Не участвует" else user.ratingPosition.toString() + "-й",
                            )
                        }

                        if (user.location != null) {
                            TitledColumn(title = "Откуда") {
                                Text(
                                    text = user.location,
                                )
                            }
                        }

                        if (user.birthday != null) {

                            TitledColumn(title = "Дата рождения") {
                                Text(
                                    text = user.birthday,
                                )
                            }
                        }

                        TitledColumn(title = "Дата регистрации") {
                            Text(
                                text = user.registrationDate,
                            )
                        }

                        if (user.lastActivityDate != null) {

                            TitledColumn(title = "Активность") {
                                Text(
                                    text = user.lastActivityDate
                                )
                            }
                        }
                        val hubs by viewModel.subscribedHubs.observeAsState()
                        hubs?.let {
                            if (it.list.size > 0) {
                                Column() {
                                    TitledColumn(title = "Состоит в хабах") {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            it.list.forEach {
                                                HubChip(hub = it) {
                                                    onHubClick(it.alias)
                                                }
                                            }
                                        }
                                    }
                                    if (viewModel.moreHubsAvailable) {
                                        TextButton(
                                            onClick = {
                                                viewModel.loadSubscribedHubs()
                                            }
                                        ) {
                                            Text(
                                                "Показать ещё",
                                                color = MaterialTheme.colors.secondary,
                                                letterSpacing = 0.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }


                    }
                }
            }
            if (isAppUser) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .clickable(onClick = onUserLogout!!),
                    elevation = 0.dp,
                    shape = RoundedCornerShape(26.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
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
}

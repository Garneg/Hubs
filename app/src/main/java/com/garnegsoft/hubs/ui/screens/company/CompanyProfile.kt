package com.garnegsoft.hubs.ui.screens.company

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.utils.placeholderColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CompanyProfile(
    company: Company
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
                    if (company.avatarUrl != null) {
                        AsyncImage(
                            model = company.avatarUrl,
                            modifier = Modifier
                                .size(55.dp)
                                .align(Alignment.Center)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            contentDescription = ""
                        )
                    } else {
                        Icon(
                            modifier = Modifier
                                .size(55.dp)
                                .border(
                                    width = 4.dp,
                                    color = placeholderColor(company.alias),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .align(Alignment.Center)
                                .padding(5.dp),
                            painter = painterResource(id = R.drawable.company_avatar_placeholder),
                            contentDescription = "",
                            tint = placeholderColor(company.alias)
                        )
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = company.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Center
                )
                Box(modifier = Modifier.fillMaxWidth()) {

                }
                if (company.description != null)
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
                            text = company.description,
                            fontWeight = FontWeight.W500,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                company.relatedData?.let{
                    var isSubscribed by rememberSaveable {
                        mutableStateOf(it.isSubscribed)
                    }
                    val subscriptionScope = rememberCoroutineScope()
                    Box(modifier = Modifier
                        .padding(8.dp)
                        .height(45.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSubscribed) Color(0xFF4CB025) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSubscribed) Color.Transparent else Color(0xFF4CB025)
                        )
                        .clickable {
                            subscriptionScope.launch(Dispatchers.IO) {
                                isSubscribed = !isSubscribed
                                isSubscribed = CompanyController.subscription(company.alias)
                            }
                        }
                    ){
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = if (isSubscribed) "Вы подписаны" else "Подписаться",
                            color = if (isSubscribed) Color.White else Color(0xFF4CB025)
                        )
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
                    Text(text = "Рейтинг", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.statistics.rating.toString(),
                        textAlign = TextAlign.Right
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Дата регистрации", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.registrationDate,
                        textAlign = TextAlign.Right
                    )
                }
                if (company.foundationDate != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Дата основания", modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = company.foundationDate,
                            textAlign = TextAlign.Right
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Численность", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.staffNumber,
                        textAlign = TextAlign.Right
                    )
                }
                company.location?.let{
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Местоположение", modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = company.location,
                            textAlign = TextAlign.Right
                        )
                    }
                }

                if (company.siteUrl != null) {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 9.dp,
                                    topEnd = 9.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 18.dp
                                )
                            )
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(company.siteUrl)
                                    )
                                )
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Перейти на сайт")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "")
                    }
                }
            }
        }

    }
}
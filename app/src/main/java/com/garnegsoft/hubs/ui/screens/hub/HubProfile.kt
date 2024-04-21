package com.garnegsoft.hubs.ui.screens.hub

import android.telephony.SubscriptionInfo
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.data.hub.Hub
import com.garnegsoft.hubs.data.hub.HubController
import com.garnegsoft.hubs.data.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import com.garnegsoft.hubs.ui.common.RefreshableContainer
import com.garnegsoft.hubs.ui.common.SubscriptionButton
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun HubProfile(
	hub: Hub,
	scrollState: ScrollState
) {
	val viewModel = viewModel { HubScreenViewModel(hub.alias) }
	val refreshing by viewModel.isRefreshing.observeAsState(false)
	RefreshableContainer(onRefresh = viewModel::refresh, refreshing = refreshing) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
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
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 12.dp)
				) {
					AsyncImage(
						modifier = Modifier
							.padding(top = 12.dp)
							.size(65.dp)
							.clip(RoundedCornerShape(12.dp))
							.background(Color.White),
						model = hub.avatarUrl,
						contentDescription = ""
					)
					
				}
				Text(
					text = buildAnnotatedString {
						append(hub.title)
						if (hub.isProfiled) {
							withStyle(
								SpanStyle(color = MaterialTheme.colors.onBackground.copy(0.5f))
							) {
								append(" *")
							}
						}
					},
					fontSize = 20.sp,
					fontWeight = FontWeight.W700,
					modifier = Modifier.fillMaxWidth(),
					textAlign = TextAlign.Center
				)
				Text(
					color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium),
					text = hub.description,
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp),
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
							color = DefaultRatingIndicatorColor
						)
						Text(
							text = "Рейтинг",
							color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)
						)
					}
				}
				hub.relatedData?.let {
					var subscribed by rememberSaveable {
						mutableStateOf(it.isSubscribed)
					}
					val subscriptionCoroutineScope = rememberCoroutineScope()
					SubscriptionButton(onClick = {
						subscriptionCoroutineScope.launch(Dispatchers.IO) {
							subscribed = !subscribed
							subscribed = HubController.subscription(hub.alias)
						}
					}, subscribed = subscribed)
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
						text = "Статистика", style = MaterialTheme.typography.subtitle1
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
						verticalArrangement = Arrangement.spacedBy(20.dp)
					) {
						TitledColumn(title = "Постов") {
							Text(
								text = formatLongNumbers(hub.statistics.postsCount),
							)
						}
						
						TitledColumn(title = "Авторов") {
							Text(
								text = formatLongNumbers(hub.statistics.authorsCount),
							)
						}
						
						TitledColumn(title = "Подписчиков") {
							Text(
								text = formatLongNumbers(hub.statistics.subscribersCount),
							)
						}
					}
					
					
				}
			}
			
			
		}
	}
	
}
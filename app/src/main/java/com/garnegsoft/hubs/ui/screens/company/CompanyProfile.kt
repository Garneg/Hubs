package com.garnegsoft.hubs.ui.screens.company

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import com.garnegsoft.hubs.ui.common.RefreshableContainer
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.RenderHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CompanyProfile(
	company: Company,
	scrollState: ScrollState
) {
	val context = LocalContext.current
	val viewModel = viewModel { CompanyScreenViewModel(company.alias) }
	val refreshing by viewModel.isRefreshing.observeAsState(false)
	RefreshableContainer(onRefresh = viewModel::refreshCompany, refreshing = refreshing) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
		) {
			Column(
				modifier = Modifier.padding(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				company.branding?.bannerImageUrl?.let {
					AsyncImage(
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(4.3f) // aspect ratio of banners (1320 / 300)
							.clip(RoundedCornerShape(26.dp))
							.background(MaterialTheme.colors.onBackground.copy(0.1f))
							.clickable(
								enabled = company.branding.bannerLinkUrl != null
							) {
								context.startActivity(
									Intent(
										Intent.ACTION_VIEW,
										Uri.parse(company.branding.bannerLinkUrl!!)
									)
								)
							},
						model = it,
						contentScale = ContentScale.Crop,
						contentDescription = "${company.title} branding banner"
					)
				}
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
						if (company.avatarUrl != null) {
							AsyncImage(
								model = company.avatarUrl,
								modifier = Modifier
									.size(65.dp)
									.align(Alignment.Center)
									.clip(
										RoundedCornerShape(12.dp)
									)
									.background(
										color = Color.White
									),
								
								
								contentDescription = ""
							)
						} else {
							Icon(
								modifier = Modifier
									.size(65.dp)
									.background(
										color = Color.White,
										shape = RoundedCornerShape(12.dp)
									)
									.border(
										width = 4.dp,
										color = placeholderColorLegacy(company.alias),
										shape = RoundedCornerShape(12.dp)
									)
									.align(Alignment.Center)
									.padding(5.dp),
								painter = painterResource(id = R.drawable.company_avatar_placeholder),
								contentDescription = "",
								tint = placeholderColorLegacy(company.alias)
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
								color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled),
								textAlign = TextAlign.Center
							)
						}
					company.relatedData?.let {
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
								color = if (isSubscribed) Color.Transparent else Color(
									0xFF4CB025
								)
							)
							.clickable {
								subscriptionScope.launch(Dispatchers.IO) {
									isSubscribed = !isSubscribed
									isSubscribed = CompanyController.subscription(company.alias)
								}
							}
						) {
							Text(
								modifier = Modifier.align(Alignment.Center),
								text = if (isSubscribed) "Вы подписаны" else "Подписаться",
								color = if (isSubscribed) Color.White else Color(0xFF4CB025)
							)
						}
					}
				}
				val whoIs by viewModel.companyWhoIs.observeAsState()
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
							text = "Описание", style = MaterialTheme.typography.subtitle1
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
							whoIs?.aboutHtml?.let {
								if (it.isNotBlank()) {
									TitledColumn(title = "О Компании") {
										RenderHtml(html = it, elementSettings = remember {
											ElementSettings(
												fontSize = 16.sp,
												lineHeight = 16.sp,
												fitScreenWidth = false
											)
										})
									}
								}
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
					BasicTitledColumn(
						title = {
							Text(
								modifier = Modifier.padding(12.dp),
								text = "Информация", style = MaterialTheme.typography.subtitle1
							)
						},
						divider = { }
					) {
						
						Column(
							modifier = Modifier.padding(
								top = 4.dp,
								bottom = 12.dp,
								start = 12.dp,
								end = 12.dp
							),
							verticalArrangement = Arrangement.spacedBy(20.dp)
						) {
							
							TitledColumn(title = "Рейтинг") {
								Text(
									text = company.statistics.rating.toString(),
								)
							}
							TitledColumn(title = "Дата регистрации") {
								Text(
									text = company.registrationDate,
								)
							}
							if (company.foundationDate != null) {
								TitledColumn(title = "Дата основания") {
									Text(
										text = company.foundationDate,
									)
								}
							}
							
							TitledColumn(title = "Численность") {
								Text(
									text = company.staffNumber,
								)
							}
							
							company.location?.let {
								TitledColumn(title = "Местоположение") {
									Text(
										text = company.location
									)
								}
							}
						}
					}
					
				}
				if (company.siteUrl != null) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clip(
								RoundedCornerShape(26.dp)
							)
							.background(MaterialTheme.colors.surface)
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
						Text(text = "Перейти на сайт", color = MaterialTheme.colors.onSurface)
						Spacer(modifier = Modifier.width(4.dp))
						Icon(
							imageVector = Icons.Outlined.ArrowForward,
							contentDescription = "",
							tint = MaterialTheme.colors.onSurface
						)
					}
				}
			}
			
		}
	}
}
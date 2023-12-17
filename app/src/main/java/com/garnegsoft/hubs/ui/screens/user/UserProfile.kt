package com.garnegsoft.hubs.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy
import com.garnegsoft.hubs.ui.common.AsyncSvgImage
import com.garnegsoft.hubs.ui.common.BasicTitledColumn
import com.garnegsoft.hubs.ui.common.HubChip
import com.garnegsoft.hubs.ui.common.RefreshableContainer
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.RenderHtml
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserProfile(
	isAppUser: Boolean,
	onUserLogout: (() -> Unit)? = null,
	onHubClick: (alias: String) -> Unit,
	onWorkPlaceClick: (alias: String) -> Unit,
	scrollState: ScrollState,
	viewModel: UserScreenViewModel
) {
	val userState by viewModel.user.observeAsState()
	val isRefreshing by viewModel.isRefreshingUser.observeAsState(false)
	RefreshableContainer(onRefresh = viewModel::refreshUser, refreshing = isRefreshing) {
		userState?.let { user ->
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(scrollState)
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
											color = placeholderColorLegacy(user.alias),
											shape = RoundedCornerShape(12.dp)
										)
										.align(Alignment.Center)
										.padding(5.dp),
									painter = painterResource(id = R.drawable.user_avatar_placeholder),
									contentDescription = "",
									tint = placeholderColorLegacy(user.alias)
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
										RatingPositiveColor
									else
										if (user.score == 0)
											MaterialTheme.colors.onSurface
										else
											RatingNegativeColor
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
									color = DefaultRatingIndicatorColor
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
										color = if (subscribed) Color.Transparent else Color(
											0xFF4CB025
										)
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
								divider = {  }
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
											MaterialTheme.colors.onSurface.copy(0.04f)
										)
										.padding(8.dp)
								) {
									Text(
										color = MaterialTheme.colors.onSurface.copy(0.75f),
										text = note?.text ?: ""
									)
								}
							}
						}
					}
					val whoIs by viewModel.whoIs.observeAsState()
					val hubs by viewModel.subscribedHubs.observeAsState()
					
					if ((whoIs != null && (!whoIs?.aboutHtml.isNullOrBlank() || whoIs!!.badges.isNotEmpty()
						|| whoIs!!.invite != null || whoIs!!.contacts.isNotEmpty()))
						|| !hubs?.list.isNullOrEmpty()
					) {
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
									text = "Описание",
									style = MaterialTheme.typography.subtitle1
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
									whoIs?.let { whoIs ->
										whoIs.badges.let {
											TitledColumn(title = "Значки") {
												FlowRow(
													horizontalArrangement = Arrangement.spacedBy(8.dp),
													verticalArrangement = Arrangement.spacedBy(8.dp)
												) {
													it.forEach { Badge(title = it.title) }
												}
											}
										}
										
										whoIs.aboutHtml?.let {
											if (it.isNotBlank()) {
												TitledColumn(title = "О Себе") {
													RenderHtml(
														html = it,
														elementSettings = remember {
															ElementSettings(
																fontSize = 16.sp,
																lineHeight = 16.sp,
																fitScreenWidth = false
															)
														})
												}
											}
										}
										
										whoIs.invite?.let {
											TitledColumn(title = "Приглашен") {
												Text(text = "${it.inviteDate} по приглашению от ${it.inviterAlias ?: "НЛО"}")
											}
										}
										
										whoIs.let {
											if (it.contacts.size > 0) {
												TitledColumn(title = "Контакты") {
													Column(
														verticalArrangement = Arrangement.spacedBy(4.dp)
													) {
														val context = LocalContext.current
														it.contacts.forEach {
															Row(
																modifier = Modifier
																	.fillMaxWidth()
																	.clip(RoundedCornerShape(8.dp))
																	.background(
																		MaterialTheme.colors.onSurface.copy(
																			0.04f
																		)
																	)
																	.clickable {
																		context.startActivity(
																			Intent(
																				Intent.ACTION_VIEW,
																				Uri.parse(it.url)
																			)
																		)
																	}
																	.padding(12.dp),
																verticalAlignment = Alignment.CenterVertically
															) {
																if (it.faviconUrl != null && it.faviconUrl.isNotBlank()) {
																	AsyncSvgImage(
																		modifier = Modifier
																			.size(24.dp)
																			.clip(
																				RoundedCornerShape(4.dp)
																			)
																			.background(if (MaterialTheme.colors.isLight) Color.Transparent else MaterialTheme.colors.onSurface),
																		data = it.faviconUrl,
																		revertColorsOnDarkTheme = false,
																		contentDescription = it.title,
																		contentScale = ContentScale.Fit
																	)
																} else {
																	Icon(
																		modifier = Modifier.size(24.dp),
																		painter = painterResource(id = R.drawable.website_favicon_placeholder),
																		contentDescription = "website",
																		tint = MaterialTheme.colors.onSurface.copy(
																			0.4f
																		)
																	)
																}
																
																Spacer(modifier = Modifier.width(12.dp))
																
																Text(it.title)
															}
														}
													}
												}
											}
										}
										
										
									}
									hubs?.let {
										if (it.list.size > 0) {
											Column() {
												TitledColumn(title = "Состоит в хабах") {
													FlowRow(
														horizontalArrangement = Arrangement.spacedBy(
															8.dp
														)
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
								verticalArrangement = Arrangement.spacedBy(20.dp)
							) {
								TitledColumn(
									title = "Место в рейтинге"
								) {
									Text(
										text = if (user.ratingPosition == null) "Не участвует" else user.ratingPosition.toString() + "-й",
									)
								}
								
								user.location?.let {
									TitledColumn(title = "Откуда") {
										Text(
											text = it,
										)
									}
								}
								
								if (user.workPlaces.size > 0) {
									TitledColumn(title = "Работает в") {
										Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
											user.workPlaces.forEach {
												Row(
													modifier = Modifier
														.fillMaxWidth()
														.clip(RoundedCornerShape(8.dp))
														.background(
															MaterialTheme.colors.onSurface.copy(
																0.04f
															)
														)
														.clickable {
															onWorkPlaceClick(it.alias)
														}
														.padding(12.dp),
													verticalAlignment = Alignment.CenterVertically
												) {
													Text(it.title)
												}
											}
										}
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
	}
	if (userState == null) {
		Box(modifier = Modifier.fillMaxSize()) {
			CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
		}
	}
	
}

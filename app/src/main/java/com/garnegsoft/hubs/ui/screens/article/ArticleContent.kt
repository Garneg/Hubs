package com.garnegsoft.hubs.ui.screens.article

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.data.EditorVersion
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.data.PublicationComplexity
import com.garnegsoft.hubs.data.PostType
import com.garnegsoft.hubs.data.article.Article
import com.garnegsoft.hubs.data.company.CompanyController
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.common.HubChip
import com.garnegsoft.hubs.ui.theme.TranslationLabelColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleContent(
	article: Article,
	scrollState: LazyListState = rememberLazyListState(),
	contentPadding: PaddingValues = PaddingValues(16.dp),
	onAuthorClicked: () -> Unit,
	onHubClicked: (alias: String) -> Unit,
	onCompanyClick: (alias: String) -> Unit,
	onArticleClick: (id: Int) -> Unit,
	onViewImageRequest: (url: String) -> Unit
) {
	val context = LocalContext.current
	
	val viewModel = viewModel<ArticleScreenViewModel>()
	val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
	val mostReadingArticles by viewModel.mostReadingArticles.observeAsState()
	
	Box() {
		val contentNodes by viewModel.parsedArticleContent.observeAsState()
		val fontSize by HubsDataStore.Settings
			.getValueFlow(context, HubsDataStore.Settings.ArticleScreen.FontSize)
			.collectAsState(
				initial = null
			)
		
		val color = MaterialTheme.colors.onSurface
		val spanStyle = remember(fontSize, color) {
			SpanStyle(
				color = color,
				fontSize = fontSize?.sp ?: 16.sp
			)
		}
		val elementsSettings = remember {
			ElementSettings(
				fontSize = fontSize?.sp ?: 16.sp,
				lineHeight = 16.sp,
				fitScreenWidth = false
			)
		}
		
		val updatedPolls by viewModel.updatedPolls.observeAsState()
		
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			state = scrollState,
			contentPadding = contentPadding
		) {
			if (article.editorVersion == EditorVersion.FirstVersion) {
				item {
					DisableSelection {
						
						
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(8.dp))
								.background(MaterialTheme.colors.error.copy(alpha = 0.75f))
								.padding(8.dp), verticalAlignment = Alignment.CenterVertically
						) {
							Icon(
								imageVector = Icons.Outlined.Warning,
								contentDescription = "",
								tint = MaterialTheme.colors.onError
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text(
								"Эта статья написана с помощью первой версии редактора, некоторые элементы могут отображаться некорректно",
								color = MaterialTheme.colors.onError
							)
						}
						Spacer(modifier = Modifier.height(16.dp))
					}
				}
			}
			
			if (article.postType == PostType.Megaproject && article.metadata != null) {
				item {
					AsyncImage(
						model = article.metadata.mainImageUrl,
						contentDescription = "",
						modifier = Modifier
							.fillMaxWidth()
							.clip(RoundedCornerShape(8.dp)),
					)
					Spacer(Modifier.height(8.dp))
				}
			}
			
			if (article.isCorporative) {
				item {
					DisableSelection {
						val companyAlias = article.hubs.find { it.isCorporative }!!.alias
						
						var companyTitle: String? by rememberSaveable {
							mutableStateOf(null)
						}
						var companyAvatarUrl: String? by rememberSaveable {
							mutableStateOf(null)
						}
						LaunchedEffect(key1 = Unit, block = {
							if (companyTitle == null) {
								launch(Dispatchers.IO) {
									CompanyController.get(companyAlias)?.let {
										companyAvatarUrl = it.avatarUrl
										companyTitle = it.title
									}
								}
							}
						})
						
						Row(
							modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.clickable(onClick = { onCompanyClick(companyAlias) }),
							verticalAlignment = Alignment.CenterVertically,
						) {
							if (companyTitle != null) {
								AsyncImage(
									modifier = Modifier
										.size(38.dp)
										.clip(RoundedCornerShape(8.dp))
										.background(Color.White),
									model = companyAvatarUrl,
									contentDescription = ""
								)
								Spacer(modifier = Modifier.width(4.dp))
								Text(
									modifier = Modifier.fillMaxWidth(),
									text = companyTitle!!, fontWeight = FontWeight.W600,
									fontSize = 14.sp,
									color = MaterialTheme.colors.onBackground
								)
							} else {
								Box(modifier = Modifier.size(34.dp))
							}
							
							
						}
						Spacer(modifier = Modifier.height(8.dp))
					}
				}
			}
			
			if (article.author != null && article.postType != PostType.Megaproject) {
				item {
					DisableSelection {
						
						Row(
							modifier = Modifier
								.clip(RoundedCornerShape(8.dp))
								.clickable(onClick = { onAuthorClicked() }),
							verticalAlignment = Alignment.CenterVertically,
						) {
							if (article.author.avatarUrl != null) {
								AsyncImage(
									modifier = Modifier
										.size(38.dp)
										.clip(RoundedCornerShape(8.dp))
										.background(Color.White),
									model = article.author.avatarUrl,
									contentDescription = ""
								)
							}
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = article.author.alias, fontWeight = FontWeight.W600,
								fontSize = 14.sp,
								color = MaterialTheme.colors.onBackground
							)
							Spacer(modifier = Modifier.weight(1f))
							
						}
						Spacer(modifier = Modifier.height(8.dp))
					}
				}
			}
			
			item {
				
					Text(
						text = article.title,
						fontSize = ((fontSize ?: 16f) + 4f).sp,
						fontWeight = FontWeight.W700,
						color = MaterialTheme.colors.onBackground
					)
				
				DisableSelection {
					Spacer(modifier = Modifier.height(4.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Start
					) {
						
						Text(
							text = article.timePublished,
							color = MaterialTheme.colors.onBackground.copy(0.6f),
							fontSize = 14.sp,
							fontWeight = FontWeight.W500
						)
						
					}
					
					Spacer(Modifier.height(4.dp))
					
					Row(
						verticalAlignment = Alignment.CenterVertically,
					) {
						if (article.complexity != PublicationComplexity.None) {
							Icon(
								modifier = Modifier.size(height = 10.dp, width = 20.dp),
								painter = painterResource(id = R.drawable.speedmeter_hard),
								contentDescription = "",
								tint = when (article.complexity) {
									PublicationComplexity.Low -> Color(0xFF4CBE51)
									PublicationComplexity.Medium -> Color(0xFFEEBC25)
									PublicationComplexity.High -> Color(0xFFEB3B2E)
									else -> Color.Red
								}
							)
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = when (article.complexity) {
									PublicationComplexity.Low -> "Простой"
									PublicationComplexity.Medium -> "Средний"
									PublicationComplexity.High -> "Сложный"
									else -> ""
								},
								color = when (article.complexity) {
									PublicationComplexity.Low -> Color(0xFF4CBE51)
									PublicationComplexity.Medium -> Color(0xFFEEBC25)
									PublicationComplexity.High -> Color(0xFFEB3B2E)
									else -> Color.Red
								},
								fontWeight = FontWeight.W500,
								fontSize = 14.sp
							
							)
							
							Spacer(modifier = Modifier.width(12.dp))
						}
						Icon(
							painter = painterResource(id = R.drawable.clock_icon),
							modifier = Modifier.size(14.dp),
							contentDescription = "",
							tint = statisticsColor
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "${article.readingTime} мин",
							color = statisticsColor,
							fontWeight = FontWeight.W500,
							fontSize = 14.sp
						)
						Spacer(modifier = Modifier.width(12.dp))
						if (article.translationData.isTranslation) {
							Icon(
								painter = painterResource(id = R.drawable.translation),
								modifier = Modifier.size(14.dp),
								contentDescription = "",
								tint = TranslationLabelColor
							)
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = "Перевод",
								color = TranslationLabelColor,
								fontWeight = FontWeight.W500,
								fontSize = 14.sp
							)
						}
						
					}
					Spacer(Modifier.height(4.dp))
					
					HubsRow(
						hubs = article.hubs,
						onHubClicked = onHubClicked,
						onCompanyClicked = onCompanyClick
					)
					
					TranslationMessage(
						modifier = Modifier.padding(vertical = 8.dp),
						translationInfo = article.translationData
					) {
						val intent = Intent(
							Intent.ACTION_VIEW,
							Uri.parse(article.translationData.originUrl)
						)
						context.startActivity(intent)
					}
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
			contentNodes?.let {
				items(items = it) {
					it?.invoke(spanStyle, elementsSettings)
				}
				
				if (article.polls.size > 0) {
					item {
						Divider(modifier = Modifier.padding(vertical = 24.dp))
					}
				}
				itemsIndexed(items = article.polls) { index, originalPoll ->
					
					if (index > 0) {
						Spacer(modifier = Modifier.height(48.dp))
					}
					val poll =
						updatedPolls?.find { originalPoll.id == it.id } ?: originalPoll
					Poll(
						poll = poll,
						onVote = { variants ->
							viewModel.vote(poll.id, variants)
						},
						onPass = {})
				}
				item {
					DisableSelection {
						Divider(modifier = Modifier.padding(vertical = 24.dp))
						// Hubs
						TitledColumn(
							title = "Хабы",
							titleStyle = MaterialTheme.typography.subtitle2.copy(
								color = MaterialTheme.colors.onBackground.copy(
									0.5f
								)
							)
						) {
							FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								article.hubs.forEach {
									HubChip(
										title = if (it.isProfiled) it.title + "*" else it.title
									) {
										if (it.isCorporative)
											onCompanyClick(it.alias)
										else
											onHubClicked(it.alias)
									}
								}
							}
						}
					}
				}
				
				article.author?.let { author ->
					item {
						Divider(modifier = Modifier.padding(vertical = 24.dp))
						DisableSelection {
							TitledColumn(
								title = "Автор",
								titleStyle = MaterialTheme.typography.subtitle2.copy(
									color = MaterialTheme.colors.onBackground.copy(
										0.5f
									)
								)
							) {
								
								ArticleAuthorElement(
									onClick = { onAuthorClicked() },
									userAvatarUrl = author.avatarUrl!!,
									fullName = author.fullname,
									alias = author.alias
								)
							}
						}
					}
				}
				
				
				item {
					if (viewModel.mostReadingArticles.isInitialized) {
						Divider(modifier = Modifier.padding(vertical = 24.dp))
						TitledColumn(
							title = "Читают сейчас",
							titleStyle = MaterialTheme.typography.subtitle2.copy(
								color = MaterialTheme.colors.onBackground.copy(
									0.5f
								)
							),
							verticalArrangement = Arrangement.spacedBy(8.dp)
						) {
							var readMoreMode by remember { mutableStateOf(ReadMoreMode.MostReading) }

//                        Row(
//                            modifier = Modifier.horizontalScroll(rememberScrollState()),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.MostReading,
//                                onClick = { readMoreMode = ReadMoreMode.MostReading }) {
//                                Text(text = "Читают сейчас")
//                            }
//                            if (article.isCorporative) {
//                                HubsFilterChip(
//                                    selected = readMoreMode == ReadMoreMode.Blog,
//                                    onClick = { readMoreMode = ReadMoreMode.Blog }) {
//                                    Text(text = "Блог")
//                                }
//                            }
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.News,
//                                onClick = { readMoreMode = ReadMoreMode.News }) {
//                                Text(text = "Новости")
//                            }
//                            HubsFilterChip(
//                                selected = readMoreMode == ReadMoreMode.Similar,
//                                onClick = { readMoreMode = ReadMoreMode.Similar }) {
//                                Text(text = "Похожие")
//                            }
//
//                        }
						
						
						}
					}
				}
				mostReadingArticles?.let { list ->
					
					itemsIndexed(list) { index, it ->
						DisableSelection {
							Column {
								ArticleShort(
									article = it,
									onClick = {
										onArticleClick(it.id)
									}
								)
								if (index != list.lastIndex) {
									Spacer(modifier = Modifier.height(8.dp))
								}
							}
						}
					}
				}
			}
		}
		
		ScrollBar(modifier = Modifier.align(Alignment.CenterEnd), lazyListState = scrollState)
		
	}
	
}

enum class ReadMoreMode {
	MostReading,
	News,
	Similar,
	Blog
}

@Composable
fun ScrollBar(
	modifier: Modifier,
	lazyListState: LazyListState,
	color: Color = MaterialTheme.colors.onBackground.copy(0.25f)
) {
	val scrollBarAlpha by animateFloatAsState(
		targetValue = if (lazyListState.isScrollInProgress) 1f else 0f,
		tween(600)
	)
	val draw by remember { derivedStateOf { lazyListState.layoutInfo.totalItemsCount > 0 } }
	if (draw) {
		Canvas(modifier = modifier
			.width(4.dp)
			.fillMaxHeight(), onDraw = {
			val topLeft = Offset(
				0f,
				(this.size.height * lazyListState.firstVisibleItemIndex / lazyListState.layoutInfo.totalItemsCount)
			)
			val barHeight =
				this.size.height.roundToInt() / (lazyListState.layoutInfo.totalItemsCount / lazyListState.layoutInfo.visibleItemsInfo.size).toFloat()
			drawRoundRect(
				color = color,
				topLeft = topLeft,
				alpha = if (lazyListState.isScrollInProgress) 1f else scrollBarAlpha,
				size = Size(width = 3f * density, height = barHeight),
				cornerRadius = CornerRadius(400f, 400f)
			)
			
		})
	}
}

//object : FlingBehavior {
//    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
//        if (abs(initialVelocity) <= 1f)
//            return initialVelocity
//
//        val performedInitialVelocity = initialVelocity * 1.2f
//
//        var velocityLeft = performedInitialVelocity
//        var lastValue = 0f
//        AnimationState(
//            initialValue = 0f,
//            initialVelocity = performedInitialVelocity
//        ).animateDecay(splineBasedDecay(Density(3f))) {
//            val delta = value - lastValue
//            val consumed = scrollBy(delta)
//            lastValue = value
//            velocityLeft = velocity
//
//            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
//
//        }
//        return velocityLeft
//
//    }
//
//}
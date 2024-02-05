package com.garnegsoft.hubs.ui.screens.offline

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.AsyncGifImage
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.ui.common.HubChip
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.ScrollBar
import com.garnegsoft.hubs.ui.screens.article.parseChildElements
import org.jsoup.Jsoup
import org.jsoup.nodes.Element


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfflineArticleScreen(
	articleId: Int,
	onSwitchToNormalMode: () -> Unit,
	onViewImageRequest: (data: String) -> Unit,
	onBack: () -> Unit,
	onDelete: () -> Unit
) {
	val viewModel = viewModel<OfflineArticleScreenViewModel>()
	val lazyListState = rememberLazyListState()
	
	var counter by rememberSaveable { mutableStateOf(1) }
	LaunchedEffect(key1 = Unit, block = {
		Log.e("offline_counter", counter.toString())
		counter++
	})
	
	val context = LocalContext.current
	val article by viewModel.offlineArticle.observeAsState()
	val fontSize by HubsDataStore.Settings
		.getValueFlow(context, HubsDataStore.Settings.ArticleScreen.FontSize)
		.collectAsState(initial = null)
	
	val textColor = MaterialTheme.colors.onSurface
	val spanStyle = remember(fontSize, textColor) {
		SpanStyle(
		color = textColor,
		fontSize = fontSize?.sp
			?: 16.sp,
	) }
	
	LaunchedEffect(key1 = Unit, block = {
		if (!viewModel.offlineArticle.isInitialized) {
			viewModel.loadArticle(articleId, context)
			
		}
	})
	
	val parsedContent by viewModel.parsedArticleContent.observeAsState()
	
	var showTopBarMenu by remember {
		mutableStateOf(false)
	}
	
	
	
	Scaffold(
		topBar = {
			TopAppBar(
				elevation = 0.dp,
				title = { Text(text = "Публикация") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					Box {
						IconButton(onClick = { showTopBarMenu = true }) {
							Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
						}
						
						OfflineArticleScreenMenu(
							show = showTopBarMenu,
							onDismiss = { showTopBarMenu = false },
							onDelete = onDelete,
							onSwitchToNormalMode = { onSwitchToNormalMode(); showTopBarMenu = false  }
						)
					}
					
				}
			)
		}
	) {
		
		if (viewModel.offlineArticle.isInitialized && article != null) {
			LaunchedEffect(key1 = Unit, block = {
				if (!viewModel.parsedArticleContent.isInitialized && fontSize != null) {
					val element =
						Jsoup.parse(article!!.contentHtml).getElementsByTag("body").first()
							?.child(0)
							?: Element("")
					
					viewModel.parsedArticleContent.postValue(
						parseChildElements(
							element,
							spanStyle,
							onViewImageRequest
						).second
					)
				}
			})
		}
		val elementSettings = remember() {
			ElementSettings(20.sp, 30.sp, true)
		}
		Box(
			modifier = Modifier
				.background(if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else MaterialTheme.colors.background)
				.fillMaxSize()
				.padding(it), contentAlignment = Alignment.Center
		) {
			if (parsedContent != null) {
				
				
				LazyColumn(
					state = lazyListState,
					modifier = Modifier.fillMaxSize(),
					contentPadding = PaddingValues(16.dp)
				) {
					item {
						Row(
							verticalAlignment = Alignment.CenterVertically,
						) {
							AsyncGifImage(
								modifier = Modifier
									.size(34.dp)
									.clip(RoundedCornerShape(8.dp))
									.background(Color.White),
								model = article!!.authorAvatarUrl,
								contentDescription = ""
							)
							
							Spacer(modifier = Modifier.width(4.dp))
							Text(
								text = article!!.authorName ?: "",
								fontWeight = FontWeight.W600,
								fontSize = 14.sp,
								color = MaterialTheme.colors.onBackground
							)
							Spacer(modifier = Modifier.weight(1f))
							Text(
								text = formatTime(article!!.timePublished),
								color = Color.Gray,
								fontSize = 12.sp,
								fontWeight = FontWeight.W400
							)
						}
					}
					item {
						Spacer(modifier = Modifier.height(8.dp))
					}
					item {
						Text(
							text = article!!.title,
							fontSize = 22.sp,
							fontWeight = FontWeight.W700,
							color = MaterialTheme.colors.onBackground
						)
					}
					item {
						val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
						
						Row(
							modifier = Modifier.padding(vertical = 4.dp),
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.spacedBy(12.dp)
						) {
							Row(
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									painter = painterResource(id = R.drawable.clock_icon),
									modifier = Modifier.size(15.dp),
									contentDescription = "",
									tint = statisticsColor
								)
								Spacer(modifier = Modifier.width(4.dp))
								Text(
									text = "${article!!.readingTime} мин",
									color = statisticsColor,
									fontWeight = FontWeight.W500,
									fontSize = 14.sp
								)
							}
							if (article!!.isTranslation) {
								Row(verticalAlignment = Alignment.CenterVertically) {
									Icon(
										painter = painterResource(id = R.drawable.translation),
										modifier = Modifier.size(15.dp),
										contentDescription = "",
										tint = statisticsColor
									)
									Spacer(modifier = Modifier.width(4.dp))
									Text(
										text = "Перевод",
										color = statisticsColor,
										fontWeight = FontWeight.W500,
										fontSize = 14.sp
									)
								}
							}
							Row(
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									painter = painterResource(id = R.drawable.offline),
									modifier = Modifier.size(15.dp),
									contentDescription = "",
									tint = statisticsColor
								)
								Spacer(modifier = Modifier.width(4.dp))
								Text(
									text = "Оффлайн режим",
									color = statisticsColor,
									fontWeight = FontWeight.W500,
									fontSize = 14.sp
								)
							}
						}
					}
					item {
						var hubsText by remember { mutableStateOf("") }
						
						LaunchedEffect(key1 = Unit, block = {
							if (hubsText == "") {
								hubsText =
									article!!.hubs.hubsList.joinToString(separator = ", ") {
										it.replace(" ", "\u00A0")
									}
							}
						})
						
						// Hubs
						Text(
							text = hubsText, style = TextStyle(
								color = Color.Gray,
								fontWeight = FontWeight.W500,
								lineHeight = (fontSize?.let { it - 4f }?.sp ?: 16.sp) * 1.25f,
								fontSize = fontSize?.let { it - 4f }?.sp ?: 16.sp
							)
						)
						Spacer(modifier = Modifier.height(8.dp))
					}
					
					// html
					items(
						items = parsedContent!!
					) {
						it?.invoke(spanStyle, elementSettings)
					}
					
					
					item {
						Divider(modifier = Modifier.padding(vertical = 24.dp))
						TitledColumn(
							title = "Хабы",
							titleStyle = MaterialTheme.typography.subtitle2.copy(
								color = MaterialTheme.colors.onBackground.copy(
									0.5f
								)
							)
						) {
							FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
								article!!.hubs.hubsList.forEach {
									HubChip(it) { }
								}
							}
						}
					}
					
					
				}
				ScrollBar(modifier = Modifier.align(Alignment.CenterEnd), lazyListState = lazyListState)
				
			} else {
				CircularProgressIndicator()
			}
		}
	}
	
}
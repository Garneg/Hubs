package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RangeSlider
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.PostComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCard
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.screens.settings.cards.SettingsCardItem
import kotlinx.coroutines.launch


class FeedSettingsScreenViewModel : ViewModel() {
	
	fun ChangeShowImageSetting(context: Context, show: Boolean) {
		viewModelScope.launch {
			HubsDataStore.Settings.edit(context, HubsDataStore.Settings.ArticleCard.ShowImage, show)
			
		}
	}
	
	fun ChangeShowTextSnippetSetting(context: Context, show: Boolean) {
		viewModelScope.launch {
			HubsDataStore.Settings.edit(
				context,
				HubsDataStore.Settings.ArticleCard.ShowTextSnippet,
				show
			)
			
		}
		
	}
	
	fun ChangeTitleFontSizeSetting(context: Context, size: Float) {
		viewModelScope.launch {
			HubsDataStore.Settings.edit(
				context,
				HubsDataStore.Settings.ArticleCard.TitleFontSize,
				size
			)
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FeedSettingsScreen(
	onBack: () -> Unit
) {
	val viewModel = viewModel<FeedSettingsScreenViewModel>()
	val context = LocalContext.current
	
	val scaffoldState = rememberBottomSheetScaffoldState()
	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		topBar = {
			TopAppBar(
				title = { Text(text = "Лента") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
					}
				})
		},
		sheetContent = {
			Column(modifier = Modifier.fillMaxHeight(0.5f)) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(16.dp)
				) {
					Spacer(
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.height(4.dp)
							.width(32.dp)
							.clip(RoundedCornerShape(50))
							.background(
								MaterialTheme.colors.onBackground.copy(0.15f)
							)
					)
				}
				Spacer(modifier = Modifier.height(16.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp)
						.verticalScroll(rememberScrollState()),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					val showImage by HubsDataStore.Settings.getValueFlow(
						LocalContext.current,
						HubsDataStore.Settings.ArticleCard.ShowImage
					).collectAsState(initial = null)
					showImage?.let {
						SettingsCardItem(
							title = "Показывать КДПВ",
							onClick = { viewModel.ChangeShowImageSetting(context, !it) },
							trailingIcon = {
								Checkbox(checked = it, onCheckedChange = {
									viewModel.ChangeShowImageSetting(context, it)
								})
							})
					}
					
					val showTextSnippet by HubsDataStore.Settings.getValueFlow(
						LocalContext.current,
						HubsDataStore.Settings.ArticleCard.ShowTextSnippet
					).collectAsState(initial = null)
					showTextSnippet?.let {
						SettingsCardItem(
							title = "Показать начало статьи",
							onClick = {
								viewModel.ChangeShowTextSnippetSetting(context, !it)
							},
							trailingIcon = {
								Checkbox(checked = it, onCheckedChange = {
									viewModel.ChangeShowTextSnippetSetting(context, it)
								})
							})
						
					}
					
					val titleFontSize by HubsDataStore.Settings.getValueFlow(
						LocalContext.current,
						HubsDataStore.Settings.ArticleCard.TitleFontSize
					).collectAsState(initial = null)
					
					titleFontSize?.let {
						Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
							var sliderValue by remember { mutableStateOf(it) }
							Text(text = "Размер шрифта заголовка: ${"%.1f".format(sliderValue)}")
							Slider(
								value = sliderValue,
								valueRange = 16f..26f,
								steps = 4,
								onValueChange = {
									sliderValue = it
								},
								onValueChangeFinished = {
									viewModel.ChangeTitleFontSizeSetting(context, sliderValue)
								})
						}
						
						
					}
				}
				
			}
		},
		sheetElevation = 8.dp,
		sheetPeekHeight = 80.dp,
		sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
	) {
		Column(
			modifier = Modifier.padding(it)
		) {
			ArticleCardStyle.defaultArticleCardStyle()?.let { style ->
				LazyColumn(
					contentPadding = PaddingValues(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(20) {
						
						Box(modifier = Modifier.animateContentSize()) {
							ArticleCard(
								article = TestArticle,
								onClick = { /*TODO*/ },
								onAuthorClick = { /*TODO*/ },
								onCommentsClick = { /*TODO*/ },
								style = style
							)
							Box(modifier = Modifier
								.matchParentSize()
								.pointerInput(Unit) {})
						}
					}
				}
			}
		}
	}
}

val TestArticle = ArticleSnippet(
	0,
	"вчера, 06:32",
	false,
	"Что делать дальше?",
	EditorVersion.FirstVersion,
	PostType.Unknown,
	listOf(),
	Article.Author(
		"squada",
		avatarUrl = "https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_1280.png"
	),
	Article.Statistics(0, 0, 0, 0, 0, 0),
	listOf(
		Article.Hub("", true, false, "Программирование", null),
		Article.Hub("", true, false, ".NET", Article.Hub.RelatedData(true)),
		Article.Hub("", false, false, "Карьера в IT", null)
	),
	"""Как мне дальше двигаться по карьере? - такой вопрос многие задавали себе как в начале своего пути так и позже не только в IT, но и в других сферах. Спектор этого вопроса охватывает почти все работящее население планеты, ведь карьера для человека сегодня дело очень серъезное бла бла мне надоело писать эту заглушку, извините.""".trimMargin(),
	"https://mykaleidoscope.ru/x/uploads/posts/2022-09/1663365833_63-mykaleidoscope-ru-p-pozdravlenie-s-povisheniem-vkontakte-65.jpg",
	null,
	5,
	PostComplexity.Low,
	null,
	false
)
package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.EditorVersion
import com.garnegsoft.hubs.api.PublicationComplexity
import com.garnegsoft.hubs.api.PostType
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCard
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import com.garnegsoft.hubs.ui.screens.settings.cards.SettingsCardItem
import kotlinx.coroutines.launch
import kotlin.math.abs


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
	
	fun ChangeSnippetMaxLinesSetting(context: Context, lines: Int) {
		viewModelScope.launch {
			HubsDataStore.Settings.edit(context, HubsDataStore.Settings.ArticleCard.TextSnippetMaxLines, lines)
		}
	}
	
	fun ChangeSnippetFontSize(context: Context, size: Float) {
		viewModelScope.launch {
			HubsDataStore.Settings.edit(context, HubsDataStore.Settings.ArticleCard.TextSnippetFontSize, size)
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
		modifier = Modifier.imePadding(),
		scaffoldState = scaffoldState,
		topBar = {
			TopAppBar(
				elevation = 0.dp,
				title = { Text(text = "Лента") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
					}
				})
		},
		sheetContent = {
			Column(modifier = Modifier.fillMaxHeight(0.65f)) {
				val scrollState = rememberScrollState()
				val lockBottomSheet = remember(scrollState.isScrollInProgress) { scrollState.canScrollBackward }
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
							.background(MaterialTheme.colors.onBackground.copy(0.15f))
					)

				}
				Spacer(modifier = Modifier.height(16.dp))
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.verticalScroll(scrollState)
						.padding(horizontal = 16.dp)
						.padding(bottom = 20.dp),
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
							}
						)
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
								valueRange = 16f..28f,
								steps = 5,
								onValueChange = {
									sliderValue = it
								},
								onValueChangeFinished = {
									viewModel.ChangeTitleFontSizeSetting(context, sliderValue)
								},
								colors = ArticleScreenSettingsSliderColors
							)
						}
						
						
					}
					
					val snippetFontSize by HubsDataStore.Settings.getValueFlow(
						LocalContext.current,
						HubsDataStore.Settings.ArticleCard.TextSnippetFontSize
					).collectAsState(initial = null)
					
					snippetFontSize?.let {
						Column(
							modifier = Modifier.padding(4.dp)
						) {
							var sliderValue by remember { mutableStateOf(it) }
							
							Text(text = "Размер шрифта начала статьи: ${"%.0f".format(sliderValue)}",)
							Slider(
								value = sliderValue,
								enabled = showTextSnippet ?: false,
								valueRange = 12f..24f,
								steps = 5,
								onValueChange = {
									sliderValue = it
								},
								onValueChangeFinished = {
									viewModel.ChangeSnippetFontSize(context, sliderValue)
								},
								colors = ArticleScreenSettingsSliderColors
							)
						}
					}
					
					val snippetMaxLines by HubsDataStore.Settings.getValueFlow(
						LocalContext.current,
						HubsDataStore.Settings.ArticleCard.TextSnippetMaxLines
					).collectAsState(initial = null)
					
					snippetMaxLines?.let {
						Column(
							modifier = Modifier.padding(4.dp)
						) {
							var sliderValue by remember { mutableStateOf(it.toFloat()) }
							
							Text(text = "Показывать строк начала статьи: ${"%.0f".format(sliderValue)}",)
							Slider(
								value = sliderValue,
								enabled = showTextSnippet ?: false,
								valueRange = 2f..10f,
								steps = 7,
								onValueChange = {
									sliderValue = it
								},
								onValueChangeFinished = {
									viewModel.ChangeSnippetMaxLinesSetting(context, sliderValue.toInt())
								},
								colors = ArticleScreenSettingsSliderColors
							)
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
			val fakeArticles = listOf(TestArticle, SecondArticle)
			ArticleCardStyle.defaultArticleCardStyle()?.let { style ->
				LazyColumn(
					contentPadding = PaddingValues(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(fakeArticles) {
						
						Box(modifier = Modifier.animateContentSize()) {
							ArticleCard(
								article = it,
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

val TestArticle
	@Composable
	get() = ArticleSnippet(
	0,
	"вчера, 06:32",
	false,
	"Что делать дальше?",
	EditorVersion.FirstVersion,
	PostType.Unknown,
	listOf(),
	Article.Author(
		"squada",
		avatarUrl = "https://assets.habr.com/habr-web/img/avatars/012.png"
	),
	Article.Statistics(5, 9, 2000, 4, 0, 0),
	listOf(
		Article.Hub("", true, false, "Программирование", null),
		Article.Hub("", true, false, ".NET", Article.Hub.RelatedData(true)),
		Article.Hub("", false, false, "Карьера в IT", null)
	),
	stringResource(id = R.string.settings_first_article_snippet_text),
	"https://megapicture.com/non-existing-picture.png",
	null,
	15,
	PublicationComplexity.Low,
	null,
	false
)


val SecondArticle
	@Composable
	get() = ArticleSnippet(
	0,
	"вчера, 03:28",
	false,
	"Игорь уничтожил нам серверную!!!",
	EditorVersion.FirstVersion,
	PostType.Unknown,
	listOf(),
	Article.Author(
		"gohonor",
		avatarUrl = "https://assets.habr.com/habr-web/img/avatars/016.png"
	),
	Article.Statistics(45, 12, 4000, -14, 0, 0),
	listOf(
		Article.Hub("", false, false, "Системное администрирование", null),
	),
	stringResource(R.string.settings_second_article_snippet_text),
		"https://megapicture.com/non-existing-picture.png",
	null,
	10,
	PublicationComplexity.None,
	null,
	false
)
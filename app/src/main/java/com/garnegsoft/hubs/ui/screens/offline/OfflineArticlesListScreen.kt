package com.garnegsoft.hubs.ui.screens.offline

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.article.offline.OfflineArticleSnippet
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesController
import com.garnegsoft.hubs.api.article.offline.OfflineArticlesDatabase
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle
import kotlinx.coroutines.flow.Flow

class OfflineArticleScreenViewModel(context: Context) : ViewModel() {
	private val dao = OfflineArticlesDatabase.getDb(context).articlesDao()
	val articles: Flow<List<OfflineArticleSnippet>> = dao.getAllSnippetsSortedByIdDesc()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OfflineArticlesListScreen(
	onBack: () -> Unit,
	onArticleClick: (articleId: Int) -> Unit
) {
	
	val context = LocalContext.current
	val viewModel = viewModel { OfflineArticleScreenViewModel(context) }
	
	val articles by viewModel.articles.collectAsState(initial = null)
	var showMenu by remember { mutableStateOf(false) }
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Сохранённые") },
				elevation = 0.dp,
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "назад")
					}
				},
				actions = {
					IconButton(onClick = { showMenu = true}) {
						Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "меню")
					}
				}
			)
			
		}
	) {
		val cardsStyle = ArticleCardStyle.defaultArticleCardStyle()
		cardsStyle?.let { style ->
			articles?.let { articlesList ->
				if (articlesList.isEmpty()) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(it)
							.padding(32.dp)
					){
						Column(modifier = Modifier.align(Alignment.Center),) {
							Text(
								modifier = Modifier.fillMaxWidth(),
								text = "Нет скачанных статей",
								style = MaterialTheme.typography.subtitle1,
								textAlign = TextAlign.Center
							)
							Spacer(modifier = Modifier.height(4.dp))
							Text(
								textAlign = TextAlign.Center,
								color = MaterialTheme.colors.onBackground.copy(0.5f),
								text = "Для того, чтобы скачать статью вы можете зайти на статью и нажать на иконку скачивания справа сверху или после долгого нажатия на кнопку добавления в закладки в лентах нажать на кнопку с той же иконкой. Второй способ работает только после входа в аккаунт",
							)
						}
						
					}
				}
				else {
					LazyColumn(
						modifier = Modifier
							.fillMaxSize()
							.padding(it),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						contentPadding = PaddingValues(8.dp)
					) {
						items(
							items = articlesList,
							key = { it.articleId }
						) {
							Box(modifier = Modifier.animateItemPlacement()) {
								OfflineArticleCard(
									article = it,
									onClick = { onArticleClick(it.articleId) },
									onDelete = {
										OfflineArticlesController.deleteArticle(
											it.articleId,
											context
										)
									},
									style = style
								)
							}
						}
					}
				}
			}
		}
		
	}
}
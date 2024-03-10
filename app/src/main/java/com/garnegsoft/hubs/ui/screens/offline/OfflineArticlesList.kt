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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

class OfflineArticlesListScreenViewModel(context: Context) : ViewModel() {
	private val dao = OfflineArticlesDatabase.getDb(context).articlesDao()
	val articles: Flow<List<OfflineArticleSnippet>> = dao.getAllSnippetsSortedByIdDesc()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OfflineArticlesList(
	onArticleClick: (articleId: Int) -> Unit
) {
	val context = LocalContext.current
	val viewModel = viewModel { OfflineArticlesListScreenViewModel(context) }
	
	val lazyListState = rememberLazyListState()
	
	
	val articles by viewModel.articles.collectAsState(initial = null)
	var firstArticleId by rememberSaveable { mutableStateOf(0) }
	
	LaunchedEffect(key1 = articles?.firstOrNull()?.articleId, block = {
		if (firstArticleId > 0 && articles?.firstOrNull()?.articleId != null && articles?.firstOrNull()?.articleId != firstArticleId ) {
			lazyListState.scrollToItem(0)
			firstArticleId = articles?.firstOrNull()!!.articleId
		} else {
			firstArticleId = articles?.firstOrNull()?.articleId ?: 0
		}
	})
	
		val cardsStyle = ArticleCardStyle.defaultArticleCardStyle()
		cardsStyle?.let { style ->
			articles?.let { articlesList ->
				if (articlesList.isEmpty()) {
					Box(
						modifier = Modifier
							.fillMaxSize()
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
							.fillMaxSize(),
						state = lazyListState,
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
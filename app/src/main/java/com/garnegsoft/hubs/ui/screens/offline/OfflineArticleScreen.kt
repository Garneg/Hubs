package com.garnegsoft.hubs.ui.screens.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.article.offline.offlineArticlesDatabase
import com.garnegsoft.hubs.ui.common.feedCards.article.ArticleCardStyle

@Composable
fun OfflineArticlesScreen(
	onBack: () -> Unit,
	onArticleClick: (articleId: Int) -> Unit
) {
	val articlesDao = LocalContext.current.offlineArticlesDatabase.articlesDao()
	
	val articles by articlesDao.getAllSnippetsSortedByIdDesc().collectAsState(initial = emptyList())
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Сохраненные публикации") },
				elevation = 0.dp,
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
					}
				}
			)
			
		}
	) {
		val cardsStyle = ArticleCardStyle.defaultArticleCardStyle()
		cardsStyle?.let { style ->
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
					.padding(it),
				verticalArrangement = Arrangement.spacedBy(8.dp),
				contentPadding = PaddingValues(8.dp)
			) {
				items(
					items = articles,
					key = { it.articleId }
				) {
					OfflineArticleCard(
						article = it,
						onClick = { onArticleClick(it.articleId) },
						style = style
					)
				}
			}
		}
		
	}
}
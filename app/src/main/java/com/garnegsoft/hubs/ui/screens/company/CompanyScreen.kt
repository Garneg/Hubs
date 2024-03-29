package com.garnegsoft.hubs.ui.screens.company

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.HabrScrollableTabRow
import com.garnegsoft.hubs.ui.common.ScrollUpMethods
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompanyScreen(
	viewModelStoreOwner: ViewModelStoreOwner,
	alias: String,
	onArticleClick: (id: Int) -> Unit,
	onUserClick: (alias: String) -> Unit,
	onCommentsClick: (postId: Int) -> Unit,
	onBack: () -> Unit
) {
	val viewModel = viewModel(viewModelStoreOwner) { CompanyScreenViewModel(alias) }
	val companyProfile by viewModel.companyProfile.observeAsState()
	val whoIs by viewModel.companyWhoIs.observeAsState()
	
	val commonCoroutineScope = rememberCoroutineScope()
	Scaffold(
		topBar = {
			val context = LocalContext.current
			TopAppBar(
				title = { Text("Компания") },
				elevation = 0.dp,
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
					}
				},
				actions = {
					IconButton(
						onClick = {
							val intent = Intent(Intent.ACTION_SEND)
							intent.putExtra(
								Intent.EXTRA_TEXT,
								"Блог ${companyProfile?.title} — https://habr.com/ru/companies/$alias/blog"
							)
							intent.setType("text/plain")
							val chooser = Intent.createChooser(intent, null)
							context.startActivity(chooser)
						}) {
						Icon(imageVector = Icons.Outlined.Share, contentDescription = "")
					}
				}
			)
		}
	) {
		LaunchedEffect(key1 = Unit, block = {
			launch(Dispatchers.IO) {
				if (!viewModel.companyProfile.isInitialized) {
					viewModel.companyProfile.postValue(
						CompanyController.get(alias)
					)
				}
			}
		})
		
		val profilePageScrollState = rememberScrollState()
		val articlesLazyListState = rememberLazyListState()
		val newsLazyListState = rememberLazyListState()
		val followersLazyListState = rememberLazyListState()
		val employeesLazyListState = rememberLazyListState()
		
		companyProfile?.let { companyProfile ->
			Column(modifier = Modifier.padding(it)) {
				val tabs = remember {
					var map: Map<String, @Composable () -> Unit> = mapOf(
						"Профиль" to {
							if (whoIs != null) {
								CompanyProfile(company = companyProfile, scrollState = profilePageScrollState)
							} else {
								LaunchedEffect(key1 = Unit, block = {
									launch(Dispatchers.IO) {
										viewModel.companyWhoIs.postValue(
											CompanyController.getWhoIs(alias)
										)
									}
								})
							}
						},
					)
					if (companyProfile.statistics.articlesCount > 0) {
						map += "Блог ${formatLongNumbers(companyProfile.statistics.articlesCount)}" to {
							ArticlesListPageWithFilter(
								listModel = viewModel.blogArticlesListModel,
								lazyListState = articlesLazyListState,
								onArticleSnippetClick = onArticleClick,
								onArticleAuthorClick = onUserClick,
								onArticleCommentsClick = onCommentsClick,
							) { defaultValues, onDismiss, onDone ->
								CompanyBlogArticlesFilter(
									defaultValues = defaultValues,
									onDismiss = onDismiss,
									onDone = onDone
								)
							}
						}
					}
					if (companyProfile.statistics.newsCount > 0) {
						map += "Новости ${formatLongNumbers(companyProfile.statistics.newsCount)}" to {
							ArticlesListPage(
								listModel = viewModel.blogNewsListModel,
								lazyListState = newsLazyListState,
								onArticleSnippetClick = onArticleClick,
								onArticleAuthorClick = onUserClick,
								onArticleCommentsClick = onCommentsClick
							)
						}
					}
					if (companyProfile.statistics.subscribersCount > 0) {
						map += "Подписчики ${formatLongNumbers(companyProfile.statistics.subscribersCount)}" to {
							UsersListPage(
								listModel = viewModel.followersListModel, 
								lazyListState = followersLazyListState,
								onUserClick = onUserClick)
							
						}
					}
					if (companyProfile.statistics.employeesCount > 0) {
						map += "Сотрудники ${formatLongNumbers(companyProfile.statistics.employeesCount)}" to {
							UsersListPage(
								listModel = viewModel.employeesListModel, 
								lazyListState = employeesLazyListState,
								onUserClick = onUserClick)
						}
					}
					map
				}
				val pagerState = rememberPagerState {
					tabs.size
				}
				HabrScrollableTabRow(
					pagerState = pagerState,
					tabs = remember { tabs.keys.toList() }){ index, title ->  
					when {
						title.startsWith("Профиль") -> { ScrollUpMethods.scrollNormalList(profilePageScrollState)}
						title.startsWith("Блог") -> { ScrollUpMethods.scrollLazyList(articlesLazyListState) }
						title.startsWith("Новости") -> { ScrollUpMethods.scrollLazyList(newsLazyListState) }
						title.startsWith("Подписчики") -> { ScrollUpMethods.scrollLazyList(followersLazyListState) }
						title.startsWith("Сотрудники") -> { ScrollUpMethods.scrollLazyList(employeesLazyListState) }
					}
				}
				HorizontalPager(state = pagerState) {
					tabs.values.elementAt(it).invoke()
				}
			}
		} ?: Box(modifier = Modifier.padding(it)) {
		
		}
	}
}


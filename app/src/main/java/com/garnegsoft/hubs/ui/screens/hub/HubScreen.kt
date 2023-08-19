package com.garnegsoft.hubs.ui.screens.hub

import ArticlesListController
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.Hub
import com.garnegsoft.hubs.api.hub.HubController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.ui.common.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CompaniesListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage
import com.garnegsoft.hubs.ui.screens.search.ArticlesSearchFilter
import com.garnegsoft.hubs.ui.theme.RatingPositive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HubScreen(
    alias: String,
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClick: (postId: Int) -> Unit,
    onUserClick: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit,
    onCommentsClick: (postId: Int) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel = viewModel(viewModelStoreOwner) { HubScreenViewModel(alias) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Хаб") },
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND)
                        sendIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "${viewModel.hub.value?.title} — https://habr.com/ru/hub/${alias}/"
                        )
                        sendIntent.setType("text/plain")
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = "")
                    }
                }
            )
        }) {
        Column(modifier = Modifier.padding(it)) {
            val tabs = remember {
                listOf(
                    "Профиль",
                    "Статьи",
                    "Новости",
                    "Авторы",
                    "Компании"
                )
            }
            val profilePageScrollState = rememberScrollState()
            val articlesLazyListState = rememberLazyListState()
            val newsLazyListState = rememberLazyListState()
            val authorsLazyListState = rememberLazyListState()
            val companiesLazyListState = rememberLazyListState()
            
            val pagerState = rememberPagerState { 5 }
            HabrScrollableTabRow(pagerState = pagerState, tabs = tabs) { index, title ->
                when(index) {
                    0 -> ScrollUpMethods.scrollNormalList(profilePageScrollState)
                    1 -> ScrollUpMethods.scrollLazyList(articlesLazyListState)
                    2 -> ScrollUpMethods.scrollLazyList(newsLazyListState)
                    3 -> ScrollUpMethods.scrollLazyList(authorsLazyListState)
                    4 -> ScrollUpMethods.scrollLazyList(companiesLazyListState)
                }
            }
            HorizontalPager(state = pagerState) {
                when (it) {
                    0 -> {
                        if (viewModel.hub.observeAsState().value != null) {
                            HubProfile(
                                hub = viewModel.hub.observeAsState().value!!,
                                scrollState = profilePageScrollState
                            )
                        } else {
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.hub.postValue(
                                        HubController.get("hubs/${alias}/profile")
                                    )
                                }
                            })

                        }
                    }
                    // articles
                    1 -> {
                        ArticlesListPageWithFilter(
                            listModel = viewModel.articlesListModel,
                            lazyListState = articlesLazyListState,
                            onArticleSnippetClick = onArticleClick,
                            onArticleAuthorClick = onUserClick,
                            onArticleCommentsClick = onCommentsClick
                        ) { defaultValues, onDismiss, onDone ->
                            HubArticlesFilter(defaultValues, onDismiss, onDone)
                        }
                    }
                    // news
                    2 -> {
                        ArticlesListPageWithFilter(
                            listModel = viewModel.newsListModel,
                            lazyListState = newsLazyListState,
                            onArticleSnippetClick = onArticleClick,
                            onArticleAuthorClick = onUserClick,
                            onArticleCommentsClick = onCommentsClick
                        ) { defaultValues, onDismiss, onDone ->
                            HubsNewsFilterDialog(
                                defaultValues = defaultValues,
                                onDismiss = onDismiss,
                                onDone = onDone
                            )
                        }
                    }
                    // authors
                    3 -> {
                        UsersListPage(
                            listModel = viewModel.authorsListModel,
                            lazyListState = authorsLazyListState,
                            onUserClick = onUserClick
                        )
                    }
                    // companies
                    4 -> {
                        CompaniesListPage(
                            listModel = viewModel.companiesListModel,
                            lazyListState = companiesLazyListState,
                            onCompanyClick = onCompanyClick
                        )
                    }
                }
            }
        }
    }

}



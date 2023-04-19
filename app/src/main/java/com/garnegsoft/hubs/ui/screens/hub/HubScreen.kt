package com.garnegsoft.hubs.ui.screens.hub

import ArticlesListController
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HubScreenViewModel : ViewModel() {
    var articles = MutableLiveData<HabrList<ArticleSnippet>>()
    var hub = MutableLiveData<Hub>()
    var authors = MutableLiveData<HabrList<UserSnippet>>()
    var companies = MutableLiveData<HabrList<CompanySnippet>>()

    fun loadArticles(alias: String, page: Int, vararg additionalArgs: Pair<String, String>){
        viewModelScope.launch(Dispatchers.IO) {
            ArticlesListController.getArticlesSnippets(
                "articles",
                mapOf("hub" to alias, "page" to page.toString()) + additionalArgs.toMap()
            )?.let {
                if (articles.value != null && page > 1){
                    articles.postValue(articles.value!! + it)
                }
                else{
                    articles.postValue(it)
                }
            }
        }
    }
}


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
    var viewModel = viewModel<HubScreenViewModel>(viewModelStoreOwner)
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
                        enabled = viewModel.hub.isInitialized,
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
                    "Авторы",
                    "Компании"
                )
            }

            val articles = viewModel.articles.observeAsState().value
            val authors = viewModel.authors.observeAsState().value
            val companies = viewModel.companies.observeAsState().value
            val pagerState = rememberPagerState()
            HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
            HorizontalPager(state = pagerState, pageCount = 4) {
                when (it) {
                    0 -> {
                        if (viewModel.hub.observeAsState().value != null) {
                            HubProfile(hub = viewModel.hub.observeAsState().value!!)
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
                        if (articles != null) {
                            PagedHabrSnippetsColumn(
                                data = articles,
                                onNextPageLoad = {
                                    viewModel.loadArticles(alias, it, "sort" to "all")
                                }
                            ) {
                                ArticleCard(
                                    article = it,
                                    onClick = { onArticleClick(it.id) },
                                    onAuthorClick = { it.author?.let { it1 -> onUserClick(it1.alias) } },
                                    onCommentsClick = { onCommentsClick(it.id) }
                                )
                            }
                        } else {
                            LaunchedEffect(key1 = Unit, block = {
                                viewModel.loadArticles(alias, 1, "sort" to "all")
                            })
                        }
                    }
                    // authors
                    2 -> {
                        if (authors != null){
                            PagedHabrSnippetsColumn(
                                data = authors,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        viewModel.authors.postValue(
                                            authors + UsersListController.get("hubs/$alias/authors", mapOf("page" to it.toString()))!!
                                        )
                                    }
                                }
                            ) {
                                UserCard(user = it, onClick = {onUserClick(it.alias)})
                            }
                        }
                        else{
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.authors.postValue(
                                        UsersListController.get("hubs/$alias/authors")
                                    )
                                }
                            })
                        }
                    }
                    // companies
                    3 -> {
                        if (companies != null){
                            PagedHabrSnippetsColumn(
                                data = companies,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        viewModel.companies.postValue(
                                            CompaniesListController.get("hubs/$alias/companies", mapOf("page" to it.toString()))
                                        )
                                    }
                                }
                            ) {
                                CompanyCard(company = it, onClick = { onCompanyClick(it.alias)})
                            }
                        }
                        else{
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.companies.postValue(
                                        CompaniesListController.get("hubs/$alias/companies")
                                    )
                                }
                            })
                        }
                    }
                }
            }
        }
    }

}



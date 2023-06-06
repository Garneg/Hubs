package com.garnegsoft.hubs.ui.screens.company

import ArticlesListController
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.HabrScrollableTabRow
import com.garnegsoft.hubs.ui.common.PagedHabrSnippetsColumn
import com.garnegsoft.hubs.ui.common.UserCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CompanyScreenViewModel : ViewModel() {
    var companyProfile = MutableLiveData<Company>()
    var companyWhoIs = MutableLiveData<Company.WhoIs>()
    var blogArticles = MutableLiveData<HabrList<ArticleSnippet>>()
    var blogNews = MutableLiveData<HabrList<ArticleSnippet>>()
    var followers = MutableLiveData<HabrList<UserSnippet>>()
    var employees = MutableLiveData<HabrList<UserSnippet>>()
}

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
    val viewModel = viewModel<CompanyScreenViewModel>(viewModelStoreOwner)
    val companyProfile by viewModel.companyProfile.observeAsState()
    val articles by viewModel.blogArticles.observeAsState()
    val employees by viewModel.employees.observeAsState()
    val news by viewModel.blogNews.observeAsState()
    val followers by viewModel.followers.observeAsState()
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
                        enabled = viewModel.companyProfile.isInitialized,
                        onClick = {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT, "Блог ${companyProfile?.title} — https://habr.com/ru/companies/$alias/blog")
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

        Column(modifier = Modifier.padding(it)) {
            val pagerState = rememberPagerState()
            val tabs = remember(key1 = companyProfile?.statistics) {
                listOf(
                    "Профиль",
                    "Блог${companyProfile?.let{ " " + formatLongNumbers(it.statistics.postsCount) } ?: ""}",
                    "Новости${companyProfile?.let{ " " + formatLongNumbers(it.statistics.newsCount) } ?: ""}",
                    "Подписчики${companyProfile?.let{ " " + formatLongNumbers(it.statistics.subscribersCount) } ?: ""}",
                    "Сотрудники${companyProfile?.let{ " " + formatLongNumbers(it.statistics.employees) } ?: ""}"
                )
            }
            HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
            HorizontalPager(state = pagerState, pageCount = 5) {
                when (it) {
                    0 -> {
                        if (companyProfile != null) {
                            CompanyProfile(company = companyProfile!!)
                        } else {
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.companyProfile.postValue(
                                        CompanyController.get(alias)
                                    )
                                }
                            })
                        }
                    }
                    1 -> {
                        if (articles != null) {
                            PagedHabrSnippetsColumn(
                                data = articles!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("company" to alias, "page" to it.toString())
                                        )?.let {
                                            viewModel.blogArticles.postValue(
                                                articles!! + it
                                            )
                                        }

                                    }
                                }
                            ) {
                                ArticleCard(
                                    article = it,
                                    onClick = { onArticleClick(it.id) },
                                    onCommentsClick = { onCommentsClick(it.id) },
                                    onAuthorClick = { onUserClick(it.author!!.alias) }
                                )
                            }
                        } else {
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    viewModel.blogArticles.postValue(
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf("company" to alias)
                                        )
                                    )
                                }
                            })
                        }
                    }

                    2 -> {
                        if (news != null){
                            PagedHabrSnippetsColumn(
                                data = news!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        ArticlesListController.getArticlesSnippets(
                                            "articles",
                                            mapOf(
                                                "companyNews" to alias,
                                                "page" to it.toString()
                                            )
                                        )?.let {
                                            viewModel.blogNews.postValue(
                                            news!! + it
                                            )
                                        }

                                    }
                                }
                            ) {
                                ArticleCard(article = it,
                                    onClick = { onArticleClick(it.id)},
                                    onAuthorClick = { onUserClick(it.author!!.alias) },
                                    onCommentsClick = { onCommentsClick(it.id)}
                                )
                            }
                        }
                        else{
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    ArticlesListController.getArticlesSnippets(
                                        "articles",
                                        mapOf(
                                            "companyNews" to alias,
                                        )
                                    )?.let {
                                        viewModel.blogNews.postValue(
                                            it
                                        )
                                    }

                                }
                            })
                        }
                    }

                    3 -> {
                        if (followers != null) {
                            PagedHabrSnippetsColumn(
                                data = followers!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        UsersListController.get("companies/$alias/fans/all",
                                        mapOf("page" to it.toString()))?.let{
                                            viewModel.followers.postValue(
                                                followers!! + it
                                            )
                                        }

                                    }
                                }
                            ) {
                                UserCard(user = it) {
                                    onUserClick(it.alias)
                                }
                            }
                        }
                        else{
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO){
                                    viewModel.followers.postValue(
                                        UsersListController.get("companies/$alias/fans/all")
                                    )
                                }
                            })
                        }
                    }
                    4 -> {
                        if (employees != null){
                            PagedHabrSnippetsColumn(
                                data = employees!!,
                                onNextPageLoad = {
                                    launch(Dispatchers.IO) {
                                        UsersListController.get("companies/$alias/workers/all",
                                        mapOf("page" to it.toString()))?.let {
                                            viewModel.employees.postValue(
                                                employees!! + it
                                            )
                                        }
                                    }
                                }
                            ) {
                                UserCard(user = it) {
                                    onUserClick(it.alias)
                                }
                            }
                        }
                        else{
                            LaunchedEffect(key1 = Unit, block = {
                                launch(Dispatchers.IO) {
                                    UsersListController.get("companies/$alias/workers/all",)?.let {
                                        viewModel.employees.postValue(it)
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}


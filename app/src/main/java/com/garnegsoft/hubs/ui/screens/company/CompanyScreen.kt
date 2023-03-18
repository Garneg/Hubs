package com.garnegsoft.hubs.ui.screens.company

import ArticlesListController
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.common.ArticleCard
import com.garnegsoft.hubs.ui.common.HabrScrollableTabRow
import com.garnegsoft.hubs.ui.common.PagedHabrSnippetsColumn
import com.garnegsoft.hubs.ui.common.UserCard
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CompanyScreenViewModel : ViewModel() {
    var companyProfile = MutableLiveData<Company>()
    var blogArticles = MutableLiveData<HabrList<ArticleSnippet>>()
    var blogNews = MutableLiveData<HabrList<ArticleSnippet>>()
    var followers = MutableLiveData<HabrList<UserSnippet>>()
    var employees = MutableLiveData<HabrList<UserSnippet>>()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CompanyScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    alias: String,
    onArticleClick: (id: Int) -> Unit,
    onUserClick: (alias: String) -> Unit,
    onCommentsClick: (postId: Int) -> Unit,
    onBack: () -> Unit
) {
    var viewModel = viewModel<CompanyScreenViewModel>(viewModelStoreOwner)
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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_TEXT, "Блог ${companyProfile?.title} — https://habr.com/ru/company/$alias/blog")
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
            HorizontalPager(state = pagerState, count = 5) {
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

@Composable
fun CompanyProfile(
    company: Company
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (company.avatarUrl != null) {
                        AsyncImage(
                            model = company.avatarUrl,
                            modifier = Modifier
                                .size(55.dp)
                                .align(Alignment.Center)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                ),
                            contentDescription = ""
                        )
                    } else {
                        Icon(
                            modifier = Modifier
                                .size(55.dp)
                                .border(
                                    width = 4.dp,
                                    color = placeholderColor(company.alias),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .align(Alignment.Center)
                                .padding(5.dp),
                            painter = painterResource(id = R.drawable.company_avatar_placeholder),
                            contentDescription = "",
                            tint = placeholderColor(company.alias)
                        )
                    }
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = company.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Center
                )
                Box(modifier = Modifier.fillMaxWidth()) {

                }
                if (company.description != null)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = 8.dp,
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp
                            )
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = company.description,
                            fontWeight = FontWeight.W500,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Информация", fontSize = 20.sp, fontWeight = FontWeight.W500)
                }
                Divider(modifier = Modifier.padding(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Рейтинг", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.statistics.rating.toString(),
                        textAlign = TextAlign.Right
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Дата регистрации", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.registrationDate,
                        textAlign = TextAlign.Right
                    )
                }
                if (company.foundationDate != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Дата основания", modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.weight(1f),
                            text = company.foundationDate,
                            textAlign = TextAlign.Right
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Численность", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.staffNumber,
                        textAlign = TextAlign.Right
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(text = "Местоположение", modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = company.location,
                        textAlign = TextAlign.Right
                    )
                }

                if (company.siteUrl != null) {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(19.dp))
                            .clickable {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(company.siteUrl)
                                    )
                                )
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Перейти на сайт")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "")
                    }
                }
            }
        }

    }
}
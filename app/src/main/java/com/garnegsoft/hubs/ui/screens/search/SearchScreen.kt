package com.garnegsoft.hubs.ui.screens.search

import ArticlesListController
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.list.ArticleSnippet
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.api.hub.list.HubsListController
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.ui.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchScreenViewModel : ViewModel() {
    var articles = MutableLiveData<HabrList<ArticleSnippet>>()
    var hubs = MutableLiveData<HabrList<HubSnippet>>()
    var companies = MutableLiveData<HabrList<CompanySnippet>>()
    var users = MutableLiveData<HabrList<UserSnippet>>()
    var comments = MutableLiveData<HabrList<CommentSnippet>>()

    fun loadArticles(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            ArticlesListController.getArticlesSnippets(
                "articles",
                mapOf("query" to query, "page" to page.toString()) + additionalArgs.toMap()
            )?.let {
                if (articles.value != null && page > 1)
                    articles.postValue(articles.value!! + it)
                else
                    articles.postValue(it)
            }
        }
    }

    fun loadHubs(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            HubsListController.get(
                "hubs/search",
                mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
            )?.let {
                if (hubs.value != null && page > 1) {
                    hubs.postValue(hubs.value!! + it)
                } else {
                    hubs.postValue(it)
                }
            }
        }
    }

    fun loadCompanies(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            CompaniesListController.get(
                "companies/search",
                mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
            )?.let {
                if (companies.value != null && page > 1)
                    companies.postValue(companies.value!! + it)
                else
                    companies.postValue(it)
            }
        }
    }

    fun loadUsers(query: String, page: Int, vararg additionalArgs: Pair<String, String>) {
        viewModelScope.launch(Dispatchers.IO) {
            UsersListController.get(
                "users/search",
                mapOf("q" to query, "page" to page.toString()) + additionalArgs.toMap()
            )?.let {
                if (users.value != null && page > 1) {
                    users.postValue(users.value!! + it)
                } else
                    users.postValue(it)
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModelStoreOwner: ViewModelStoreOwner,
    onArticleClicked: (Int) -> Unit,
    onHubClicked: (alias: String) -> Unit,
    onUserClicked: (alias: String) -> Unit,
    onCompanyClicked: (alias: String) -> Unit,
    onCommentsClicked: (parentPostId: Int) -> Unit,
    onBackClicked: () -> Unit,
) {
    val viewModel = viewModel<SearchScreenViewModel>(viewModelStoreOwner)
    var showPages by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Поиск") },
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "")
                    }
                })
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            var searchTextValue by rememberSaveable { mutableStateOf("") }
            var showClearAllButton by rememberSaveable { mutableStateOf(false) }
            val focusRequester by remember { mutableStateOf(FocusRequester()) }
            val keyboardController = LocalSoftwareKeyboardController.current
            val coroutineScope = rememberCoroutineScope()

            var pageNumber by rememberSaveable { mutableStateOf(1) }

            var currentQuery by rememberSaveable {
                mutableStateOf(searchTextValue)
            }

            val articlesLazyListState = rememberLazyListState()

            val articlesList by viewModel.articles.observeAsState()

            var doRequestFocus by rememberSaveable {
                mutableStateOf(true)
            }
            LaunchedEffect(key1 = Unit) {
                if (doRequestFocus) {
                    focusRequester.requestFocus()
                    doRequestFocus = false
                }
            }
            Row(
                modifier = Modifier
                    .background(if (currentQuery.isNotEmpty()) MaterialTheme.colors.surface else MaterialTheme.colors.background)
                    .padding(8.dp)
                    .padding(horizontal = 4.dp)
                    .clip(shape = RoundedCornerShape(8.dp))
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colors.secondary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(top = 8.dp, bottom = 8.dp, start = 8.dp)
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusEvent {
                            if (it.isCaptured) {
                                keyboardController?.show()
                            }
                        },

                    value = searchTextValue,
                    onValueChange = {
                        searchTextValue = it
                        showClearAllButton = it.length > 0
                    },
                    textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = false,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions {
                        keyboardController?.hide()
                        if (searchTextValue.startsWith(".id")) {
                            if (searchTextValue.drop(3).isDigitsOnly())
                                onArticleClicked(searchTextValue.drop(3).toInt())
                        } else {
                            currentQuery = searchTextValue
                            coroutineScope.launch(Dispatchers.IO) {
                                pageNumber = 1
                                viewModel.loadArticles(currentQuery, 1)

                            }
                            showPages = true
                        }
                    },
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colors.secondary)
                )
                if (showClearAllButton)
                    IconButton(
                        onClick = {
                            searchTextValue = ""
                            showClearAllButton = false
                        }) {
                        Icon(
                            tint = MaterialTheme.colors.secondary,
                            imageVector = Icons.Outlined.Close,
                            contentDescription = ""
                        )
                    }

            }
            var pagerState = rememberPagerState()
            if (showPages) {
                var tabs = listOf(
                    "Публикации",
                    "Хабы",
                    "Компании",
                    "Пользователи"
                )

                HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
                HorizontalPager(state = pagerState, pageCount = 4) {
                    when (it) {
                        0 -> {
                            if (articlesList != null) {
                                PagedHabrSnippetsColumn(
                                    modifier = Modifier.fillMaxHeight(),
                                    data = articlesList!!,
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    lazyListState = articlesLazyListState,
                                    onNextPageLoad = {
                                        viewModel.loadArticles(currentQuery, it)
                                    }
                                ) {
                                    ArticleCard(
                                        article = it,
                                        onClick = { onArticleClicked(it.id) },
                                        onAuthorClick = { onUserClicked(it.author!!.alias) },
                                        onCommentsClick = { onCommentsClicked(it.id) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            val hubs by viewModel.hubs.observeAsState()
                            if (hubs != null) {
                                PagedHabrSnippetsColumn(
                                    modifier = Modifier.fillMaxHeight(),
                                    data = hubs!!,
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    lazyListState = rememberLazyListState(),
                                    onNextPageLoad = { viewModel.loadHubs(currentQuery, it) }
                                ) {
                                    HubCard(hub = it, onClick = { onHubClicked(it.alias) })
                                }
                            }
                            LaunchedEffect(key1 = currentQuery, block = {
                                viewModel.loadHubs(currentQuery, 1)
                            })
                        }
                        2 -> {
                            val companies by viewModel.companies.observeAsState()

                            if (companies != null) {
                                PagedHabrSnippetsColumn(
                                    modifier = Modifier.fillMaxHeight(),
                                    data = companies!!,
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    onNextPageLoad = {
                                        viewModel.loadCompanies(currentQuery, it)
                                    }
                                ) {
                                    CompanyCard(
                                        company = it,
                                        onClick = { onCompanyClicked(it.alias) })
                                }

                            }
                            LaunchedEffect(key1 = currentQuery, block = {
                                viewModel.loadCompanies(currentQuery, 1)
                            })
                        }
                        3 -> {
                            val users by viewModel.users.observeAsState()
                            val usersLazyListState = rememberLazyListState()

                            if (users != null) {
                                PagedHabrSnippetsColumn(
                                    lazyListState = usersLazyListState,
                                    data = users!!,
                                    contentPadding = PaddingValues(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    onNextPageLoad = {
                                        viewModel.loadUsers(currentQuery, it)
                                    }
                                ) {
                                    UserCard(user = it, onClick = { onUserClicked(it.alias) })
                                }
                            }
                            LaunchedEffect(key1 = currentQuery, block = {
                                viewModel.loadUsers(currentQuery, 1)
                            })

                        }

                    }
                }

            }
        }

    }

}
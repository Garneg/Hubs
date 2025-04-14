package com.garnegsoft.hubs.ui.screens.search

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.api.utils.SearchUrlHandler
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.common.feedCards.company.CompanyCard
import com.garnegsoft.hubs.ui.common.feedCards.hub.HubCard
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.screens.article.ArticleShort
import kotlinx.coroutines.delay


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
    val context = LocalContext.current

    var showPages by rememberSaveable {
        mutableStateOf(false)
    }
    val mostReadingArticles by viewModel.mostReadingArticles.observeAsState()
    var showCopiedLinkSuggestSnippet by rememberSaveable {
        mutableStateOf(false)
    }
    val copiedLinkSuggestSnippetData by viewModel.clipboardLinkSnippetData.observeAsState()
    var copiedLinkSuggestSnippetDataIdentifier by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Поиск") },
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "")
                    }
                })
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            var searchTextValue by rememberSaveable { mutableStateOf("") }
            val queryIsUrlToHabr by remember {
                derivedStateOf {
                    searchTextValue.trimStart().startsWith("https://habr.com")
                }
            }
            var showClearAllButton by rememberSaveable { mutableStateOf(false) }
            val focusRequester by remember { mutableStateOf(FocusRequester()) }
            val keyboardController = LocalSoftwareKeyboardController.current

            var currentQuery by rememberSaveable {
                mutableStateOf(searchTextValue)
            }

            val articlesLazyListState = rememberLazyListState()
            val articlesFilterContentState = rememberCollapsingContentState()

            val hubsLazyListState = rememberLazyListState()
            val companiesLazyListState = rememberLazyListState()
            val usersLazyListState = rememberLazyListState()

            var doRequestFocus by rememberSaveable {
                mutableStateOf(true)
            }
            val clipboard = LocalClipboardManager.current

            LaunchedEffect(key1 = Unit) {
                if (doRequestFocus) {
                    focusRequester.requestFocus()
                    doRequestFocus = false
                }
                if (!viewModel.mostReadingArticles.isInitialized) {
                    viewModel.loadMostReading()
                }

                delay(1000)
                    if (clipboard.getText() != null && clipboard.getText()!!.trimStart()
                            .startsWith("https://habr.com/")
                    ) {
                        val copiedLink = clipboard.getText()!!.text
                        val result = SearchUrlHandler.recongnizeUrlDataTypeAndIdentifier(copiedLink)
                        if (result.first != SearchUrlHandler.UrlDataType.Unknown) {
                            showCopiedLinkSuggestSnippet = true
                            viewModel.loadCopiedLinkSnippetData(copiedLink)
                            copiedLinkSuggestSnippetDataIdentifier = result.second!!
                        }
                    } else {
                        showCopiedLinkSuggestSnippet = false
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
                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val keyboardOptions = remember(queryIsUrlToHabr) {
                        if (queryIsUrlToHabr) {
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Go
                            )
                        } else {
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Search
                            )
                        }

                    }
                    BasicTextField(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .onFocusEvent {
                                if (it.isCaptured) {
                                    keyboardController?.show()
                                }
                            }
                            .fillMaxWidth(),
                        value = searchTextValue,
                        onValueChange = {
                            searchTextValue = it
                            showClearAllButton = it.isNotEmpty()
                        },
                        textStyle = TextStyle(color = MaterialTheme.colors.onBackground),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = KeyboardActions {
                            if (searchTextValue.isNotBlank()) {
                                keyboardController?.hide()

                                when {
                                    searchTextValue.startsWith(".id") -> {
                                        if (searchTextValue.drop(3).isDigitsOnly())
                                            onArticleClicked(searchTextValue.drop(3).toInt())
                                    }

                                    queryIsUrlToHabr -> {
                                        viewModel.parseUrl(searchTextValue.trimStart())
                                            .let { data ->
                                                val type = data.first
                                                val identifier = data.second

                                                when (type) {
                                                    SearchUrlHandler.UrlDataType.Article -> onArticleClicked(
                                                        identifier!!.toInt()
                                                    )

                                                    SearchUrlHandler.UrlDataType.User -> onUserClicked(
                                                        identifier!!
                                                    )

                                                    SearchUrlHandler.UrlDataType.Hub -> onHubClicked(
                                                        identifier!!
                                                    )

                                                    SearchUrlHandler.UrlDataType.Company -> onCompanyClicked(
                                                        identifier!!
                                                    )

                                                    else -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Не удалось перейти по ссылке.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                    }

                                    else -> {
                                        currentQuery = searchTextValue


                                        viewModel.articlesListModel.editFilter(
                                            ArticlesSearchFilter(
                                                order = (viewModel.articlesListModel.filter.value as ArticlesSearchFilter).order,
                                                query = currentQuery
                                            )
                                        )
                                        showPages = true
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colors.secondary)
                    )
                    if (searchTextValue.isEmpty()) {
                        Text(
                            text = "Введите запрос или ссылку",
                            color = MaterialTheme.colors.secondary.copy(0.5f)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showClearAllButton,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            searchTextValue = ""
                            showClearAllButton = false
                        },

                        ) {
                        Icon(
                            tint = MaterialTheme.colors.secondary,
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = "clear input"
                        )
                    }
                }

            }


            val pagerState = rememberPagerState { 4 }
            if (showPages) {
                val tabs = remember {
                    listOf(
                        "Публикации",
                        "Хабы",
                        "Компании",
                        "Пользователи"
                    )
                }

                HabrScrollableTabRow(
                    pagerState = pagerState, tabs = tabs
                ) { index, title ->
                    when {
                        title.startsWith("Публикации") -> {
                            articlesFilterContentState.show()
                            ScrollUpMethods.scrollLazyList(articlesLazyListState)
                        }

                        title.startsWith("Хабы") -> {
                            ScrollUpMethods.scrollLazyList(hubsLazyListState)
                        }

                        title.startsWith("Компании") -> {
                            ScrollUpMethods.scrollLazyList(companiesLazyListState)
                        }

                        title.startsWith("Пользователи") -> {
                            ScrollUpMethods.scrollLazyList(usersLazyListState)
                        }

                    }
                }
                HorizontalPager(state = pagerState) {
                    when (it) {
                        0 -> {

                            ArticlesListPageWithFilter(
                                listModel = viewModel.articlesListModel,
                                lazyListState = articlesLazyListState,
                                collapsingContentState = articlesFilterContentState,
                                onArticleSnippetClick = onArticleClicked,
                                onArticleAuthorClick = onUserClicked,
                                onArticleCommentsClick = onCommentsClicked,
                                doInitialLoading = false
                            ) { defaultValues, onDismiss, onDone ->
                                ArticlesSearchFilter(
                                    defaultValues = defaultValues,
                                    onDismiss = onDismiss,
                                    onDone = onDone
                                )
                            }

                        }

                        1 -> {
                            val hubs by viewModel.hubs.observeAsState()
                            if (hubs != null) {

                                PagedHabrSnippetsColumn(
                                    modifier = Modifier.fillMaxHeight(),
                                    data = hubs!!,
                                    lazyListState = hubsLazyListState,
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
                                    lazyListState = companiesLazyListState,
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

                            if (users != null) {
                                PagedHabrSnippetsColumn(
                                    lazyListState = usersLazyListState,
                                    data = users!!,
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

            } else {


                var firstCardHeight by remember() { mutableIntStateOf(0) }
                BoxWithConstraints(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Box {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showCopiedLinkSuggestSnippet,
                            enter = slideInVertically { -it }
                            ) {
                            ClipboardLinkSnippet(
                                modifier = Modifier.padding(top = 8.dp),
                                data = copiedLinkSuggestSnippetData,
                                onClick = {
                                    copiedLinkSuggestSnippetData?.let { data ->
                                        when(copiedLinkSuggestSnippetData?.type) {
                                            SearchUrlHandler.UrlDataType.Article -> onArticleClicked(copiedLinkSuggestSnippetDataIdentifier.toInt())
                                            SearchUrlHandler.UrlDataType.User -> onUserClicked(copiedLinkSuggestSnippetDataIdentifier)
                                            SearchUrlHandler.UrlDataType.Hub -> onHubClicked(copiedLinkSuggestSnippetDataIdentifier)
                                            SearchUrlHandler.UrlDataType.Company -> onCompanyClicked(copiedLinkSuggestSnippetDataIdentifier)
                                            else -> {}
                                        }
                                    }

                                }
                            )
                        }

                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = mostReadingArticles != null,
                        enter = slideInVertically { it / 2 }
                    ) {
                        Box(
                            modifier = Modifier.padding(top = with(LocalDensity.current) {
                                (this@BoxWithConstraints.minHeight - firstCardHeight.toDp() - 8.dp - 12.dp - 26.dp).coerceAtLeast(
                                    0.dp
                                ) // these values are paddings for arrangement(8.dp), top of card(12.dp), and size of title(~26.dp)
                            })
                        ) {
                            TitledColumn(
                                title = "Читают сейчас",
                                titleStyle = MaterialTheme.typography.subtitle2.copy(
                                    color = MaterialTheme.colors.onBackground.copy(
                                        0.5f
                                    )
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                mostReadingArticles?.let { mostReading ->
                                    mostReading.forEachIndexed() { index, it ->
                                        Box(
                                            modifier = if (index == 0) Modifier
                                                .onGloballyPositioned {
                                                    firstCardHeight = it.size.height
                                                } else Modifier
                                        ) {
                                            ArticleShort(
                                                article = it,
                                                onClick = {
                                                    onArticleClicked(it.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

}
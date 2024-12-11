package com.garnegsoft.hubs.ui.screens.subscriptions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.dataStore.AuthDataController
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.hub.list.HubSnippet
import com.garnegsoft.hubs.ui.common.feedCards.company.CompanyCard
import com.garnegsoft.hubs.ui.common.feedCards.hub.HubCard
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionManagementScreen(
    onBack: () -> Unit,
    onHubClick: (alias: String) -> Unit,
    onUserClick: (alias: String) -> Unit,
    onCompanyClick: (alias: String) -> Unit
) {

    val userAlias by HubsDataStore.Auth.getValueFlow(LocalContext.current, HubsDataStore.Auth.Alias)
        .collectAsState(null)

    val viewModel = viewModel<SubscriptionsManagementScreenViewModel>()
    val developHubs by viewModel.developFlowHubs.observeAsState()
    val adminHubs by viewModel.adminFlowHubs.observeAsState()
    val designHubs by viewModel.designFlowHubs.observeAsState()
    val managementHubs by viewModel.managementFlowHubs.observeAsState()
    val marketingHubs by viewModel.marketingFlowHubs.observeAsState()
    val popsciHubs by viewModel.popsciFlowHubs.observeAsState()

    val companies by viewModel.companies.observeAsState()
    val authors by viewModel.authors.observeAsState()
    val blocklisted by viewModel.blocklisted.observeAsState()

    val lazyListState = rememberLazyListState()


    LaunchedEffect(userAlias) {
        if (developHubs == null && userAlias != null && userAlias != "") {
            viewModel.loadData(userAlias!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление подписками") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                elevation = 0.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {

                commonSubscriptionsHeader("Хабы")

                developHubs?.let {
                    hubSection(
                        sectionTitle = "Разработка",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }

                adminHubs?.let {
                    hubSection(
                        sectionTitle = "Администрирование",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }

                designHubs?.let {
                    hubSection(
                        sectionTitle = "Дизайн",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }

                managementHubs?.let {
                    hubSection(
                        sectionTitle = "Менеджмент",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }

                marketingHubs?.let {
                    hubSection(
                        sectionTitle = "Маркетинг",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }

                popsciHubs?.let {
                    hubSection(
                        sectionTitle = "Научпоп",
                        onHubClick = onHubClick,
                        items = it.list
                    )
                }


                companies?.let {
                    commonSubscriptionsHeader("Компании")

                    items(
                        items = it.list
                    ) {
                        CompanyCard(
                            company = it,
                            onClick = { onCompanyClick(it.alias) }
                        )
                    }
                }

                authors?.let {
                    commonSubscriptionsHeader("Авторы")

                    items(
                        items = it.list
                    ) {
                        UserCard(
                            user = it,
                            onClick = { onUserClick(it.alias) },
                            indicator = {
                                Text(text = "test")
                            }
                        )
                    }
                }

                blocklisted?.let {
                    commonSubscriptionsHeader("Заблокированные авторы")

                    items(
                        items = it
                    ) {
                        BlockedUserCard(
                            user = it,
                            onClick = { onUserClick(it.alias)}
                        ) {

                            Text(text = "test")
                        }
                    }

                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.commonSubscriptionsHeader(
    headerTitle: String
) {
    item {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                //.background(MaterialTheme.colors.surface.copy(0.9f))

                .padding(start = 8.dp, top = 16.dp, bottom = 0.dp),
            text = headerTitle,
            fontSize = 24.sp,
            fontWeight = FontWeight.W600
        )
    }
}

private fun LazyListScope.hubSection(
    sectionTitle: String,
    onHubClick: (alias: String) -> Unit,
//    onSubscribe: (alias: String) -> Boolean,
//    onUnsubscribe: (alias: String) -> Boolean,
    items: List<HubSnippet>
) {
    item {
        Text(
            modifier = Modifier.padding(top = 4.dp)
                .padding(start = 8.dp),
            text = sectionTitle,
            color = MaterialTheme.colors.onBackground.copy(0.5f),
            fontSize = 18.sp,
            fontWeight = FontWeight.W500
        )
    }

    items(
        items
    ) { snippet ->
        HubCard(
            hub = snippet,
            onClick = { onHubClick(snippet.alias) },
            indicator = {
                Button(
                    onClick = {},
                    elevation = null,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = RatingPositiveColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Подписаться")
                }
            }
        )
    }

}
package com.garnegsoft.hubs.ui.screens.user

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.company.Company
import com.garnegsoft.hubs.api.company.CompanyController
import com.garnegsoft.hubs.api.user.UserController
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import com.garnegsoft.hubs.ui.screens.article.RenderHtml
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor
import com.garnegsoft.hubs.ui.theme.RatingNegativeColor
import com.garnegsoft.hubs.ui.theme.RatingPositiveColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.text.contains

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserProfile(
    isAppUser: Boolean,
    onUserLogout: (() -> Unit)? = null,
    onHubClick: (alias: String) -> Unit,
    onWorkPlaceClick: (alias: String) -> Unit,
    scrollState: ScrollState,
    viewModel: UserScreenViewModel
) {


    val user by viewModel.user.observeAsState()

    LaunchedEffect(key1 = user, block = {
        user?.let {
            if (!viewModel.note.isInitialized) {
                viewModel.loadNote()
            }
            if (!viewModel.subscribedHubs.isInitialized) {
                viewModel.loadSubscribedHubs()
            }
            if (!viewModel.whoIs.isInitialized) {
                viewModel.loadWhoIs()
            }
        }
    })

    val userTransition = updateTransition(user != null)
    val userCardAlphaAnimated by userTransition.animateFloat(
        { tween(durationMillis = 300) }
    ) {
        if (it) 1f else 0f
    }
    val userCardOffsetAnimated by userTransition.animateDp(
        { tween(durationMillis = 300) }
    ) {
        if (it) 0.dp else (-12).dp
    }
    val whoIsMutex = remember { Mutex(true) }
    val isRefreshing by viewModel.isRefreshingUser.observeAsState(false)
    RefreshableContainer(onRefresh = viewModel::refreshUser, refreshing = isRefreshing) {
        user?.let { user ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = userCardAlphaAnimated
                            translationY = userCardOffsetAnimated.toPx()
                        }
                        .padding(8.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(MaterialTheme.colors.surface)
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                modifier = Modifier
                                    .size(65.dp)
                                    .align(Alignment.Center)
                                    .clip(
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(Color.White),
                                contentDescription = ""
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (user.fullname != null)
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "${user.fullname}\n@${user.alias}",
                                    fontWeight = FontWeight.W700,
                                    fontSize = 26.sp,
                                    textAlign = TextAlign.Center
                                )
                            else
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "@${user.alias}",
                                    fontWeight = FontWeight.W700,
                                    fontSize = 26.sp,
                                    textAlign = TextAlign.Center
                                )
                        }
                        if (user.speciality != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = user.speciality,
                                    fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (user.isReadonly) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "Read Only",
                                    fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colors.onSurface.copy(0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = user.score.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.W600,
                                    color =
                                        if (user.score > 0)
                                            RatingPositiveColor
                                        else
                                            if (user.score == 0)
                                                MaterialTheme.colors.onSurface
                                            else
                                                RatingNegativeColor
                                )
                                Text(
                                    text = "Карма",
                                    color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = user.rating.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.W600,
                                    color = DefaultRatingIndicatorColor
                                )
                                Text(
                                    text = "Рейтинг",
                                    color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled)
                                )
                            }
                        }
                        if (!isAppUser && !user.isReadonly) {
                            user.relatedData?.let {
                                var subscribed by rememberSaveable(user?.relatedData?.isSubscribed) {
                                    mutableStateOf(it.isSubscribed)
                                }
                                var blocked by rememberSaveable(user?.isInBlockList) {
                                    mutableStateOf(user.isInBlockList)
                                }

                                val coroutineScope = rememberCoroutineScope()
                                AnimatedVisibility(visible = !blocked) {
                                    var throttleButton by remember { mutableStateOf(false) }
                                    SubscriptionButton(
                                        subscribed = subscribed,
                                        onClick = {
                                            throttleButton = true // disable button and wait for response
                                            subscribed = !subscribed
                                            coroutineScope.launch(Dispatchers.IO) {
                                                subscribed = UserController.subscription(user.alias)
                                                throttleButton = false
                                            }
                                        },
                                        throttle = throttleButton
                                    )
                                }
                                BlockUserButton(blocked = blocked, onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        blocked = !blocked
                                        blocked = UserController.blockListToggle(user.alias)
                                            ?: !blocked // undo last change in case request failed
                                    }
                                })
                            }
                        }

                    }


                    val density = LocalDensity.current
                    val note by viewModel.note.observeAsState()
                    val noteAlphaAnimated by animateFloatAsState(
                        if (note != null) {
                            1f
                        } else {
                            0f
                        },
                        tween(durationMillis = 300, delayMillis = 200)
                    )
                    if (viewModel.note.isInitialized && note?.text != null) {
                        var showNoteDeferred by rememberSaveable { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(10)
                            showNoteDeferred = true
                        }
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = noteAlphaAnimated
                                }
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showNoteDeferred,
                                enter = slideInVertically { -it }
                            ) {
                                if (!isAppUser && note?.text != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(26.dp))
                                            .background(MaterialTheme.colors.surface)
                                            .padding(8.dp)
                                    ) {
                                        BasicTitledColumn(
                                            title = {
                                                Text(
                                                    modifier = Modifier.padding(12.dp),
                                                    text = "Заметка", style = MaterialTheme.typography.subtitle1
                                                )
                                            },
                                            divider = { }
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        start = 12.dp,
                                                        end = 12.dp,
                                                        bottom = 12.dp
                                                    )
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(
                                                        MaterialTheme.colors.onSurface.copy(0.04f)
                                                    )
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    color = MaterialTheme.colors.onSurface.copy(0.75f),
                                                    text = note?.text ?: ""
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    val whoIs by viewModel.whoIs.observeAsState()
                    val hubs by viewModel.subscribedHubs.observeAsState()

                    if ((whoIs != null && (!whoIs?.aboutHtml.isNullOrBlank() || whoIs!!.badges.isNotEmpty()
                                || whoIs!!.invite != null || whoIs!!.contacts.isNotEmpty()))
                        || !hubs?.list.isNullOrEmpty()
                    ) {
                        var showWhoIsDeferred by rememberSaveable { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(10)
                            showWhoIsDeferred = true
                            whoIsMutex.unlock()
                        }
                        val whoIsAlphaAnimated by animateFloatAsState(
                            if (showWhoIsDeferred) 1f else 0f,
                            tween(durationMillis = 150, delayMillis = 50))

                        val whoIsOffsetAnimated by animateDpAsState(if (showWhoIsDeferred) 0.dp else (-12).dp,
                            tween(durationMillis = 150, delayMillis = 50))
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = whoIsAlphaAnimated
                                    translationY = whoIsOffsetAnimated.toPx()
                                }
                                .fillMaxWidth()
//                                .animateContentSize()
                        ) {

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(26.dp))
                                        .background(MaterialTheme.colors.surface)
                                        .padding(8.dp)
                                ) {
                                    BasicTitledColumn(title = {
                                        Text(
                                            modifier = Modifier.padding(12.dp),
                                            text = "Описание",
                                            style = MaterialTheme.typography.subtitle1
                                        )
                                    }, divider = {
//                              Divider()
                                    }) {
                                        Column(
                                            modifier = Modifier.padding(
                                                start = 12.dp,
                                                end = 12.dp,
                                                bottom = 12.dp,
                                                top = 4.dp
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            whoIs?.let { whoIs ->
                                                whoIs.badges.let {
                                                    if (it.isNotEmpty()) {
                                                        TitledColumn(title = "Значки") {
                                                            FlowRow(
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    8.dp
                                                                ),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                it.forEach { Badge(title = it.title) }
                                                            }
                                                        }
                                                    }
                                                }

                                                whoIs.aboutHtml?.let {
                                                    if (it.isNotBlank()) {
                                                        TitledColumn(title = "О Себе") {
                                                            RenderHtml(
                                                                html = it,
                                                                elementSettings = remember {
                                                                    ElementSettings(
                                                                        fontSize = 16.sp,
                                                                        lineHeight = 16.sp,
                                                                        fitScreenWidth = false
                                                                    )
                                                                })
                                                        }
                                                    }
                                                }

                                                whoIs.invite?.let { invite ->
                                                    TitledColumn(title = "Приглашен") {
                                                        val context = LocalContext.current
                                                        val textLinkStyles = commonTextLinkStyles()
                                                        Text(
                                                            text = remember {
                                                                buildAnnotatedString {
                                                                    append("${invite.inviteDate} по приглашению от ")
                                                                    if (invite.inviterAlias != null) {
                                                                        withLink(
                                                                            LinkAnnotation.Url(
                                                                                url = "https://habr.com/ru/users/${invite.inviterAlias}",
                                                                                styles = textLinkStyles,
                                                                                linkInteractionListener = {
                                                                                    val intent = Intent(
                                                                                        Intent.ACTION_VIEW,
                                                                                        Uri.parse("https://habr.com/ru/users/${invite.inviterAlias}")
                                                                                    ).apply {
                                                                                        setPackage(BuildConfig.APPLICATION_ID)
                                                                                    }
                                                                                    context.startActivity(
                                                                                        Intent.createChooser(
                                                                                            intent,
                                                                                            null
                                                                                        )
                                                                                    )
                                                                                }
                                                                            )) {
                                                                            append("@${invite.inviterAlias}")
                                                                        }

                                                                    } else {
                                                                        append("НЛО")
                                                                    }
                                                                }
                                                            },
                                                            style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
                                                        )
                                                    }
                                                }

                                                whoIs.let {
                                                    if (it.contacts.size > 0) {
                                                        TitledColumn(title = "Контакты") {
                                                            Column(
                                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                val context = LocalContext.current
                                                                it.contacts.forEach {
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .background(
                                                                                MaterialTheme.colors.onSurface.copy(
                                                                                    0.04f
                                                                                )
                                                                            )
                                                                            .clickable {
                                                                                context.startActivity(
                                                                                    Intent(
                                                                                        Intent.ACTION_VIEW,
                                                                                        Uri.parse(it.url)
                                                                                    )
                                                                                )
                                                                            }
                                                                            .padding(12.dp),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        var favicon = it.faviconUrl
                                                                        var title = it.title
                                                                        if (favicon == null && (it.url.contains("linkedin.com") || it.url.contains(
                                                                                "linked.in"
                                                                            ))
                                                                        ) {
                                                                            favicon =
                                                                                "https://static.licdn.com/aero-v1/sc/h/al2o9zrvru7aqj8e1x2rzsrca"
                                                                            title = "LinkedIn"
                                                                        }
                                                                        if (favicon != null && favicon.isNotBlank()) {
                                                                            AsyncSvgImage(
                                                                                modifier = Modifier
                                                                                    .size(24.dp)
                                                                                    .clip(
                                                                                        RoundedCornerShape(4.dp)
                                                                                    )
                                                                                    .background(if (MaterialTheme.colors.isLight) Color.Transparent else MaterialTheme.colors.onSurface),
                                                                                data = favicon,
                                                                                revertColorsOnDarkTheme = false,
                                                                                contentDescription = it.title,
                                                                                contentScale = ContentScale.Fit
                                                                            )
                                                                        } else {
                                                                            Icon(
                                                                                modifier = Modifier.size(24.dp),
                                                                                painter = painterResource(id = R.drawable.website_favicon_placeholder),
                                                                                contentDescription = "website",
                                                                                tint = MaterialTheme.colors.onSurface.copy(
                                                                                    0.4f
                                                                                )
                                                                            )
                                                                        }

                                                                        Spacer(modifier = Modifier.width(12.dp))

                                                                        Text(title)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }


                                            }
                                            hubs?.let {
                                                if (it.list.isNotEmpty()) {
                                                    Column() {
                                                        TitledColumn(title = "Состоит в хабах") {
                                                            FlowRow(
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    8.dp
                                                                )
                                                            ) {
                                                                it.list.forEach {
                                                                    HubChip(hub = it) {
                                                                        onHubClick(it.alias)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if (viewModel.moreHubsAvailable) {
                                                            TextButton(
                                                                onClick = {
                                                                    viewModel.loadSubscribedHubs()
                                                                }
                                                            ) {
                                                                Text(
                                                                    "Показать ещё",
                                                                    color = MaterialTheme.colors.secondary,
                                                                    letterSpacing = 0.sp
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
                    var showInfo by rememberSaveable { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        whoIsMutex.withLock {
                            delay(100)
                            showInfo = true
                        }
                    }

                    val showInfoAlphaAnimated by animateFloatAsState(if (showInfo) 1f else 0f)
                    val showInfoOffsetAnimated by animateDpAsState(if (showInfo) 0.dp else (-8).dp)

                    if (viewModel.note.isInitialized && viewModel.whoIs.isInitialized && showInfo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = showInfoAlphaAnimated
                                    translationY = showInfoOffsetAnimated.toPx()
                                }
                                .clip(RoundedCornerShape(26.dp))
                                .background(MaterialTheme.colors.surface)
                                .padding(8.dp)
                        ) {
                            BasicTitledColumn(title = {
                                Text(
                                    modifier = Modifier.padding(12.dp),
                                    text = "Информация", style = MaterialTheme.typography.subtitle1
                                )
                            }, divider = {
//                    Divider()
                            }) {
                                Column(
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 12.dp,
                                        bottom = 12.dp,
                                        top = 4.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    TitledColumn(
                                        title = "Место в рейтинге"
                                    ) {
                                        Text(
                                            text = if (user.ratingPosition == null) "Не участвует" else user.ratingPosition.toString() + "-й",
                                        )
                                    }

                                    user.location?.let {
                                        TitledColumn(title = "Откуда") {
                                            Text(
                                                text = it,
                                            )
                                        }
                                    }

                                    if (user.workPlaces.size > 0) {
                                        TitledColumn(title = "Работает в") {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                user.workPlaces.forEach {
                                                    var company by remember { mutableStateOf<Company?>(null) }
                                                    LaunchedEffect(Unit) {
                                                        if (company == null) withContext(Dispatchers.IO) {
                                                            company = CompanyController.get(it.alias)
                                                        }

                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                MaterialTheme.colors.onSurface.copy(
                                                                    0.04f
                                                                )
                                                            )
                                                            .clickable {
                                                                onWorkPlaceClick(it.alias)
                                                            }
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        AsyncImage(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(
                                                                    RoundedCornerShape(4.dp)
                                                                ),
                                                            model = company?.avatarUrl,
                                                            contentDescription = null
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text(it.title)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (user.birthday != null) {

                                        TitledColumn(title = "Дата рождения") {
                                            Text(
                                                text = user.birthday,
                                            )
                                        }
                                    }

                                    TitledColumn(title = "Дата регистрации") {
                                        Text(
                                            text = user.registrationDate,
                                        )
                                    }

                                    if (user.lastActivityDate != null) {

                                        TitledColumn(title = "Активность") {
                                            Text(
                                                text = user.lastActivityDate
                                            )
                                        }
                                    }


                                }
                            }
                        }
                    }
                    if (isAppUser && showInfo) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .clickable(onClick = onUserLogout!!),
                            elevation = 0.dp,
                            shape = RoundedCornerShape(26.dp),
                            backgroundColor = MaterialTheme.colors.surface,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "Выйти из аккаунта",
                                    color = MaterialTheme.colors.error,
                                    fontWeight = FontWeight.W500
                                )
                            }
                        }
                    }
                }

            }
        }
    }

}

package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HubsDataStore
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.settingsDataStore
import com.garnegsoft.hubs.settingsDataStoreFlow
import com.garnegsoft.hubs.ui.common.HubsFilterChip
import com.garnegsoft.hubs.ui.common.TitledColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


class ArticleScreenSettingsScreenViewModel : ViewModel() {

    val Context.fontSize: Flow<Float?>
        get() {
            return this.settingsDataStoreFlow(HubsDataStore.Settings.Keys.ArticleScreen.FontSize)
    }

    fun Context.setFontSize(size: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsDataStore.edit {
                it.set(HubsDataStore.Settings.Keys.ArticleScreen.FontSize, size)
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleScreenSettingsScreen(
    onBack: () -> Unit,
) {
    val viewModel = viewModel<ArticleScreenSettingsScreenViewModel>()

    val context = LocalContext.current



    val defaultFontSize = MaterialTheme.typography.body1.fontSize.value
    val originalFontSize: Float? by with(viewModel) { context.fontSize.collectAsState(initial = defaultFontSize) }
    var fontSize: Float? by remember { mutableStateOf(null) }
    var lineHeightFactor by remember { mutableStateOf(1.5f) }
    var justifyText by remember { mutableStateOf(false) }
    val state = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        scaffoldState = state,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Публикация")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                elevation = 0.dp
            )
        },
        sheetElevation = 8.dp,
        sheetPeekHeight = 80.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(modifier = Modifier.fillMaxHeight(0.6f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .height(4.dp)
                            .width(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                MaterialTheme.colors.onBackground.copy(0.15f)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TitledColumn(
                        title = "Размер шрифта: ${"%.1f".format(fontSize ?: originalFontSize ?: defaultFontSize)}"
                    ) {

                        Slider(
                            value = fontSize ?: originalFontSize ?: defaultFontSize,
                            valueRange = 12f..32f,
                            steps = 9,
                            onValueChange = {
                                fontSize = it
                            },
                            onValueChangeFinished = {
                                with(viewModel) { context.setFontSize(fontSize!!) }
                            },
                            colors = ArticleScreenSettingsSliderColors
                        )
                    }

                    TitledColumn(
                        title = "Межстрочный интервал"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HubsFilterChip(
                                modifier = Modifier.weight(1f),
                                selected = lineHeightFactor == 1.15f,
                                onClick = { lineHeightFactor = 1.15f }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1.15")
                                }
                            }

                            HubsFilterChip(
                                modifier = Modifier.weight(1f),
                                selected = lineHeightFactor == 1.5f,
                                onClick = { lineHeightFactor = 1.5f }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1.5")
                                }
                            }

                            HubsFilterChip(
                                modifier = Modifier.weight(1f),
                                selected = lineHeightFactor == 1.75f,
                                onClick = { lineHeightFactor = 1.75f }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "1.75")
                                }
                            }

                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                justifyText = !justifyText
                            }
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            text = "Выровнять текст по ширине экрана"
                        )
                        Checkbox(checked = justifyText, onCheckedChange = {
                            justifyText = it
                        })
                    }

                }

            }
        }) {
        Column(
            modifier = Modifier
                .background(
                    if (MaterialTheme.colors.isLight) {
                        MaterialTheme.colors.surface
                    } else {
                        MaterialTheme.colors.background
                    }
                )
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .size(34.dp)
                        .border(
                            width = 2.dp,
                            color = placeholderColor("Boomburum"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp),
                    painter = painterResource(id = R.drawable.user_avatar_placeholder),
                    contentDescription = "",
                    tint = placeholderColor("Boomburum")
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Boomburum", fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onBackground
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Пример публикации",
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = MaterialTheme.colors.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statisticsColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)

                Icon(
                    painter = painterResource(id = R.drawable.clock_icon),
                    modifier = Modifier.size(14.dp),
                    contentDescription = "",
                    tint = statisticsColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "5 мин",
                    color = statisticsColor,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Разработка, Программирование*, Habr, Jetpack Compose*", style = TextStyle(
                    color = Color.Gray,
                    fontWeight = FontWeight.W500
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            val animatedLineHeightFactor by animateFloatAsState(targetValue = lineHeightFactor)
            val textAlign = remember(justifyText) {
                if (justifyText)
                    TextAlign.Justify
                else
                    TextAlign.Left
            }
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Cras pulvinar mattis nunc sed blandit libero volutpat. Lacus sed viverra tellus in hac habitasse platea dictumst vestibulum. Et ligula ullamcorper malesuada proin libero nunc consequat. Vitae justo eget magna fermentum iaculis eu non diam phasellus. Quam adipiscing vitae proin sagittis nisl. Lacus sed viverra tellus in hac habitasse platea dictumst vestibulum. Elit duis tristique sollicitudin nibh. Nisl pretium fusce id velit ut tortor pretium. Mattis aliquam faucibus purus in. In vitae turpis massa sed elementum tempus egestas sed sed.\n" +
                        "\n" +
                        "Est lorem ipsum dolor sit amet consectetur adipiscing. Viverra accumsan in nisl nisi scelerisque eu ultrices. Diam maecenas sed enim ut sem viverra. Id volutpat lacus laoreet non curabitur. Aliquam vestibulum morbi blandit cursus risus. Ac tortor vitae purus faucibus ornare suspendisse sed nisi lacus. Nunc faucibus a pellentesque sit amet porttitor eget. Dolor sed viverra ipsum nunc aliquet bibendum enim facilisis gravida. Urna nec tincidunt praesent semper feugiat nibh sed pulvinar. Felis eget nunc lobortis mattis aliquam faucibus purus in. Tellus in metus vulputate eu. Quam id leo in vitae turpis. Porta non pulvinar neque laoreet suspendisse interdum consectetur libero id. Mauris augue neque gravida in fermentum et. Massa vitae tortor condimentum lacinia quis vel eros donec ac.",
                style = TextStyle(
                    lineHeight = (fontSize ?: originalFontSize ?: defaultFontSize).sp * animatedLineHeightFactor,
                    textAlign = textAlign,
                ),

                fontSize = (fontSize ?: originalFontSize ?: defaultFontSize).sp,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(Modifier.height(4.dp))
        }
    }

}

val ArticleScreenSettingsSliderColors: SliderColors
    @Composable get() = SliderDefaults.colors(
        thumbColor = MaterialTheme.colors.secondary,
        activeTrackColor = MaterialTheme.colors.secondary
    )




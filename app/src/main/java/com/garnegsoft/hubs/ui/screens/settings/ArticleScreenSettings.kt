package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.garnegsoft.hubs.GoogleFontProvider
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.ui.common.BaseTitledDialog
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.screens.article.HubsRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.round


class ArticleScreenSettingsScreenViewModel : ViewModel() {

    val Context.fontSize: Flow<Float>
        get() {
            return HubsDataStore.Settings
                .getValueFlow(this, HubsDataStore.Settings.ArticleScreen.FontSize)
        }

    /**
     * Sets font family in preferences. To set default system font family, set familyName to empty string
     */
    fun setFontFamily(context: Context, familyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            HubsDataStore.Settings.edit(context, HubsDataStore.Settings.ArticleScreen.FontFamily, familyName)
        }
    }

    fun setFontSize(context: Context, size: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            HubsDataStore.Settings.edit(
                context,
                HubsDataStore.Settings.ArticleScreen.FontSize,
                size
            )
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

    val fontFamilyPreference by collectPreferenceAsState(HubsDataStore.Settings.ArticleScreen.FontFamily)


    BottomSheetScaffold(
        scaffoldState = state,
        topBar = {
            HubsTopAppBar(
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

            val notoSerifGoogle = GoogleFont("Noto Serif")
            val merriweatherGoogle = GoogleFont("Merriweather")
            val ptSansGoogle = GoogleFont("PT Sans")

            val defaultFontFamily = FontFamily.Default
            val notoSerifFamily = FontFamily(Font(notoSerifGoogle, GoogleFontProvider))
            val merriweatherFamily = FontFamily(Font(merriweatherGoogle, GoogleFontProvider))
            val ptSansFamily = FontFamily(Font(ptSansGoogle, GoogleFontProvider))

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
                            .background(MaterialTheme.colors.onBackground.copy(0.15f))
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
                                if (round(it) % 2 == 0f) {
                                    fontSize = round(it)
                                }
                            },
                            onValueChangeFinished = {
                                viewModel.setFontSize(context, fontSize!!)
                            },
                            colors = ArticleScreenSettingsSliderColors
                        )
                    }

                    val fontsMap = remember {
                        mapOf(
                            "Системный" to "",
                            notoSerifGoogle.name to notoSerifGoogle.name,
                            merriweatherGoogle.name to merriweatherGoogle.name,
                            ptSansGoogle.name to ptSansGoogle.name,
                            "Другой..." to ""
                        )
                    }
                    var showSetGoogleFontDialog by remember { mutableStateOf(false) }
                    if (showSetGoogleFontDialog) {
                        BaseTitledDialog(
                            onDismiss = {
                                showSetGoogleFontDialog = false
                            },
                            title = "Задать другой шрифт (бета)"
                        ) {
                            Column {
                                Text(
                                    text = "Вы можете указать любой шрифт из открытой библиотеки Google Fonts\n" +
                                            "Имейте ввиду, что некоторые шрифты не поддерживают кириллический набор символов, " +
                                            "в таком случае вместо указанного шрифта будет отображаться установленный в системе\n\n" +
                                            "Просто скопируйте и вставьте название шрифта",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface.copy(0.5f),
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                var textFieldValue by remember { mutableStateOf(TextFieldValue(fontFamilyPreference ?: "")) }
                                TextField(
                                    value = textFieldValue,
                                    onValueChange = {
                                        textFieldValue = it
                                    },
                                    keyboardActions = KeyboardActions {
                                        viewModel.setFontFamily(context, textFieldValue.text)
                                        showSetGoogleFontDialog = false
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.setFontFamily(context, textFieldValue.text)
                                            showSetGoogleFontDialog = false
                                        },
                                        elevation = null
                                    ) {
                                        Text(text = "Готово")
                                    }
                                }

                            }
                        }
                    }

                    SettingsCardItemPicker(
                        title = "Шрифт",
                        items = fontsMap.keys.toList(),
                        pickedItemIndex = if (fontsMap.values.contains(fontFamilyPreference)) {
                            fontsMap.values.indexOf(fontFamilyPreference)
                        } else {
                            if (fontFamilyPreference?.length == 0) 0 else fontsMap.keys.size - 1
                        },
                        onItemPicked = {
                            if (it != fontsMap.keys.size - 1) {
                                viewModel.setFontFamily(context, fontsMap.values.elementAtOrNull(it) ?: "")
                            } else {
                                showSetGoogleFontDialog = true
                            }
                        }
                    )

//                    TitledColumn(
//                        title = "Межстрочный интервал"
//                    ) {
//                        Row(
//                            horizontalArrangement = Arrangement.spacedBy(4.dp)
//                        ) {
//                            HubsFilterChip(
//                                modifier = Modifier.weight(1f),
//                                selected = lineHeightFactor == 1.15f,
//                                onClick = { lineHeightFactor = 1.15f }
//                            ) {
//                                Box(
//                                    modifier = Modifier.fillMaxSize(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(text = "1.15")
//                                }
//                            }
//
//                            HubsFilterChip(
//                                modifier = Modifier.weight(1f),
//                                selected = lineHeightFactor == 1.5f,
//                                onClick = { lineHeightFactor = 1.5f }
//                            ) {
//                                Box(
//                                    modifier = Modifier.fillMaxSize(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(text = "1.5")
//                                }
//                            }
//
//                            HubsFilterChip(
//                                modifier = Modifier.weight(1f),
//                                selected = lineHeightFactor == 1.75f,
//                                onClick = { lineHeightFactor = 1.75f }
//                            ) {
//                                Box(
//                                    modifier = Modifier.fillMaxSize(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(text = "1.75")
//                                }
//                            }
//
//                        }
//                    }
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clip(RoundedCornerShape(8.dp))
//                            .clickable {
//                                justifyText = !justifyText
//                            }
//                            .padding(start = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(end = 4.dp),
//                            text = "Выровнять текст по ширине экрана"
//                        )
//                        Checkbox(checked = justifyText, onCheckedChange = {
//                            justifyText = it
//                        })
//                    }

                }

            }
        }) {

        val googleFont = when (fontFamilyPreference) {
            "" -> null
            null -> null
            else -> {
                GoogleFont(fontFamilyPreference!!)
            }
        }

        val fontFamily = googleFont?.let {
            FontFamily(
                Font(googleFont = googleFont, GoogleFontProvider),
            )
        } ?: FontFamily.Default

        Column(
            modifier = Modifier
                .background(
                    if (MaterialTheme.colors.isLight) {
                        MaterialTheme.colors.surface
                    } else {
                        MaterialTheme.colors.background
                    }
                )
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    model = "https://assets.habr.com/habr-web/img/avatars/012.png", contentDescription = null
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "squada", fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onBackground
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Пример публикации",
                fontSize = animateFloatAsState((fontSize ?: originalFontSize ?: defaultFontSize) + 4f).value.sp,
                fontWeight = FontWeight.W700,
                fontFamily = fontFamily,
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
            HubsRow(
                hubs = listOf(
                    Article.Hub("", false, false, "Разработка", null),
                    Article.Hub("", true, false, "Программирование", Article.Hub.RelatedData(true)),
                    Article.Hub("", false, false, "Habr", null),
                    Article.Hub("", true, false, "Jetpack Compose", null)

                ),
                onHubClicked = { },
                onCompanyClicked = { }
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
                text = AnnotatedString.fromHtml(
                    "Представьте, что у вас есть задача проиллюстрировать читателю, как будет выглядеть тот или иной шрифт. Самое простое, что может прийти на ум это написать все 33 или 26 букв нужного вам алфавита в ряд - абвгдеёж.. или abcdefg.. И хоть такой пример может в целом продемонстрировать, как будут выглядеть буквы заданного шрифта, у читателя может совсем не появится понимание, как этот шрифт будет выглядеть в <i><u>реальном</u></i> тексте и насколько удобно будет читать, используя его. \n" +
                            "И тут мы приходим к тому, что нам нужно продемонстрировать как будут выглядеть буквы и лигатуры шрифта и одновременно с этим показать, каков будет опыт пользователя, использующего этот шрифт. Для решения этой задачи человечество придумало <b>\"панограммы\"</b> - небольшой отрывок текста, содержащий все или как можно больше букв алфавита, призванный показать читателю, каково пользоваться этим шрифтом. \n" +
                            "Из самых известных можно отметить \"The quick brown fox jumps over the lazy dog\" для латинского алфавита. Благодаря тому, что эта панограмма содержит все 26 букв стандартного латинского алфавита, её очень удобно использовать, не только чтобы показать, как будет выглядеть шрифт и слова в нем, но еще и для тестирования печатного оборудования, а также практики \"слепой\" печати."
                ),
                style = TextStyle(
                    lineHeight = (fontSize ?: originalFontSize ?: defaultFontSize).sp * animatedLineHeightFactor,
                    textAlign = textAlign,
                ),
                fontFamily = fontFamily,
                fontSize = animateFloatAsState(fontSize ?: originalFontSize ?: defaultFontSize).value.sp,
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




package com.garnegsoft.hubs.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.common.TitledColumn
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun bottomsheettest() {
    val defaultFontSize = MaterialTheme.typography.body1.fontSize
    var sliderValue by remember { mutableStateOf(defaultFontSize.value) }
    HubsTheme() {
        val state = rememberBottomSheetScaffoldState()
        BottomSheetScaffold(
            scaffoldState = state,
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Публикация")
                    },
                    navigationIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
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
                                    MaterialTheme.colors.onBackground.copy(0.1f)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {

                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            TitledColumn(
                                title = "Размер шрифта: ${"%.1f".format(sliderValue)}"
                            ) {

                                Slider(
                                    value = sliderValue,
                                    valueRange = 12f..32f,
                                    steps = 9,
                                    onValueChange = {
                                        sliderValue = it
                                    },
                                    onValueChangeFinished = {
                                        Log.e("val_fin", sliderValue.toString())
                                    }
                                )
                            }
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
                    text = "Разработка, Программирование*, Habr", style = TextStyle(
                        color = Color.Gray,
                        fontWeight = FontWeight.W500
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Cras pulvinar mattis nunc sed blandit libero volutpat. Lacus sed viverra tellus in hac habitasse platea dictumst vestibulum. Et ligula ullamcorper malesuada proin libero nunc consequat. Vitae justo eget magna fermentum iaculis eu non diam phasellus. Quam adipiscing vitae proin sagittis nisl. Lacus sed viverra tellus in hac habitasse platea dictumst vestibulum. Elit duis tristique sollicitudin nibh. Nisl pretium fusce id velit ut tortor pretium. Mattis aliquam faucibus purus in. In vitae turpis massa sed elementum tempus egestas sed sed.\n" +
                            "\n" +
                            "Est lorem ipsum dolor sit amet consectetur adipiscing. Viverra accumsan in nisl nisi scelerisque eu ultrices. Diam maecenas sed enim ut sem viverra. Id volutpat lacus laoreet non curabitur. Aliquam vestibulum morbi blandit cursus risus. Ac tortor vitae purus faucibus ornare suspendisse sed nisi lacus. Nunc faucibus a pellentesque sit amet porttitor eget. Dolor sed viverra ipsum nunc aliquet bibendum enim facilisis gravida. Urna nec tincidunt praesent semper feugiat nibh sed pulvinar. Felis eget nunc lobortis mattis aliquam faucibus purus in. Tellus in metus vulputate eu. Quam id leo in vitae turpis. Porta non pulvinar neque laoreet suspendisse interdum consectetur libero id. Mauris augue neque gravida in fermentum et. Massa vitae tortor condimentum lacinia quis vel eros donec ac.",
                    style = TextStyle(lineHeight = sliderValue.sp * 1.5f),
                    fontSize = sliderValue.sp,
                    color = MaterialTheme.colors.onBackground
                )

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}


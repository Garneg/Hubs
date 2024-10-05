package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.garnegsoft.hubs.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun NoInternetElement(
    onTryAgain: suspend () -> Boolean,
    onSavedArticles: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val coroutineScope = rememberCoroutineScope()
            val noInternetAnimationComposition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(
                    R.raw.no_internet_element_animations
                )
            )
            val lottieAnimatable = rememberLottieAnimatable()
            val animationSegments = remember {
                arrayListOf(
                    LottieClipSpec.Marker("FirstAppearance"),      // appearance
                    LottieClipSpec.Marker("NoWifi2Spinner"),  // no-wifi to spinner
                    LottieClipSpec.Marker("SpinnerSpin"),     // spinner spinning
                    LottieClipSpec.Marker("Spinner2NoWifi")   // spinner to no-wifi
                )
            }

            LaunchedEffect(noInternetAnimationComposition) {
                lottieAnimatable.animate(
                    composition = noInternetAnimationComposition,
                    clipSpec = animationSegments[0]
                )
            }

            LottieAnimation(
                modifier = Modifier.size(150.dp),
                composition = noInternetAnimationComposition,
                progress = { lottieAnimatable.progress }
            )

//            Spacer(Modifier.height(75.dp))
            Text(
                text = "Нет интернета", style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Сейчас вы не можете читать Хабр, без подключения вам доступны только скачанные ранее статьи. \nПопробуйте установить соединение еще раз, если сеть снова появилась.",
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground.copy(0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onSavedArticles() },
                elevation = null
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.download_done),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Скачанные статьи")
            }

            var isRetryInProgress by remember { mutableStateOf(false) }
            TextButton(
                onClick = {
                    if (!isRetryInProgress) {
                        coroutineScope.launch {
                            isRetryInProgress = true
                            launch {
                                lottieAnimatable.animate(
                                    noInternetAnimationComposition,
                                    clipSpec = animationSegments[1]
                                )
                                lottieAnimatable.animate(
                                    noInternetAnimationComposition,
                                    clipSpec = animationSegments[2],
                                    iterations = LottieConstants.IterateForever,
                                    cancellationBehavior = LottieCancellationBehavior.OnIterationFinish
                                )
                            }

                            val connected = onTryAgain()
                            delay(500) // Without this delay animation stops at first frame for some reason
                            if (!connected) {
                                lottieAnimatable.animate(
                                    noInternetAnimationComposition,
                                    clipSpec = animationSegments[3],
                                    cancellationBehavior = LottieCancellationBehavior.OnIterationFinish,
                                    iterations = 1
                                )
                            }
                            isRetryInProgress = false

                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Попробовать еще раз")
            }
        }
    }
}
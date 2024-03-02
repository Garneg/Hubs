package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun NoInternetElement(
	onTryAgain: () -> Unit,
	onSavedArticles: () -> Unit
) {
	Box(modifier = Modifier
		.fillMaxSize()
		.padding(horizontal = 32.dp), contentAlignment = Alignment.Center){
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState()),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(text = "Нет интернета", style = MaterialTheme.typography.subtitle1,
				textAlign = TextAlign.Center)
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = "Сейчас вы не можете читать Хабр, без подключения вам доступны только скачанные ранее статьи. \nПопробуйте установить соединение еще раз, если сеть снова появилась.",
				style = MaterialTheme.typography.body1,
				color = MaterialTheme.colors.onBackground.copy(0.5f),
				textAlign = TextAlign.Center)
			Spacer(modifier = Modifier.height(32.dp))
			Button(onClick = onSavedArticles, elevation = null) {
				Text(text = "Скачанные статьи")
			}
			TextButton(onClick = onTryAgain) {
				Text(text = "Попробовать еще раз")
			}
		}
	}
}
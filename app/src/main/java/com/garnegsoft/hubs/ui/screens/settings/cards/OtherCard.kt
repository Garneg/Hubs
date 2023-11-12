package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.garnegsoft.hubs.ui.screens.settings.SettingsScreenViewModel

@Composable
fun OtherCard(
	viewModel: SettingsScreenViewModel
) {
	val context = LocalContext.current
	SettingsCard(title = "Другое") {
		SettingsCardItem(title = "Подготовить отчёт об ошибке", onClick = {viewModel.captureLogsAndShare(context)})
		
	}
}
package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.settings.cards.AppearanceSettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.ExperimentalFeaturesSettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.SettingsCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsScreenViewModel : ViewModel() {
	fun getTheme(context: Context): Flow<HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme> {
		return HubsDataStore.Settings
			.getValueFlow(context, HubsDataStore.Settings.Theme.ColorSchemeMode)
			.run { HubsDataStore.Settings.Theme.ColorSchemeMode.mapValues(this) }
	}
	
	fun setTheme(
		context: Context,
		theme: HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme
	) {
		viewModelScope.launch(Dispatchers.IO) {
			HubsDataStore.Settings.edit(
				context,
				HubsDataStore.Settings.Theme.ColorSchemeMode,
				theme.ordinal
			)
		}
	}
}

@Composable
fun SettingsScreen(
	onBack: () -> Unit,
	onArticleScreenSettings: () -> Unit,
) {
	val viewModel = viewModel<SettingsScreenViewModel>()
	val context = LocalContext.current
	
	val theme by viewModel.getTheme(context).collectAsState(initial = null)
	
	Scaffold(
		topBar = {
			TopAppBar(
				elevation = 0.dp,
				title = { Text("Настройки") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.Default.ArrowBack,
							contentDescription = "Назад"
						)
					}
				}
			)
		},
	) {
		
		Column(
			modifier = Modifier
				.padding(it)
				.verticalScroll(rememberScrollState())
				.padding(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			AppearanceSettingsCard(
				viewModel = viewModel,
				onArticleScreenSettings = onArticleScreenSettings
			)
			ExperimentalFeaturesSettingsCard(viewModel = viewModel)
			
		}
	}
	
}

package com.garnegsoft.hubs.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.screens.settings.cards.AppearanceSettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.OtherSettingsCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
	
	
	fun captureLogsAndShare(context: Context) {
		val dateFormat = SimpleDateFormat("ddMMyyHHmm", Locale.US)
		val date = dateFormat.format(Calendar.getInstance().time)
		val name = "Отчет об ошибке $date.txt"
		var authorized: Boolean = false
		runBlocking {
			val flow = HubsDataStore.Auth.getValueFlow(context, HubsDataStore.Auth.Authorized)
			authorized = flow.firstOrNull() ?: false
		}
		val info = """
				Версия приложения: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
				Был авторизован: ${authorized}
				Имя устройства: ${Build.BRAND} ${Build.MODEL} (${android.os.Build.DEVICE})
				Версия ОС (SDK): ${Build.VERSION.SDK_INT}
				Процессор: ${Build.HARDWARE}
			""".trimIndent()
		
		Runtime.getRuntime()
			.exec("logcat -d")
			.inputStream.use { input ->
				
				context.openFileOutput(name, Context.MODE_PRIVATE).use {
					it.write(input.readBytes() + "\n".toByteArray() + info.toByteArray())
					
				}
			}
		val logsPath = File(context.filesDir, "")
		val file = File(logsPath, name)
		val logFileUri = FileProvider.getUriForFile(context, "com.garnegsoft.hubs.fileprovider", file)
		val intent = if (BuildConfig.DEBUG){
			Toast.makeText(context, "Debug mode, logs will be opened", Toast.LENGTH_SHORT).show()
			
			Intent(Intent.ACTION_VIEW).apply {
				this.data = logFileUri
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
		} else {
			Intent(Intent.ACTION_SEND).apply {
				type = "message/rfc822"
				putExtra(Intent.EXTRA_EMAIL, arrayOf("garnegsoft@gmail.com"))
				putExtra(Intent.EXTRA_SUBJECT, "Отчет об ошибке (логи)")
				putExtra(Intent.EXTRA_STREAM, logFileUri)
			}
		}
		context.startActivity(Intent.createChooser(intent, null))
		
		
	}
}

@Composable
fun SettingsScreen(
	onBack: () -> Unit,
	onArticleScreenSettings: () -> Unit,
	onFeedSettings: () -> Unit
) {
	val viewModel = viewModel<SettingsScreenViewModel>()
	
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
				onArticleScreenSettings = onArticleScreenSettings,
				onFeedSettings = onFeedSettings
			)
			
			OtherSettingsCard(viewModel = viewModel)
		}
	}
	
}

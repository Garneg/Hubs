package com.garnegsoft.hubs.ui.screens.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.MostReadingWidget
import com.garnegsoft.hubs.MostReadingWidgetReceiver
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.ui.common.BaseMenuContainer
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import com.garnegsoft.hubs.ui.screens.settings.cards.AppearanceSettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.OtherSettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.SettingsCard
import com.garnegsoft.hubs.ui.screens.settings.cards.SettingsCardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
	onFeedSettings: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val viewModel = viewModel<SettingsScreenViewModel>()
	
	Scaffold(
		modifier = modifier,
		topBar = {
			HubsTopAppBar(
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

			WidgetSettingsCard()

			
			OtherSettingsCard(viewModel = viewModel)
		}
	}
	
}

@Composable
fun WidgetSettingsCard(modifier: Modifier = Modifier) {
	val context = LocalContext.current
	SettingsCard(
		title = "Виджет читают сейчас"
	) {
		var showWidgetCard by rememberSaveable { mutableStateOf(false) }
		val context = LocalContext.current
		val coroutineScope = rememberCoroutineScope()

		LaunchedEffect(Unit) {
			showWidgetCard = GlanceAppWidgetManager(context).getGlanceIds(MostReadingWidget::class.java).size == 0
		}
		if (showWidgetCard) {
			SettingsCardItem(
				title = "Добавить виджет",
				onClick = {
					val widgetManager = AppWidgetManager.getInstance(context)
					val widgetProvider = ComponentName(context, MostReadingWidgetReceiver::class.java)
					if (Build.VERSION.SDK_INT >= 26 && widgetManager.isRequestPinAppWidgetSupported) {
						widgetManager.requestPinAppWidget(widgetProvider, null, null)
					}
				},
				trailingIcon = {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = null
					)
				})
		} else {
			if (Build.VERSION.SDK_INT >= 31) {
				val themeMode by collectPreferenceAsState(HubsDataStore.Settings.Widget.ThemeMode)
				SettingsCardItemPicker(
					title = "Темы:",
					items = listOf("Адаптивная (Material You)", "Как в приложении"),
					pickedItemIndex = themeMode ?: 0,
					onItemPicked = {
						coroutineScope.launch(Dispatchers.IO) {
							HubsDataStore.Settings.Widget.ThemeMode.edit(context = context, it)
							MostReadingWidgetReceiver().glanceAppWidget.updateAll(context)
						}
					}
				)
			}


		}



	}
}

@Composable
fun SettingsCardItemPicker(
	modifier: Modifier = Modifier,
	title: String,
	enabled: Boolean = true,
	items: List<String>,
	onItemPicked: (index: Int) -> Unit,
	pickedItemIndex: Int
) {
	var showSelectThemeMenu by remember { mutableStateOf(false) }
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(10.dp))
//				.clickable {
//					showSelectThemeMenu = true
//				}
			.padding(start = 4.dp)
			.height(48.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		val showMenuTransition = updateTransition(showSelectThemeMenu)
		Text(modifier = Modifier.weight(1f), text = title)
		Box {
			Row(
				modifier = Modifier
					.clip(RoundedCornerShape(8.dp))
					.clickable { showSelectThemeMenu = true }
					.padding(vertical = 6.dp)
					.padding(start = 12.dp, end = 4.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = items[pickedItemIndex],
					textAlign = TextAlign.End
				)
				Spacer(modifier = Modifier.width(4.dp))
				val rotationAnimated by showMenuTransition.animateFloat { if (it) 180f else 0f }
				Icon(
					modifier = Modifier
						.graphicsLayer {
							rotationZ = rotationAnimated
						},
					imageVector = Icons.Default.ArrowDropDown, contentDescription = null
				)

			}

			val scaleFactor by showMenuTransition.animateFloat { if (it) 1f else 0f }
			val offsetFactor by showMenuTransition.animateFloat { if (it) 0f else 1f }
			if (showSelectThemeMenu || showMenuTransition.currentState || showMenuTransition.targetState) {
				Popup(
					popupPositionProvider = object : PopupPositionProvider {
						override fun calculatePosition(
							anchorBounds: IntRect,
							windowSize: IntSize,
							layoutDirection: LayoutDirection,
							popupContentSize: IntSize
						): IntOffset {
							return IntOffset(
								anchorBounds.right - popupContentSize.width,
								anchorBounds.bottom
							)
						}

					},
					onDismissRequest = {
						showSelectThemeMenu = false
					},
					properties = PopupProperties(true)
				) {
					Box(
						modifier = Modifier
							.offset {
								IntOffset(x = 8.dp.roundToPx(), y = -8.dp.roundToPx())
							}
							.graphicsLayer {
								alpha = scaleFactor
								translationY = -8.dp.roundToPx() * offsetFactor
//										scaleY = scaleFactor
							}
					) {
						BaseMenuContainer() {
							items.forEachIndexed { index, item ->
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.heightIn(48.dp)
										.clickable(
											enabled = enabled,
										) {
											onItemPicked(index)
											showSelectThemeMenu = false
										}
										.padding(horizontal = 16.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										modifier = Modifier.weight(1f),
										text = item,
										style = MaterialTheme.typography.body1
									)
									if (index == pickedItemIndex) {
										Icon(
											modifier = Modifier.size(20.dp),
											imageVector = Icons.Default.Done,
											contentDescription = "Выбрано"
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

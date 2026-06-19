package com.garnegsoft.hubs.ui.screens.settings.screens

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.garnegsoft.hubs.GoogleFontProvider
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.api.dataStore.collectPreferenceAsState
import com.garnegsoft.hubs.ui.common.HubsTopAppBar
import com.garnegsoft.hubs.ui.common.MenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextToSpeechSettingsScreen(
    onBack: () -> Unit,
    allowDisplayFullContent: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val preferredEngine by collectPreferenceAsState(HubsDataStore.Settings.TextToSpeech.EnginePackageName)
    val preferredVoice by collectPreferenceAsState(HubsDataStore.Settings.TextToSpeech.Voice)
    var isTTSSpeaking by remember { mutableStateOf(false) }

    var ttsInitialized by remember(preferredEngine) { mutableStateOf(false) }
    val tts = remember(preferredEngine) {
        TextToSpeech(context, {
            if (it == TextToSpeech.SUCCESS) {
                ttsInitialized = true
            }
        }, if (!preferredEngine.isNullOrBlank()) preferredEngine else null).apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    isTTSSpeaking = false
                }

                override fun onError(utteranceId: String?) {
                    isTTSSpeaking = false
                }

                override fun onStart(utteranceId: String?) {
                    isTTSSpeaking = true
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    isTTSSpeaking = false
                    super.onStop(utteranceId, interrupted)
                }

            })
        }
    }
    var enginesList by remember() { mutableStateOf(emptyList<TextToSpeech.EngineInfo>()) }

    var ttsVoicesList by remember { mutableStateOf<List<Voice>>(emptyList()) }
    LaunchedEffect(ttsInitialized) {
        if (ttsInitialized) {
            ttsVoicesList = tts.voices.filter { it.name.contains("ru") }
            enginesList = tts.engines

        }
    }
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Expanded)
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    var sheetIntHeight by remember { mutableStateOf(0) }


    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            HubsTopAppBar(title = {
                Text(text = "Озвучка статей")
            }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = null,
                    )
                }
            })
        },
        sheetGesturesEnabled = false,
        sheetContent = {
            Column(
                modifier = Modifier
                    .onPlaced {
                        sheetIntHeight = it.size.height
                    }
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                val preferredFontFamily by collectPreferenceAsState(HubsDataStore.Settings.ArticleScreen.FontFamily)
                val fontFamily = preferredFontFamily?.let {

                    val googleFont = GoogleFont(preferredFontFamily!!)
                    FontFamily(Font(googleFont, GoogleFontProvider))
                } ?: FontFamily.Default

                Text(
                    text = "Это тест озвучки статей, послушайте, подходят ли вам эти настройки",
                    fontSize = 18.sp,
                    fontFamily = fontFamily,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = MaterialTheme.colors.secondary)
                            .size(64.dp)
                            .clickable {
                                if (isTTSSpeaking) {
                                    tts.stop()
                                } else {
                                    tts.speak(
                                        "Это тест озвучки статей, послушайте, подходят ли вам эти настройки",
                                        TextToSpeech.QUEUE_FLUSH, Bundle.EMPTY, "tts_test"
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTTSSpeaking) {
                            Icon(
                                painterResource(R.drawable.stop),
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSecondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(65.dp))
            }
        }
    ) { scaffoldPadding ->
        if (allowDisplayFullContent) {
            val density = LocalDensity.current
            Column(
                modifier = Modifier
                    .padding(bottom = (sheetIntHeight.toFloat() / density.density).dp)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Text To Speech движки:", fontWeight = FontWeight.W600, fontSize = 20.sp
                )
                AnimatedVisibility(
                    visible = enginesList.isNotEmpty(), enter = fadeIn(), exit = fadeOut()
                ) {
                    FlowRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        enginesList.forEach { engine ->
                            TTSChip(
                                onClick = {
                                    tts.stop()
                                    coroutineScope.launch(Dispatchers.IO) {
                                        HubsDataStore.Settings.TextToSpeech.EnginePackageName.edit(context, engine.name)
                                    }
                                },
                                leadingIcon = {
                                    val pm = remember(context) { context.packageManager }
                                    val iconBitmap =
                                        remember(pm) { pm.getDrawable(engine.name, engine.icon, null)?.toBitmap() }
                                    iconBitmap?.let {
                                        Icon(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            tint = Color.Unspecified
                                        )
                                    }

                                },
                                trailingIcon = if ((preferredEngine.isNullOrBlank() && tts.defaultEngine == engine.name) || preferredEngine == engine.name) {
                                    {
                                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                                    }
                                } else {
                                    null
                                }) {
                                Text(
                                    text = engine.label,
                                    fontWeight = FontWeight.W500,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val density = LocalDensity.current

                AnimatedVisibility(
                    visible = ttsVoicesList.isNotEmpty(),
                    enter = fadeIn() + slideInVertically { (-20 * density.density).roundToInt() },
                    exit = fadeOut()
                ) {
                    Column {
                        Text(
                            modifier = Modifier.padding(vertical = 8.dp),
                            text = "Голоса:",
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp
                        )

                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colors.surface)
                                .verticalScroll(rememberScrollState())
                        ) {
                            ttsVoicesList.forEach { voice ->

                                MenuItem(
                                    title = if (preferredVoice != null && voice.name == preferredVoice)
                                        voice.name + " (выбрано)"
                                    else
                                        voice.name,
                                    onClick = {
                                        tts.stop()
                                        tts.voice = voice
                                        coroutineScope.launch(Dispatchers.IO) {
                                            HubsDataStore.Settings.TextToSpeech.Voice.edit(context, voice.name)
                                        }
                                    },
                                    icon = {
                                        if (voice.isNetworkConnectionRequired) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cloud),
                                                contentDescription = null
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(id = R.drawable.cloud_off),
                                                contentDescription = null
                                            )
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TTSChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .animateContentSize()
            .heightIn(min = 36.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon?.let {
            leadingIcon.invoke()
            Spacer(modifier = Modifier.width(8.dp))
        }
        content()
        Spacer(modifier = Modifier.width(8.dp))
        trailingIcon?.let {
            trailingIcon.invoke()
        }
    }
}
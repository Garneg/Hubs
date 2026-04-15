package com.garnegsoft.hubs.ui.screens.article.tts

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.tts.HubsTTSService
import com.garnegsoft.hubs.api.tts.TTSBinder
import com.garnegsoft.hubs.ui.screens.article.ArticleScreenViewModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Locale

// TODO: REMOVE THIS GARBAGE ASAP
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TtsTestDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    binder: TTSBinder?
) {
    val articleScreenViewModel = viewModel<ArticleScreenViewModel>()
    val articleHtmlDoc =
        remember(articleScreenViewModel.article.value) {
            Jsoup.parse(
                articleScreenViewModel.article.value?.contentHtml ?: ""
            )
        }

    var ttsEnginePackage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var textToSpeech by remember(ttsEnginePackage) { mutableStateOf<TextToSpeech?>(null) }
    var ttsEnginesList by remember { mutableStateOf(emptyList<TextToSpeech.EngineInfo>()) }
    var ttsCreatedSuccessfully by remember(ttsEnginePackage) { mutableStateOf(false) }
    LaunchedEffect(textToSpeech, ttsEnginePackage) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(
                context, {
                    if (it == TextToSpeech.ERROR)
                        textToSpeech = null
                    else {
                        ttsCreatedSuccessfully = true
                        textToSpeech!!.language = Locale("ru")
                    }
                },
                ttsEnginePackage
            )
        }
        //val article = articleScreenViewModel.article.value

        //textToSpeech?.speak(article!!.title, TextToSpeech.QUEUE_FLUSH, null, "title")
    }
    LaunchedEffect(ttsCreatedSuccessfully) {
        if (ttsCreatedSuccessfully) {
            ttsEnginesList = textToSpeech!!.engines
        }
    }
    if (show) {
        Dialog(
            onDismissRequest = onDismissRequest
        ) {
            Surface(
                modifier = modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colors.surface)
                    .padding(32.dp)
            ) {
                // I won't do this in final variant, I promise to myself 🙏


                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text("Озвучка публикации", fontWeight = FontWeight.W700, fontSize = 24.sp)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colors.secondary.copy(0.05f))
                                    .padding(8.dp),
                                text = "Эта статья содержит ${
                                    articleHtmlDoc.body().text().length
                                } символов (макс ${TextToSpeech.getMaxSpeechInputLength()}/запрос)",
                                color = MaterialTheme.colors.onSurface.copy(0.75f)
                            )
                        }
                        Text("Движки:", fontWeight = FontWeight.W500, fontSize = 18.sp)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {

                            ttsEnginesList.forEach {
                                Text(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            ttsEnginePackage = it.name
                                            textToSpeech?.stop()
                                        }
                                        .sizeIn(maxWidth = 200.dp)
                                        .background(MaterialTheme.colors.secondary.copy(0.1f))
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    text = it.label,
                                    fontWeight = if (ttsEnginePackage == it.name || (ttsEnginePackage == null && it.name == textToSpeech?.defaultEngine)) FontWeight.W500 else FontWeight.W400
                                )
                            }

                        }
                        Text("Голоса:", fontWeight = FontWeight.W500, fontSize = 18.sp)
                        if (ttsCreatedSuccessfully) {

                            textToSpeech?.let { tts ->
                                var voiceSelected by remember { mutableStateOf(tts.voice.name) }
                                tts.voices.filter { it.locale.language == Locale("ru").language }
                                    .forEach {
                                        Text(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    textToSpeech?.setVoice(it)
                                                    voiceSelected = it.name
                                                }
                                                .background(MaterialTheme.colors.secondary.copy(0.1f))
                                                .padding(vertical = 2.dp, horizontal = 8.dp),
                                            text = it.name,
                                            fontWeight = if (voiceSelected == it.name) FontWeight.W500 else FontWeight.W400
                                        )
                                    }
                            }
                        }


                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                textToSpeech?.let { tts ->
                                    binder?.loadChunks(
                                        articleHtmlDoc.text()
                                            .split(' '),
                                    )
                                }
                            },
                            enabled = ttsCreatedSuccessfully
                        ) {
                            Text("Load")
                        }

                        OutlinedButton(
                            onClick = {
                                binder?.play()
                            },
                            enabled = ttsCreatedSuccessfully
                        ) {
                            Text("Играть дальше")
                            ArticleContentSplitter.breakIntoPieces(Element("p"))
                        }
                        Row {
                            OutlinedButton(
                                onClick = {
                                    binder?.stop()
                                },
                                enabled = ttsCreatedSuccessfully
                            ) {
                                Text("Стоп")
                                ArticleContentSplitter.breakIntoPieces(Element("p"))
                            }

                            OutlinedButton(
                                onClick = {
                                    binder?.pause()
                                },
                                enabled = ttsCreatedSuccessfully
                            ) {
                                Text("pause")
                                ArticleContentSplitter.breakIntoPieces(Element("p"))
                            }
                        }




                        Row {
                            Button(
                                onClick = {
                                    binder?.seek(-5)
                                }
                            ) {
                                Text("Seek back")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    binder?.seek(5)
                                }
                            ) {
                                Text("Seek forward")
                            }
                        }

                    }

                }
            }
        }
    }
}
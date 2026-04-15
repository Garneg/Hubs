package com.garnegsoft.hubs.api.tts

import android.os.Binder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlin.math.ceil

class TTSBinder(
    val tts: TextToSpeech,
) : Binder() {
    fun speak(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, message.take(5).hashCode().toString())
    }

    private var playbackChunks = emptyList<String>()
    private var chunkIndexPlaying = 0

    fun loadChunks(chunks: List<String>) {
        tts.stop()
        playbackChunks = chunks
        chunkIndexPlaying = 0
    }

    private fun scheduleChunks() {
        if (playbackChunks.isNotEmpty()) {
            tts.speak(playbackChunks[chunkIndexPlaying], TextToSpeech.QUEUE_ADD, null, chunkIndexPlaying.toString())
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    Log.i("ttss", "onDone index:$utteranceId")

                    chunkIndexPlaying++
                    if (chunkIndexPlaying <= playbackChunks.lastIndex) {
                        tts.speak(
                            playbackChunks[chunkIndexPlaying],
                            TextToSpeech.QUEUE_ADD,
                            null,
                            chunkIndexPlaying.toString()
                        )
                    }
                }

                override fun onError(utteranceId: String?) {
                    Log.e("ttss", "onError:$utteranceId")
                }

                override fun onStart(utteranceId: String?) {
                    Log.i("ttss", "playing chunk $chunkIndexPlaying")
                }
            })
        }
    }

    fun pause() {
        tts.stop()
    }

    fun play() {
        scheduleChunks()
    }

    fun stop() {
        tts.stop()
        chunkIndexPlaying = 0
    }

    fun seek(chunksStep: Int) {
        tts.stop()
        chunkIndexPlaying = (chunkIndexPlaying + chunksStep).coerceIn(0, playbackChunks.lastIndex)
        scheduleChunks()
    }

}
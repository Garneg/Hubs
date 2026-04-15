package com.garnegsoft.hubs.api.tts

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Parcel
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.BasePlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.FileDescriptor
import java.util.Locale

class HubsTTSService : MediaSessionService() {

    var tts: TextToSpeech? = null
    var ttsInitialized = false

    var mediaSession: MediaSession? = null
    override fun onBind(p0: Intent?): IBinder {
        return TTSBinder(tts!!)
    }

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.i("TTS", "TTS Initialized")
                ttsInitialized = true

            }
        }

        val player = object : SimpleBasePlayer(Looper.getMainLooper()) {
            override fun getState(): SimpleBasePlayer.State {
                return State.Builder()
                    .setPlayWhenReady(true, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                    .build()
            }


        }


        mediaSession = MediaSession.Builder(this@HubsTTSService, player)
            .setId("hubs_article_tts")
            .build()

        mediaSession?.run {
            val mediaItem =
                MediaItem.Builder()
                    .setMediaId("media-1")
                    .setUri("dummy://aboba".toUri())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist("David Bowie")
                            .setTitle("Heroes")
                            .build()
                    )
                    .build()

            player.setMediaItem(mediaItem)
            player.addMediaItem(0, mediaItem)
                player.addMediaItem(1, mediaItem)

            player.prepare()
            player.play()

        }
        Toast.makeText(this@HubsTTSService, "Service created!", Toast.LENGTH_SHORT).show()
        tts?.let {
            Log.i("TTS", "TTS is not null")
        }
    }

    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        Log.i("media_session_controller", p0.packageName)
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this@HubsTTSService, "Service destroyed!", Toast.LENGTH_SHORT).show()
        tts?.speak("i will be back", TextToSpeech.QUEUE_FLUSH, null, "42")
    }
}


package com.garnegsoft.hubs.api.tts

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import android.os.Parcel
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.BasePlayer
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_GET_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_GET_METADATA
import androidx.media3.common.Player.COMMAND_INVALID
import androidx.media3.common.Player.COMMAND_PLAY_PAUSE
import androidx.media3.common.Player.COMMAND_PREPARE
import androidx.media3.common.Player.COMMAND_SET_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import androidx.media3.common.Player.COMMAND_STOP
import androidx.media3.common.Player.PLAYBACK_SUPPRESSION_REASON_NONE
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.audio.AudioFocusRequestCompat
import androidx.media3.common.audio.AudioManagerCompat
import androidx.media3.common.audio.AudioManagerCompat.AUDIOFOCUS_GAIN
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionResult
import com.garnegsoft.hubs.MainActivity
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.HabrDataParser
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.jsoup.Jsoup
import java.io.FileDescriptor
import java.util.Date
import java.util.Locale
import java.util.concurrent.FutureTask


data object TTSServiceCommands {
    val ACTION_LOAD_ARTICLE = "load_article"
}

class HubsTTSService : MediaSessionService() {

    var tts: TextToSpeech? = null
    var ttsInitialized = false

    var player: TTSPlayer? = null

    var mediaSession: MediaSession? = null


    override fun onBind(p0: Intent?): IBinder? {
        return super.onBind(p0)
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setShowNotificationForIdlePlayer(SHOW_NOTIFICATION_FOR_IDLE_PLAYER_NEVER)



        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {


//                NotificationManagerCompat.from(this@HubsTTSService).createNotificationChannel(
//                    NotificationChannelCompat.Builder("hubs_article_tts", NotificationManager.IMPORTANCE_LOW)
//                        .setName("Hubs text to speech")
//                        .build()
//                )
//
//                val notification = NotificationCompat.Builder(this@HubsTTSService, "hubs_article_tts")
//                    .setSmallIcon(R.drawable.notification_default_icon)
//                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.logo2))
//                    .setContentTitle("Pending TTS...")
//                    .setContentText("by hubs")
//                    .setStyle(MediaStyleNotificationHelper.MediaStyle(mediaSession!!))
//                    .build()
//
//                if (Build.VERSION.SDK_INT >= 29) {
//                    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
//                }

                Toast.makeText(this@HubsTTSService, "Service created!", Toast.LENGTH_SHORT).show()
                tts?.let {
                    Log.i("TTS", "TTS is not null")
                }
            }
        }

        Log.i("TTS", "TTS Initialized")
        ttsInitialized = true
        val audioManager = AudioManagerCompat.getAudioManager(this)

        player = TTSPlayer(tts!!, audioManager)
        player?.loadChunks(buildList { addAll(LoremIpsum(200).values.first().split(" ")) })
        player?.addMediaItem(
            MediaItem.Builder()
                .setMediaId("lorem ipsum")
                .build()
        )

        val activityPendingIntent = PendingIntent.getActivity(
            this, 676767,
            Intent(Intent.ACTION_VIEW).apply {
                setClass(
                    this@HubsTTSService,
                    MainActivity::class.java
                )
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this@HubsTTSService, player!!)
            .setId("hubs_article_tts" + System.currentTimeMillis().toString())
            .setSessionActivity(activityPendingIntent)

            .setCallback(
                object : MediaSession.Callback {

                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo
                    ): MediaSession.ConnectionResult {
                        val customCommands =
                            SessionCommands.Builder()
                                .apply { player!!.availableCommandsList.forEach { add(it) } }
                                .add(SessionCommand(TTSServiceCommands.ACTION_LOAD_ARTICLE, Bundle.EMPTY))
                                .build()
                        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                            .setAvailableSessionCommands(customCommands)
                            .build()
                    }

                    override fun onCustomCommand(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        customCommand: SessionCommand,
                        args: Bundle
                    ): ListenableFuture<SessionResult> {
                        if (customCommand.customAction == TTSServiceCommands.ACTION_LOAD_ARTICLE) {
                            player!!.prepareToLoading()
                            lifecycleScope.launch(Dispatchers.IO) {
                                val article = ArticleController.get(args.getInt("id"))
                                val snippet = ArticleController.getSnippet(args.getInt("id"))
                                withContext(Dispatchers.Main) {
                                    article?.let {
                                        val articleText = buildList {
                                            add(article.title)

                                            Jsoup.parse(article.contentHtml).body()
                                                .child(0)
                                                .children()
                                                .forEach {
                                                    when {
                                                        it.tagName() == "p" -> add(it.text())
                                                        it.tagName().startsWith("h") && it.tagName().length == 2 -> add(
                                                            it.text()
                                                        )

                                                        it.tagName() == "ol" -> add(it.text())
                                                        it.tagName() == "ul" -> add(it.text())
                                                        it.tagName() == "blockquote" -> add("Цитата — " + it.text())
                                                        it.tagName() == "div" && it.childrenSize() == 1 && it.child(0)
                                                            .className() == "table" -> add("Таблица пропущена")
                                                    }
                                                }
                                        }
                                        player?.loadChunks(articleText)
                                        player?.setMediaMetadata(
                                            TTSPlayer.ArticleMetadata(
                                                title = it.title,
                                                author = it.author?.alias ?: "Unknown",
                                                thumbnailUri = snippet?.imageUrl,
                                                articleId = it.id,
                                                offline = false
                                            )
                                        )
                                    }
                                    player!!.endLoading()
                                }
                            }
                        }

                        return super.onCustomCommand(session, controller, customCommand, args)
                    }
                }
            )
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this).apply { setSmallIcon(R.drawable.pin) }
        )


    }


    override fun onGetSession(p0: MediaSession.ControllerInfo): MediaSession? {
        Log.i("media_session_controller", p0.packageName)

        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        Toast.makeText(this@HubsTTSService, "Service destroyed!", Toast.LENGTH_SHORT).show()
        tts?.speak("i will be back", TextToSpeech.QUEUE_FLUSH, null, "42")
    }
}


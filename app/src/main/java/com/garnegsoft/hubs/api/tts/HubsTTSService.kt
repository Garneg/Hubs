package com.garnegsoft.hubs.api.tts

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
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
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.BasePlayer
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
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
import androidx.media3.common.Player.STATE_IDLE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.garnegsoft.hubs.R
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

    var player: TTSPlayer? = null

    var mediaSession: MediaSession? = null
    override fun onBind(p0: Intent?): IBinder? {
        return super.onBind(p0)
//        return TTSBinder(tts!!)
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setShowNotificationForIdlePlayer(SHOW_NOTIFICATION_FOR_IDLE_PLAYER_ALWAYS)

        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.i("TTS", "TTS Initialized")
                ttsInitialized = true

                player = TTSPlayer(tts!!)
                player?.loadChunks(buildList { LoremIpsum(200).values.first().split(" ").forEach { add(it) } })


                mediaSession = MediaSession.Builder(this@HubsTTSService, player!!)
                    .setCallback(object : MediaSession.Callback {
                    })
                    .setId("hubs_article_tts")
                    .build()

                Toast.makeText(this@HubsTTSService, "Service created!", Toast.LENGTH_SHORT).show()
                tts?.let {
                    Log.i("TTS", "TTS is not null")
                }
            }
        }

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this)
        )


    }

    @UnstableApi
    class CustomPlayer() : SimpleBasePlayer(Looper.getMainLooper()) {
        override fun getState(): State {
            return State.Builder()
                .setPlayWhenReady(true, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
                .build()
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

class TTSPlayer(
    val tts: TextToSpeech
) : Player {

    private var chunks: List<String> = emptyList()
    private var currentChunkIndex: Int = 0

    private val listeners: MutableList<Player.Listener> = mutableListOf()
    private val mediaItems: MutableList<MediaItem> = mutableListOf()

    /**
     * Loading chunks that tts will play. Each chunk's length must be less than maximum chars number for TextToSpeech engine
     */
    fun loadChunks(chunks: List<String>) {
        tts.stop()
        currentChunkIndex = 0
        this.chunks = chunks
    }

    override fun getApplicationLooper(): Looper {
        return Looper.getMainLooper()
    }

    override fun addListener(listener: Player.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    override fun setMediaItems(mediaItems: List<MediaItem>) {

    }

    override fun setMediaItems(
        mediaItems: List<MediaItem>,
        resetPosition: Boolean
    ) {
    }

    override fun setMediaItems(
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ) {
    }

    override fun setMediaItem(mediaItem: MediaItem) {
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        mediaItems.add(mediaItem)
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        mediaItems.add(index, mediaItem)
    }

    override fun addMediaItems(mediaItems: List<MediaItem>) {
        this.mediaItems.addAll(mediaItems)
    }

    override fun addMediaItems(
        index: Int,
        mediaItems: List<MediaItem>
    ) {
        this.mediaItems.addAll(index, mediaItems)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
    }

    override fun replaceMediaItems(
        fromIndex: Int,
        toIndex: Int,
        mediaItems: List<MediaItem>
    ) {
    }

    override fun removeMediaItem(index: Int) {
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
    }

    override fun clearMediaItems() {
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return when (command) {
            COMMAND_PLAY_PAUSE -> true
            COMMAND_STOP -> true
            else -> false
        }
    }

    override fun canAdvertiseSession(): Boolean {
        return true
    }

    @OptIn(UnstableApi::class)
    override fun getAvailableCommands(): Player.Commands {
        return Player.Commands.Builder()
            .add(COMMAND_PLAY_PAUSE)
            .add(COMMAND_STOP)
            .add(COMMAND_GET_METADATA)
            .add(COMMAND_PREPARE)
            .add(COMMAND_SET_MEDIA_ITEM)
            .add(COMMAND_GET_CURRENT_MEDIA_ITEM)
//            .add(COMMAND_SET_SPEED_AND_PITCH)
            .build()
    }

    override fun prepare() {

    }

    override fun getPlaybackState(): Int {
        return Player.STATE_READY
    }

    override fun getPlaybackSuppressionReason(): Int {
        return 0
    }

    override fun isPlaying(): Boolean {
        return tts.isSpeaking
    }

    override fun getPlayerError(): PlaybackException? {
        return null
    }

    override fun play() {

        if (chunks.isNotEmpty()) {
            if (currentChunkIndex >= chunks.lastIndex)
                currentChunkIndex = 0
            listeners.forEach {
                it.onIsPlayingChanged(true)
            }
            tts.speak(chunks[currentChunkIndex], TextToSpeech.QUEUE_ADD, null, currentChunkIndex.toString())
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    Log.i("ttss", "onDone index:$utteranceId")

                    currentChunkIndex++
                    if (currentChunkIndex <= chunks.lastIndex) {
                        tts.speak(
                            chunks[currentChunkIndex],
                            TextToSpeech.QUEUE_ADD,
                            null,
                            chunks.toString()
                        )
                    }
                }

                override fun onError(utteranceId: String?) {
                    Log.e("ttss", "onError:$utteranceId")
                }

                override fun onStart(utteranceId: String?) {
                    Log.i("ttss", "playing chunk $currentChunkIndex")
                }
            })
        }
    }

    override fun pause() {
        tts.stop()
        listeners.forEach {
            it.onIsPlayingChanged(false)
        }
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {

    }

    override fun getPlayWhenReady(): Boolean {
        return true
    }

    override fun setRepeatMode(repeatMode: Int) {

    }

    override fun getRepeatMode(): Int {
        return Player.REPEAT_MODE_OFF
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        // nothing
    }

    override fun getShuffleModeEnabled(): Boolean {
        return false
    }

    override fun isLoading(): Boolean {
        return false
    }

    override fun seekToDefaultPosition() {
        stop()
        play()
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {

    }

    override fun seekTo(positionMs: Long) {

    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {

    }

    override fun getSeekBackIncrement(): Long {
        return 1
    }

    override fun seekBack() {

    }

    override fun getSeekForwardIncrement(): Long {
        return 1
    }

    override fun seekForward() {

    }

    override fun hasPreviousMediaItem(): Boolean {
        return false
    }

    override fun seekToPreviousMediaItem() {

    }

    override fun getMaxSeekToPreviousPosition(): Long {
        return 0
    }

    override fun seekToPrevious() {

    }

    override fun hasNextMediaItem(): Boolean {
        return false
    }

    override fun seekToNextMediaItem() {

    }

    override fun seekToNext() {

    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {

    }

    override fun setPlaybackSpeed(speed: Float) {

    }

    override fun getPlaybackParameters(): PlaybackParameters {
        return PlaybackParameters.DEFAULT
    }

    override fun stop() {
        tts.stop()
        currentChunkIndex = 0
    }

    override fun release() {
        tts.shutdown()
    }

    override fun getCurrentTracks(): Tracks {
        return Tracks.EMPTY
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return TrackSelectionParameters.DEFAULT
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {

    }

    override fun getMediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder().setTitle("TTSaaafdfa").setArtist("habr").build()
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        return MediaMetadata.Builder().setTitle("TTSaaaa").setArtist("habr").build()
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {

    }

    override fun getCurrentManifest(): Any? {
        return null
    }

    override fun getCurrentTimeline(): Timeline {
        return Timeline.EMPTY
    }

    override fun getCurrentPeriodIndex(): Int {
        return 0
    }
    @Deprecated("")
    override fun getCurrentWindowIndex(): Int {
        return 0
    }
    @Deprecated("")
    override fun getCurrentMediaItemIndex(): Int {
        return 0
    }
    @Deprecated("")
    override fun getNextWindowIndex(): Int {
        return 0
    }

    override fun getNextMediaItemIndex(): Int {
        return 0

    }
    @Deprecated("")
    override fun getPreviousWindowIndex(): Int {
        return 0

    }

    override fun getPreviousMediaItemIndex(): Int {
        return 0

    }

    override fun getCurrentMediaItem(): MediaItem? {
        return mediaItems.lastOrNull()

    }

    override fun getMediaItemCount(): Int {
        return mediaItems.size
    }

    override fun getMediaItemAt(index: Int): MediaItem {
        return mediaItems[index]
    }

    override fun getDuration(): Long {
        return 1000

    }

    override fun getCurrentPosition(): Long {
        return 0
    }

    override fun getBufferedPosition(): Long {
        return 0

    }

    override fun getBufferedPercentage(): Int {
        return 100

    }

    override fun getTotalBufferedDuration(): Long {
        return 100
    }
    @Deprecated("")
    override fun isCurrentWindowDynamic(): Boolean {
        return false
    }

    override fun isCurrentMediaItemDynamic(): Boolean {
        return false
    }
    @Deprecated("")
    override fun isCurrentWindowLive(): Boolean {
        return false
    }

    override fun isCurrentMediaItemLive(): Boolean {
        return false
    }

    override fun getCurrentLiveOffset(): Long {
        return 100
    }
    @Deprecated("")
    override fun isCurrentWindowSeekable(): Boolean {
        return false
    }

    override fun isCurrentMediaItemSeekable(): Boolean {
        return false
    }

    override fun isPlayingAd(): Boolean {
        return false
    }

    override fun getCurrentAdGroupIndex(): Int {
        return 100
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        return 100
    }

    override fun getContentDuration(): Long {
        return 100
    }

    override fun getContentPosition(): Long {
        return 100
    }

    override fun getContentBufferedPosition(): Long {
        return 100
    }

    override fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.DEFAULT
    }

    override fun setVolume(volume: Float) {
    }

    override fun getVolume(): Float {
        return 0.5f
    }

    override fun mute() {

    }

    override fun unmute() {

    }

    override fun clearVideoSurface() {

    }

    override fun clearVideoSurface(surface: Surface?) {

    }

    override fun setVideoSurface(surface: Surface?) {

    }

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {

    }

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {

    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {

    }

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {

    }

    override fun setVideoTextureView(textureView: TextureView?) {

    }

    override fun clearVideoTextureView(textureView: TextureView?) {

    }

    override fun getVideoSize(): VideoSize {
        return VideoSize.UNKNOWN
    }

    override fun getSurfaceSize(): Size {
        return Size.ZERO
    }

    override fun getCurrentCues(): CueGroup {
        return CueGroup.EMPTY_TIME_ZERO
    }

    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_LOCAL).build()
    }

    override fun getDeviceVolume(): Int {
        return 50
    }

    override fun isDeviceMuted(): Boolean {
        return false
    }
    @Deprecated("")
    override fun setDeviceVolume(volume: Int) {

    }

    override fun setDeviceVolume(volume: Int, flags: Int) {

    }
    @Deprecated("")
    override fun increaseDeviceVolume() {

    }

    override fun increaseDeviceVolume(flags: Int) {

    }
    @Deprecated("")
    override fun decreaseDeviceVolume() {

    }

    override fun decreaseDeviceVolume(flags: Int) {

    }
    @Deprecated("")
    override fun setDeviceMuted(muted: Boolean) {

    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {

    }

    override fun setAudioAttributes(
        audioAttributes: AudioAttributes,
        handleAudioFocus: Boolean
    ) {

    }

}


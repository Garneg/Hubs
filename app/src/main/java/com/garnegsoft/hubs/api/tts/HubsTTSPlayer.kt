package com.garnegsoft.hubs.api.tts

import android.media.AudioManager
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.FlagSet
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_GET_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_GET_METADATA
import androidx.media3.common.Player.COMMAND_PLAY_PAUSE
import androidx.media3.common.Player.COMMAND_SET_SPEED_AND_PITCH
import androidx.media3.common.Player.COMMAND_STOP
import androidx.media3.common.Player.PLAYBACK_SUPPRESSION_REASON_NONE
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM
import androidx.media3.common.Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.forEach


@OptIn(UnstableApi::class)
class TTSPlayer(
    val tts: TextToSpeech,
    val audioManager: AudioManager,
) : Player {

    private var chunks: List<String> = emptyList()
    private var currentChunkIndex: Int = 0

    val listeners: MutableList<Player.Listener> = mutableListOf()
    var isPlayerLoading: Boolean = false
    private val mediaItems: MutableList<MediaItem> = mutableListOf()

    private var articleMetadata: ArticleMetadata? = null

    val availableCommandsList = listOf(
        Player.COMMAND_PLAY_PAUSE,
        Player.COMMAND_STOP,
        Player.COMMAND_GET_METADATA,
        Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
        Player.COMMAND_SET_SPEED_AND_PITCH,
        Player.COMMAND_GET_TIMELINE,
        Player.COMMAND_SEEK_FORWARD,
        Player.COMMAND_SEEK_BACK,
        Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
        Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
        )

    private var currentPlayerState = Player.STATE_IDLE

    private var mediaMetadata = MediaMetadata.Builder().setTitle("").setAuthor("").build()

    private val audioFocusRequest = AudioFocusRequestCompat.Builder(AUDIOFOCUS_GAIN)
        .setOnAudioFocusChangeListener {
            Log.i("TTS_SERIVCE", "Audio focus changed: $it")
            if (it != AUDIOFOCUS_GAIN) {
                pause()
            }
        }
        .build()

    private var ttsInProgress = false

    /**
     * Loading chunks that tts will play. Each chunk's length must be less than maximum chars number for TextToSpeech engine
     */
    fun loadChunks(chunks: List<String>) {
        tts.stop()
        currentChunkIndex = 0
        this.chunks = chunks
    }

    data class ArticleMetadata(
        val title: String,
        val author: String,
        val thumbnailUri: String?,
        val articleId: Int,
        val offline: Boolean,
    )


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
        this.mediaItems.clear()
        this.mediaItems.addAll(mediaItems)
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
        mediaItems.clear()
        mediaItems.add(mediaItem)
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
        mediaItems.removeAt(index)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {

    }

    override fun clearMediaItems() {
        mediaItems.clear()
    }

    override fun isCommandAvailable(command: Int): Boolean {
        Log.i("TTS_SERVICE", "command available check $command command")

        if (availableCommandsList.contains(command)) {
            return true
        } else {
            Log.i("TTS_SERVICE", "Unsupported command availability checked $command")
            return false
        }
    }

    override fun canAdvertiseSession(): Boolean {
        Log.i("TTS_SERVICE", "can advertise session command")

        return true
    }

    @OptIn(UnstableApi::class)
    override fun getAvailableCommands(): Player.Commands {
        return Player.Commands.Builder()
            .apply {
                availableCommandsList.forEach { command ->
                    add(command)
                }
            }
//            .add(COMMAND_SET_SPEED_AND_PITCH)
            .build()
    }

    override fun prepare() {
        Log.i("TTS_SERVICE", "prepare command")

    }

    override fun getPlaybackState(): Int {
        Log.i("TTS_SERVICE", "Get Playback state command -> $currentPlayerState")

        return currentPlayerState
    }

    override fun getPlaybackSuppressionReason(): Int {
        return PLAYBACK_SUPPRESSION_REASON_NONE
    }

    override fun isPlaying(): Boolean {
        Log.i("TTS_SERVICE", "isPlaying command -> ${ttsInProgress}")

        return ttsInProgress
    }

    override fun getPlayerError(): PlaybackException? {
        Log.i("TTS_SERVICE", "get player error command")

        return null
    }

    override fun play() {
        Log.i("TTS_SERVICE", "Play command")
        if (chunks.isNotEmpty()) {
            currentPlayerState = Player.STATE_READY

            if (currentChunkIndex >= chunks.lastIndex)
                currentChunkIndex = 0


            tts.speak(chunks[currentChunkIndex], TextToSpeech.QUEUE_ADD, null, currentChunkIndex.toString())
            listeners.forEach {
                it.onIsPlayingChanged(true)
                it.onPlaybackStateChanged(Player.STATE_READY)
                it.onPlayWhenReadyChanged(true, PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            }
            val discontinuityEvent = Player.Events(FlagSet.Builder().add(Player.EVENT_POSITION_DISCONTINUITY).build())
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
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            ttsInProgress = false
                            listeners.forEach {
                                it.onIsPlayingChanged(false)
                                it.onPlayWhenReadyChanged(false, PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM)

                            }
                        }

                    }
                }

                override fun onError(utteranceId: String?) {
                    Log.e("ttss", "onError:$utteranceId")
                }

                override fun onStart(utteranceId: String?) {
                    Log.i("ttss", "playing chunk $currentChunkIndex")
                    ttsInProgress = true
                    val response = AudioManagerCompat.requestAudioFocus(
                        audioManager,
                        audioFocusRequest
                    )
                    Log.i(
                        "TTS_SERVICE", "audio focus request ended up with: $response"
                    )

                    listeners.forEach {
                        it.onEvents(this@TTSPlayer, discontinuityEvent)
                    }


                }
            })
        }
    }

    override fun pause() {
        Log.i("TTS_SERVICE", "pause command")
        if (tts.isSpeaking) {
            ttsInProgress = false
            tts.stop()
            listeners.forEach {
                it.onIsPlayingChanged(false)
                it.onPlayWhenReadyChanged(false, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
            }
        }

    }


    override fun setPlayWhenReady(playWhenReady: Boolean) {
        Log.i("TTS_SERVICE", "set play when ready command")
        if (playWhenReady) {
            play()
        } else {
            pause()
        }
    }

    override fun getPlayWhenReady(): Boolean {
        Log.i("TTS_SERVICE", "get play when ready command -> ${ttsInProgress}")

        return ttsInProgress
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

    fun prepareToLoading() {
        stop()
        isPlayerLoading = true
        listeners.forEach {
            it.onIsLoadingChanged(true)
        }
        currentPlayerState = Player.STATE_BUFFERING
    }

    fun endLoading() {
        isPlayerLoading = false
        listeners.forEach {
            it.onIsLoadingChanged(false)
        }
        currentPlayerState = Player.STATE_READY
    }

    override fun isLoading(): Boolean {
        Log.i("TTS_SERVICE", "is loading command")

        return isPlayerLoading
    }

    override fun seekToDefaultPosition() {
        Log.i("TTS_SERVICE", "seek to default position command")

        stop()
        play()
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        stop()
        play()
    }

    override fun seekTo(positionMs: Long) {
        Log.i("TTS_SERVICE", "seek to $positionMs command")
        tts.stop()
        currentChunkIndex = positionMs.toInt().coerceIn(0, chunks.lastIndex)
        listeners.forEach { it.onPositionDiscontinuity(Player.DISCONTINUITY_REASON_SEEK) }
        play()
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {

    }

    override fun getSeekBackIncrement(): Long {
        return 1
    }

    override fun seekBack() {
        pause()
        currentChunkIndex = (currentChunkIndex - 1).coerceAtLeast(0)
        play()
    }

    override fun getSeekForwardIncrement(): Long {
        return 1
    }

    override fun seekForward() {
        pause()
        currentChunkIndex = (currentChunkIndex + 1).coerceAtMost(chunks.lastIndex)
        play()
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
        throw UnsupportedOperationException()
    }

    override fun hasNextMediaItem(): Boolean {
        return false
    }

    override fun seekToNextMediaItem() {
        throw UnsupportedOperationException()
    }

    override fun seekToNext() {
        throw UnsupportedOperationException()

    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {

    }

    override fun setPlaybackSpeed(speed: Float) {
        val result = tts.setSpeechRate(speed)
        Log.i("TTS_SERVICE", "setSpeechRate to $speed; result: $result")
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        Log.i("TTS_SERVICE", "get playback params command")

        return PlaybackParameters.DEFAULT.withSpeed(0.00001f)
    }

    override fun stop() {
        Log.i("TTS_SERVICE", "stop command")
        AudioManagerCompat.abandonAudioFocusRequest(
            audioManager,
            audioFocusRequest
        )
        tts.stop()
        currentPlayerState = Player.STATE_IDLE
        listeners.forEach {
            it.onPlaybackStateChanged(Player.STATE_IDLE)
            it.onIsPlayingChanged(false)
            it.onPlayWhenReadyChanged(false, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)
        }
        currentChunkIndex = 0
    }

    override fun release() {
        Log.i("TTS_SERVICE", "release command")
        stop()
        tts.shutdown()
    }

    override fun getCurrentTracks(): Tracks {
        Log.i("TTS_SERVICE", "getCurrentTracks command")
        return Tracks.EMPTY
    }

    @OptIn(UnstableApi::class)
    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return TrackSelectionParameters.DEFAULT
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {

    }

    fun setMediaMetadata(articleMetadata: ArticleMetadata) {
        this.articleMetadata = articleMetadata

        mediaMetadata = MediaMetadata.Builder()
            .setTitle(articleMetadata.title)
            .setAuthor('@' + articleMetadata.author)
            .setArtist('@' + articleMetadata.author)
            .setArtworkUri(articleMetadata.thumbnailUri?.toUri())
            .setExtras(
                Bundle().apply {
                    putInt("articleId", articleMetadata.articleId)
                    putBoolean("offline", articleMetadata.offline)
                })
            .build()

        listeners.forEach {
            it.onMediaMetadataChanged(mediaMetadata)
        }
    }

    override fun getMediaMetadata(): MediaMetadata {
        Log.i("TTS_SERVICE", "getMediaMetadata command")
        return mediaMetadata
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        return MediaMetadata.Builder().setTitle("Статья").setArtist("Habr").build()
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {

    }

    override fun getCurrentManifest(): Any? {
        return null
    }

    override fun getCurrentTimeline(): Timeline {
        return TTSTimeline(mediaItems.first(), chunks.size.toLong())
    }

    /**
     * Pretty much the same as SinglePeriodTimeline class from ExoPlayer artifact.
     * It is here just to not import exoplayer
     */
    internal class TTSTimeline(
        val mediaItem: MediaItem,
        val length: Long
    ) : Timeline() {
        override fun getWindowCount(): Int = 1

        override fun getWindow(
            windowIndex: Int,
            window: Window,
            defaultPositionProjectionUs: Long
        ): Window {
            return window.set(
                Window.SINGLE_WINDOW_UID,
                mediaItem,
                null,
                0L,
                0L,
                0L,
                false,
                false,
                null,
                0L,
                length,
                0,
                0,
                0L
            )
        }

        override fun getPeriodCount(): Int = 1

        override fun getPeriod(
            periodIndex: Int,
            period: Period,
            setIds: Boolean
        ): Period {
            return period.set(
                null,
                0,
                0,
                length,
                0L
            )
        }

        override fun getIndexOfPeriod(uid: Any): Int = 0
        override fun getUidOfPeriod(periodIndex: Int): Any = 0

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

    /**
     * Get amount of text chunks that player will speak
     * @since TTSPlayer
     */
    override fun getDuration(): Long {
        return chunks.size.toLong()
    }

    /**
     * Get index of currently playing chunk
     * @since TTSPlayer
     */
    override fun getCurrentPosition(): Long {
        return currentChunkIndex.toLong()
    }

    override fun getBufferedPosition(): Long {
        return 0

    }

    override fun getBufferedPercentage(): Int {
        return 100

    }

    override fun getTotalBufferedDuration(): Long {
        return 0
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
        return 0
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
        return C.INDEX_UNSET
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        return C.INDEX_UNSET
    }

    override fun getContentDuration(): Long {
        return duration
    }

    override fun getContentPosition(): Long {
        return currentPosition
    }

    override fun getContentBufferedPosition(): Long {
        return bufferedPosition
    }

    override fun getAudioAttributes(): AudioAttributes {

        return AudioAttributes.DEFAULT
    }

    override fun setVolume(volume: Float) {
        Log.i("TTS_SERVICE", "set $volume volume command")

    }

    override fun getVolume(): Float {
        Log.i("TTS_SERVICE", "get volume command")

        return 0.5f
    }

    override fun mute() {
        Log.i("TTS_SERVICE", "mute command")
    }

    override fun unmute() {
        Log.i("TTS_SERVICE", "unmute command")

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
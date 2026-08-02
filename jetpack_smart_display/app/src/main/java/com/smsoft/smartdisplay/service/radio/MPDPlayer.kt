package com.smsoft.smartdisplay.service.radio

import android.media.AudioDeviceInfo
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Effect
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK
import androidx.media3.common.PriorityTaskManager
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.CodecParameters
import androidx.media3.exoplayer.CodecParametersChangeListener
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.ScrubbingModeParameters
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.analytics.AnalyticsCollector
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.image.ImageOutput
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.exoplayer.video.spherical.CameraMotionListener
import com.smsoft.smartdisplay.utils.mpd.MPDHelper
import com.smsoft.smartdisplay.utils.mpd.data.MPDCredentials
import com.smsoft.smartdisplay.utils.mpd.data.MPDState
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus
import com.smsoft.smartdisplay.utils.mpd.event.StatusChangeListener
import de.dixieflatline.mpcw.client.CommunicationException
import de.dixieflatline.mpcw.client.ProtocolException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "MPDPlayer"

@UnstableApi
class MPDPlayer(
    private val helper: MPDHelper,
    private val credentials: MPDCredentials
): ExoPlayer {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val listeners = CopyOnWriteArrayList<Player.Listener>()
    private var playlist: List<MediaItem>? = null
    private var status: MPDStatus? = null
    private var preset = 0
    private var isPrepared = false
    @Volatile
    private var released = false

    private val availableCommands = Player.Commands.Builder()
        .add(Player.COMMAND_PLAY_PAUSE)
        .add(Player.COMMAND_STOP)
        .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
        .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
        .add(Player.COMMAND_GET_METADATA)
        .add(Player.COMMAND_GET_VOLUME)
        .add(Player.COMMAND_SET_VOLUME)
        .add(Player.COMMAND_GET_TIMELINE)
        .build()

    private val statusChangedListener = object: StatusChangeListener {
        override fun connectionStateChanged(isConnected: Boolean) {
        }

        override fun playlistChanged(newStatus: MPDStatus, playlistVersion: Int) {
            status = newStatus
            updatePlaylist()
            if ((playlist != null) && playlist!!.isEmpty()) {
                try {
                    helper.pause()
                } catch (_: CommunicationException) {
                    reconnect {
                        helper.pause()
                    }
                }
            }
        }

        override fun trackChanged(newStatus: MPDStatus, track: Int) {
            notifyListeners { it.onPlaybackStateChanged(ExoPlayer.STATE_BUFFERING) }

            status = newStatus
            preset = track

            updatePlaylist()

            coroutineScope.launch(Dispatchers.Main) {
                delay(500.milliseconds)
                notifyListeners { it.onPlaybackStateChanged(ExoPlayer.STATE_READY) }
                notifyListeners { it.onIsPlayingChanged(isPlaying) }
            }
        }

        override fun trackPositionChanged(newStatus: MPDStatus) {
            status = newStatus
            notifyListeners { it.onIsPlayingChanged(isPlaying) }
        }

        override fun volumeChanged(newStatus: MPDStatus, volume: Int) {
            notifyListeners { it.onVolumeChanged(volume / 100F) }
        }
    }

    init {
        helper.statusChangedListener = statusChangedListener
        coroutineScope.launch {
            try {
                helper.connect(credentials)
            } catch (e: Exception) {
                Log.e(TAG, "Initial MPD connect failed", e)
            }
        }
    }

    override fun addListener(listener: Player.Listener) {
        listeners.addIfAbsent(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
    }

    private inline fun notifyListeners(event: (Player.Listener) -> Unit) {
        listeners.forEach(event)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
    }

    override fun seekTo(positionMs: Long) {
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        preset = mediaItemIndex

        val mediaItem = currentMediaItem
        if (mediaItem != MediaItem.EMPTY) {
            coroutineScope.launch {
                try {
                    helper.playId(mediaItem.mediaId)
                } catch (_: CommunicationException) {
                    reconnect {
                        helper.playId(mediaItem.mediaId)
                    }
                }
            }
        }

        notifyListeners {
            it.onMediaItemTransition(mediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)
        }

        notifyListeners {
            it.onPositionDiscontinuity(
                Player.PositionInfo(
                    null,
                    0,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0
                ),
                Player.PositionInfo(
                    null,
                    0,
                    null,
                    null,
                    0,
                    0,
                    0,
                    0,
                    0
                ),
                DISCONTINUITY_REASON_SEEK
            )
        }
    }

    override fun prepare() {
        isPrepared = true
        coroutineScope.launch {
            try {
                playlist = helper.getPlaylist()
                val currentStatus = helper.getStatus()
                status = currentStatus
                preset = currentStatus.songPos
                when (currentStatus.state) {
                    MPDState.PAUSED -> helper.play()
                    MPDState.STOPPED -> {
                        val mediaItem = currentMediaItem
                        if (mediaItem != MediaItem.EMPTY) {
                            helper.playId(mediaItem.mediaId)
                        }
                    }
                    else -> {}
                }
                helper.startMonitor()
                coroutineScope.launch(Dispatchers.Main) {
                    delay(500.milliseconds)
                    notifyListeners { it.onIsPlayingChanged(isPlaying) }
                    notifyListeners { it.onMediaMetadataChanged(mediaMetadata) }
                    notifyListeners { it.onPlaybackStateChanged(ExoPlayer.STATE_READY) }
                }
            } catch (_: CommunicationException) {
                reconnect {
                    prepare()
                }
            } catch (e: ProtocolException) {
                notifyListeners {
                    it.onPlayerError(
                        PlaybackException(
                            "MPD protocol error",
                            e,
                            PlaybackException.ERROR_CODE_REMOTE_ERROR
                        )
                    )
                }
            }
        }
    }

    private fun reconnect(callback: () -> Unit) {
        try {
            helper.reconnect()
            callback()
        } catch(_: Exception) {
            notifyListeners {
                it.onPlayerError(
                    PlaybackException(
                        "Connection Failed",
                        null,
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                    )
                )
            }
        }
    }

    private fun updatePlaylist() {
        playlist = helper.updatePlaylist()
        notifyListeners { it.onMediaMetadataChanged(mediaMetadata) }
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
    }

    override fun getMediaItemCount(): Int {
        return if (playlist != null) playlist!!.size else 1
    }

    override fun isPlaying(): Boolean {
        return status?.state == MPDState.PLAYING
    }

    override fun play() {
        coroutineScope.launch {
            try {
                helper.play()
            } catch (_: CommunicationException) {
                reconnect {
                    helper.play()
                }
            }
            status?.state = MPDState.PLAYING
            withContext(Dispatchers.Main) {
                notifyListeners { it.onIsPlayingChanged(true) }
            }
        }
    }

    override fun pause() {
        coroutineScope.launch {
            try {
                helper.pause()
            } catch (_: CommunicationException) {
                reconnect {
                    helper.pause()
                }
            }
            status?.state = MPDState.PAUSED
            withContext(Dispatchers.Main) {
                notifyListeners { it.onIsPlayingChanged(false) }
            }
        }
    }

    override fun seekToPreviousMediaItem() {
        coroutineScope.launch {
            if (!isPrepared) {
                prepare()
                delay(500.milliseconds)
            }
            try {
                helper.previous()
            } catch (_: CommunicationException) {
                reconnect {
                    helper.previous()
                }
            }
        }
    }

    override fun seekToNextMediaItem() {
        coroutineScope.launch {
            if (!isPrepared) {
                prepare()
                delay(500.milliseconds)
            }
            try {
                helper.next()
            } catch (_: CommunicationException) {
                reconnect {
                    helper.next()
                }
            }
        }
    }

    override fun getMediaMetadata(): MediaMetadata {
        return currentMediaItem.mediaMetadata
    }

    override fun getApplicationLooper(): Looper {
        return Looper.getMainLooper()
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
    }

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
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
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
    }

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {
    }

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {
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
        mediaItems: MutableList<MediaItem>
    ) {
    }

    override fun removeMediaItem(index: Int) {
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
    }

    override fun clearMediaItems() {
    }

    override fun isCommandAvailable(command: Int): Boolean {
        return availableCommands.contains(command)
    }

    override fun canAdvertiseSession(): Boolean {
        return false
    }

    override fun getAvailableCommands(): Player.Commands {
        return availableCommands
    }

    @Deprecated("Deprecated in Java")
    override fun prepare(mediaSource: MediaSource) {
    }

    @Deprecated("Deprecated in Java")
    override fun prepare(mediaSource: MediaSource, resetPosition: Boolean, resetState: Boolean) {
    }

    override fun getPlaybackState(): Int {
        return Player.STATE_IDLE
    }

    override fun getPlaybackSuppressionReason(): Int {
        return Player.PLAYBACK_SUPPRESSION_REASON_NONE
    }
    override fun getPlayerError(): ExoPlaybackException? {
        return null
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
    }

    override fun getShuffleModeEnabled(): Boolean {
        return false
    }

    override fun isLoading(): Boolean {
        return false
    }

    override fun seekToDefaultPosition() {
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
    }
    override fun getSeekBackIncrement(): Long {
        return 0
    }

    override fun seekBack() {
    }

    override fun getSeekForwardIncrement(): Long {
        return 0
    }

    override fun seekForward() {
    }

    override fun hasPreviousMediaItem(): Boolean {
        return getPreviousMediaItemIndex() != C.INDEX_UNSET
    }

    override fun getMaxSeekToPreviousPosition(): Long {
        return 0
    }

    override fun seekToPrevious() {
    }

    override fun hasNextMediaItem(): Boolean {
        return getNextMediaItemIndex() != C.INDEX_UNSET
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
        coroutineScope.launch {
            try {
                helper.stop()
            } catch (_: CommunicationException) {
                reconnect {
                    helper.stop()
                }
            }
            helper.stopMonitor()
            helper.disconnect()
        }
    }

    override fun release() {
        try {
            helper.stopMonitor()
            helper.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting MPD helper on release", e)
        }
        listeners.clear()
        coroutineScope.cancel()
        released = true
    }

    override fun getCurrentTracks(): Tracks {
        return Tracks.EMPTY
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return TrackSelectionParameters.DEFAULT
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        return MediaMetadata.EMPTY
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

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getCurrentWindowIndex(): Int {
        return 0
    }

    override fun getCurrentMediaItemIndex(): Int {
        return preset
    }

    override fun getCurrentMediaItem(): MediaItem {
        val currentPlaylist = playlist
        if (currentPlaylist.isNullOrEmpty()) {
            return MediaItem.EMPTY
        }
        if (preset > currentPlaylist.size - 1) {
            preset = currentPlaylist.size - 1
        }
        if (preset < 0) {
            preset = 0
        }
        return currentPlaylist.getOrNull(preset) ?: MediaItem.EMPTY
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getNextWindowIndex(): Int {
        return 0
    }

    override fun getNextMediaItemIndex(): Int {
        val size = playlist?.size ?: 0
        return if ((status == null) || (size == 0) || (preset >= size - 1))
            C.INDEX_UNSET else preset + 1
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getPreviousWindowIndex(): Int {
        return 0
    }

    override fun getPreviousMediaItemIndex(): Int {
        val size = playlist?.size ?: 0
        return if ((status == null) || (size == 0) || (preset <= 0))
            C.INDEX_UNSET else preset - 1
    }

    override fun getMediaItemAt(index: Int): MediaItem {
        return playlist?.getOrNull(index) ?: MediaItem.EMPTY
    }

    override fun getDuration(): Long {
        return status?.totalTime ?: 0
    }

    override fun getCurrentPosition(): Long {
        return (status?.elapsedTime ?: 0) * 1000
    }

    override fun setVolume(audioVolume: Float) {
        coroutineScope.launch {
            try {
                helper.setVolume((audioVolume * 100).toInt())
            } catch (_: CommunicationException) {
                reconnect {
                    helper.setVolume((audioVolume * 100).toInt())
                }
            }
        }
    }

    override fun getVolume(): Float {
        return status?.let { it.volume.toFloat() / 100 } ?: -1F
    }

    override fun mute() {
        TODO("Not yet implemented")
    }

    override fun unmute() {
        TODO("Not yet implemented")
    }

    override fun getBufferedPosition(): Long {
        return 0
    }

    override fun getBufferedPercentage(): Int {
        return 0
    }

    override fun getTotalBufferedDuration(): Long {
        return 0
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun isCurrentWindowDynamic(): Boolean {
        return false
    }

    override fun isCurrentMediaItemDynamic(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun isCurrentWindowLive(): Boolean {
        return false
    }

    override fun isCurrentMediaItemLive(): Boolean {
        return false
    }

    override fun getCurrentLiveOffset(): Long {
        return 0
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
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
        return 0
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        return 0
    }

    override fun getContentDuration(): Long {
        return 0
    }

    override fun getContentPosition(): Long {
        return 0
    }

    override fun getContentBufferedPosition(): Long {
        return 0
    }

    override fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.DEFAULT
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
        return Size.UNKNOWN
    }

    override fun getCurrentCues(): CueGroup {
        return CueGroup.EMPTY_TIME_ZERO
    }

    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_LOCAL).build()
    }

    override fun getDeviceVolume(): Int {
        return 0
    }

    override fun isDeviceMuted(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun setDeviceVolume(volume: Int) {
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
    }

    @Deprecated("Deprecated in Java")
    override fun increaseDeviceVolume() {
    }

    override fun increaseDeviceVolume(flags: Int) {
    }

    @Deprecated("Deprecated in Java")
    override fun decreaseDeviceVolume() {
    }

    override fun decreaseDeviceVolume(flags: Int) {
    }

    @Deprecated("Deprecated in Java")
    override fun setDeviceMuted(muted: Boolean) {
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
    }

    override fun addAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {
    }

    override fun removeAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {
    }

    override fun getAnalyticsCollector(): AnalyticsCollector {
        TODO("Not yet implemented")
    }

    override fun addAnalyticsListener(listener: AnalyticsListener) {
    }

    override fun removeAnalyticsListener(listener: AnalyticsListener) {
    }

    override fun getRendererCount(): Int {
        return 0
    }

    override fun getRendererType(index: Int): Int {
        return 0
    }

    override fun getRenderer(index: Int): Renderer {
        TODO("Not yet implemented")
    }

    override fun getSecondaryRenderer(index: Int): Renderer? {
        return null
    }

    override fun getTrackSelector(): TrackSelector? {
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun getCurrentTrackGroups(): TrackGroupArray {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getCurrentTrackSelections(): TrackSelectionArray {
        TODO("Not yet implemented")
    }

    override fun getPlaybackLooper(): Looper {
        return Looper.getMainLooper()
    }

    override fun getClock(): Clock {
        return Clock.DEFAULT
    }

    override fun setMediaSources(mediaSources: MutableList<MediaSource>) {
    }

    override fun setMediaSources(mediaSources: MutableList<MediaSource>, resetPosition: Boolean) {
    }

    override fun setMediaSources(
        mediaSources: MutableList<MediaSource>,
        startMediaItemIndex: Int,
        startPositionMs: Long
    ) {
    }

    override fun setMediaSource(mediaSource: MediaSource) {
    }

    override fun setMediaSource(mediaSource: MediaSource, startPositionMs: Long) {
    }

    override fun setMediaSource(mediaSource: MediaSource, resetPosition: Boolean) {
    }

    override fun addMediaSource(mediaSource: MediaSource) {
    }

    override fun addMediaSource(index: Int, mediaSource: MediaSource) {
    }

    override fun addMediaSources(mediaSources: MutableList<MediaSource>) {
    }

    override fun addMediaSources(index: Int, mediaSources: MutableList<MediaSource>) {
    }

    override fun setShuffleOrder(shuffleOrder: ShuffleOrder) {
    }

    override fun getShuffleOrder(): ShuffleOrder {
        TODO("Provide the return value")
    }

    override fun setPreloadConfiguration(preloadConfiguration: ExoPlayer.PreloadConfiguration) {
    }

    override fun getPreloadConfiguration(): ExoPlayer.PreloadConfiguration {
        TODO("Provide the return value")
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {
    }

    override fun setAudioSessionId(audioSessionId: Int) {
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun setAuxEffectInfo(auxEffectInfo: AuxEffectInfo) {
    }

    override fun clearAuxEffectInfo() {
    }

    override fun setPreferredAudioDevice(audioDeviceInfo: AudioDeviceInfo?) {
    }

    override fun setVirtualDeviceId(virtualDeviceId: Int) {
        TODO("Not yet implemented")
    }

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
    }

    override fun getSkipSilenceEnabled(): Boolean {
        return false
    }

    override fun setScrubbingModeEnabled(scrubbingModeEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isScrubbingModeEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setScrubbingModeParameters(scrubbingModeParameters: ScrubbingModeParameters) {
        TODO("Not yet implemented")
    }

    override fun getScrubbingModeParameters(): ScrubbingModeParameters {
        TODO("Not yet implemented")
    }

    override fun setVideoEffects(videoEffects: MutableList<Effect>) {
    }

    override fun setVideoScalingMode(videoScalingMode: Int) {
    }

    override fun getVideoScalingMode(): Int {
        return 0
    }

    override fun setVideoChangeFrameRateStrategy(videoChangeFrameRateStrategy: Int) {
    }

    override fun getVideoChangeFrameRateStrategy(): Int {
        return 0
    }

    override fun setVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {
    }

    override fun clearVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {
    }

    override fun setCameraMotionListener(listener: CameraMotionListener) {
    }

    override fun clearCameraMotionListener(listener: CameraMotionListener) {
    }

    override fun createMessage(target: PlayerMessage.Target): PlayerMessage {
        TODO("Not yet implemented")
    }

    override fun setSeekParameters(seekParameters: SeekParameters?) {
    }

    override fun getSeekParameters(): SeekParameters {
        return SeekParameters.DEFAULT
    }

    override fun setSeekBackIncrementMs(seekBackIncrementMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setSeekForwardIncrementMs(seekForwardIncrementMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setMaxSeekToPreviousPositionMs(maxSeekToPreviousPositionMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setForegroundMode(foregroundMode: Boolean) {
    }

    override fun setPauseAtEndOfMediaItems(pauseAtEndOfMediaItems: Boolean) {
    }

    override fun getPauseAtEndOfMediaItems(): Boolean {
        return false
    }

    override fun getAudioFormat(): Format? {
        return null
    }

    override fun getVideoFormat(): Format? {
        return null
    }

    override fun getAudioDecoderCounters(): DecoderCounters? {
        return null
    }

    override fun getVideoDecoderCounters(): DecoderCounters? {
        return null
    }

    override fun setHandleAudioBecomingNoisy(handleAudioBecomingNoisy: Boolean) {
    }

    override fun setWakeMode(wakeMode: Int) {
    }

    override fun setPriority(priority: Int) {
        TODO("Not yet implemented")
    }

    override fun setPriorityTaskManager(priorityTaskManager: PriorityTaskManager?) {
    }

    override fun isSleepingForOffload(): Boolean {
        return false
    }

    override fun isTunnelingEnabled(): Boolean {
        return false
    }

    override fun isReleased(): Boolean {
        return released
    }

    override fun setImageOutput(imageOutput: ImageOutput?) {
    }

    override fun setAudioCodecParameters(codecParameters: CodecParameters) {
        TODO("Not yet implemented")
    }

    override fun addAudioCodecParametersChangeListener(
        listener: CodecParametersChangeListener,
        keys: List<String>
    ) {
        TODO("Not yet implemented")
    }

    override fun removeAudioCodecParametersChangeListener(listener: CodecParametersChangeListener) {
        TODO("Not yet implemented")
    }

    override fun setVideoCodecParameters(codecParameters: CodecParameters) {
        TODO("Not yet implemented")
    }

    override fun addVideoCodecParametersChangeListener(
        listener: CodecParametersChangeListener,
        keys: List<String>
    ) {
        TODO("Not yet implemented")
    }

    override fun removeVideoCodecParametersChangeListener(listener: CodecParametersChangeListener) {
        TODO("Not yet implemented")
    }
}

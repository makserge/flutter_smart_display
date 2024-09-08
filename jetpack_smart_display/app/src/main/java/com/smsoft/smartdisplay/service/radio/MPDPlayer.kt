package com.smsoft.smartdisplay.service.radio

import android.media.AudioDeviceInfo
import android.net.Uri
import android.os.Looper
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
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.ExoPlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.PlayerMessage
import androidx.media3.exoplayer.Renderer
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
class MPDPlayer(
    private val helper: MPDHelper,
    private val credentials: MPDCredentials
): ExoPlayer {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var listener: Player.Listener? = null
    private var playlist: List<MediaItem>? = null
    private var status: MPDStatus? = null
    private var preset = 1
    private var isPrepared = false

    init {
        coroutineScope.launch {
            helper.connect(credentials)
        }
    }

    override fun addListener(listener: Player.Listener) {
        this.listener = listener
    }

    private val statusChangedListener = object: StatusChangeListener {
        override fun connectionStateChanged(isConnected: Boolean) {
        }

        override fun playlistChanged(newStatus: MPDStatus, playlistVersion: Int) {
            status = newStatus
            updatePlaylist()
            if ((playlist != null) && playlist!!.isEmpty()) {
                try {
                    helper.pause()
                } catch (e: CommunicationException) {
                    reconnect {
                        helper.pause()
                    }
                }
            }
        }

        override fun trackChanged(newStatus: MPDStatus, track: Int) {
            listener?.onPlaybackStateChanged(ExoPlayer.STATE_BUFFERING)

            status = newStatus
            preset = track

            updatePlaylist()

            coroutineScope.launch(Dispatchers.Main) {
                delay(500)
                listener?.onPlaybackStateChanged(ExoPlayer.STATE_READY)
                listener?.onIsPlayingChanged(isPlaying)
            }
        }

        override fun trackPositionChanged(newStatus: MPDStatus) {
            status = newStatus
            listener?.onIsPlayingChanged(isPlaying)
        }

        override fun volumeChanged(newStatus: MPDStatus, volume: Int) {
            listener?.onVolumeChanged(volume / 100F)
        }
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
    }

    override fun seekTo(positionMs: Long) {
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        preset = mediaItemIndex

        val mediaItem = MediaItem.Builder()
            .setMediaId("")
            .setUri(Uri.EMPTY)
            .build()
        listener?.onMediaItemTransition(mediaItem, Player.MEDIA_ITEM_TRANSITION_REASON_SEEK)

        listener?.onPositionDiscontinuity(
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
            DISCONTINUITY_REASON_SEEK)
    }

    override fun prepare() {
        isPrepared = true
        coroutineScope.launch {
            try {
                playlist = helper.getPlaylist()
                status = helper.getStatus()
                preset = status!!.songPos
                if (status != null) {
                    if (status!!.state == MPDState.PAUSED) {
                        helper.play()
                    } else if (status!!.state == MPDState.STOPPED) {
                        helper.playId(currentMediaItem.mediaId)
                    }
                }
                helper.startMonitor()
                coroutineScope.launch(Dispatchers.Main) {
                    delay(500)
                    listener?.onIsPlayingChanged(isPlaying)
                    listener?.onMediaMetadataChanged(mediaMetadata)
                    listener?.onPlaybackStateChanged(ExoPlayer.STATE_READY)
                }
            } catch (e: CommunicationException) {
                reconnect {
                    prepare()
                }
            }
        }
    }

    private fun reconnect(callback: () -> Unit) {
        try {
            helper.reconnect()
            callback()
        } catch(e: Exception) {
            listener?.onPlayerError(
                PlaybackException(
                    "Connection Failed",
                    null,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                )
            )
        }
    }

    private fun updatePlaylist() {
        playlist = helper.updatePlaylist()
        listener?.onMediaMetadataChanged(mediaMetadata)
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
            } catch (e: CommunicationException) {
                reconnect {
                    helper.play()
                }
            }
        }
    }

    override fun pause() {
        coroutineScope.launch {
            try {
                helper.pause()
            } catch (e: CommunicationException) {
                reconnect {
                    helper.pause()
                }
            }
        }
    }

    override fun seekToPreviousMediaItem() {
        coroutineScope.launch {
            if (!isPrepared) {
                prepare()
                delay(500)
            }
            try {
                helper.previous()
            } catch (e: CommunicationException) {
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
                delay(500)
            }
            try {
                helper.next()
            } catch (e: CommunicationException) {
                reconnect {
                    helper.next()
                }
            }
        }
    }

    override fun getMediaMetadata(): MediaMetadata {
        return currentMediaItem.mediaMetadata
    }

    override fun removeListener(listener: Player.Listener) {
    }

    override fun getApplicationLooper(): Looper {
        TODO("Not yet implemented")
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
        return false
    }

    override fun canAdvertiseSession(): Boolean {
        return false
    }

    override fun getAvailableCommands(): Player.Commands {
        TODO("Not yet implemented")
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

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun hasPrevious(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun hasPreviousWindow(): Boolean {
        return false
    }

    override fun hasPreviousMediaItem(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun previous() {
    }

    @Deprecated("Deprecated in Java")
    override fun seekToPreviousWindow() {
    }
    override fun getMaxSeekToPreviousPosition(): Long {
        return 0
    }

    override fun seekToPrevious() {
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun hasNext(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("false"))
    override fun hasNextWindow(): Boolean {
        return false
    }

    override fun hasNextMediaItem(): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun next() {
    }

    @Deprecated("Deprecated in Java")
    override fun seekToNextWindow() {
    }

    override fun seekToNext() {
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
    }

    override fun setPlaybackSpeed(speed: Float) {
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        TODO("Not yet implemented")
    }

    override fun stop() {
        coroutineScope.launch {
            try {
                helper.stop()
            } catch (e: CommunicationException) {
                reconnect {
                    helper.stop()
                }
            }
            helper.stopMonitor()
            helper.disconnect()
        }
    }

    override fun release() {
    }

    override fun getCurrentTracks(): Tracks {
        TODO("Not yet implemented")
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        TODO("Not yet implemented")
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        TODO("Not yet implemented")
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
    }

    override fun getCurrentManifest(): Any? {
        return null
    }

    override fun getCurrentTimeline(): Timeline {
        TODO("Not yet implemented")
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
        if (playlist == null) {
            return MediaItem.EMPTY
        }
        if (preset > playlist?.size!! - 1) {
            preset = playlist?.size!! - 1
        }
        return playlist?.get(preset) ?: MediaItem.EMPTY
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getNextWindowIndex(): Int {
        return 0
    }

    override fun getNextMediaItemIndex(): Int {
        return if ((status == null) || (playlist?.size == 0) || (preset == playlist?.size))
            C.INDEX_UNSET else preset + 1
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getPreviousWindowIndex(): Int {
        return 0
    }

    override fun getPreviousMediaItemIndex(): Int {
        return if ((status == null) || (playlist?.size == 0) || (preset == 1))
            C.INDEX_UNSET else preset - 1
    }

    override fun getMediaItemAt(index: Int): MediaItem {
        TODO("Not yet implemented")
    }

    override fun getDuration(): Long {
        return if (status != null) status?.totalTime!! else 0
    }

    override fun getCurrentPosition(): Long {
        return if (status != null) status?.elapsedTime!! * 1000 else 0
    }

    override fun setVolume(audioVolume: Float) {
        coroutineScope.launch {
            try {
                helper.setVolume((audioVolume * 100).toInt())
            } catch (e: CommunicationException) {
                reconnect {
                    helper.setVolume((audioVolume * 100).toInt())
                }
            }
        }
    }

    override fun getVolume(): Float {
        return if (status != null) {
            status?.volume!!.toFloat() / 100
        } else {
            -1F
        }
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun getSurfaceSize(): Size {
        TODO("Not yet implemented")
    }

    override fun getCurrentCues(): CueGroup {
        TODO("Not yet implemented")
    }

    override fun getDeviceInfo(): DeviceInfo {
        TODO("Not yet implemented")
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

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun getAudioComponent(): ExoPlayer.AudioComponent? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun getVideoComponent(): ExoPlayer.VideoComponent? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun getTextComponent(): ExoPlayer.TextComponent? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun getDeviceComponent(): ExoPlayer.DeviceComponent? {
        return null
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
        TODO("Not yet implemented")
    }

    override fun getClock(): Clock {
        TODO("Not yet implemented")
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

    override fun setPreloadConfiguration(preloadConfiguration: ExoPlayer.PreloadConfiguration) {
        TODO("Not yet implemented")
    }

    override fun getPreloadConfiguration(): ExoPlayer.PreloadConfiguration {
        TODO("Not yet implemented")
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

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
    }

    override fun getSkipSilenceEnabled(): Boolean {
        return false
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
        TODO("Not yet implemented")
    }

    override fun setImageOutput(imageOutput: ImageOutput?) {
    }

    init {
        helper.statusChangedListener = statusChangedListener
    }
}

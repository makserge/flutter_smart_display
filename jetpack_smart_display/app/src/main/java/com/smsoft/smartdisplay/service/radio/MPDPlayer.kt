package com.smsoft.smartdisplay.service.radio

import android.media.AudioDeviceInfo
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.*
import androidx.media3.common.Player.DISCONTINUITY_REASON_SEEK
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Clock
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.*
import androidx.media3.exoplayer.analytics.AnalyticsCollector
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ShuffleOrder
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.TrackSelectionArray
import androidx.media3.exoplayer.trackselection.TrackSelector
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.exoplayer.video.spherical.CameraMotionListener
import com.smsoft.smartdisplay.utils.getRadioSettings
import com.smsoft.smartdisplay.utils.mpd.MPDHelper
import com.smsoft.smartdisplay.utils.mpd.data.MPDState
import com.smsoft.smartdisplay.utils.mpd.data.MPDStatus
import com.smsoft.smartdisplay.utils.mpd.event.StatusChangeListener
import de.dixieflatline.mpcw.client.CommunicationException
import kotlinx.coroutines.*
import java.util.*

@UnstableApi
class MPDPlayer constructor(
    private val dataStore: DataStore<Preferences>,
    private val helper: MPDHelper
    ): ExoPlayer {
    private val TAG = "MPD"
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var listener: Player.Listener? = null
    private var playlist: List<MediaItem>? = null
    private var status: MPDStatus? = null
    private var preset = 1

    override fun addListener(listener: Player.Listener) {
        Log.d(TAG, "addListener")
        this.listener = listener
    }

    private val statusChangedListener = object: StatusChangeListener {
        override fun connectionStateChanged(isConnected: Boolean) {
            Log.d(TAG, "connectionStateChanged:isConnected:$isConnected")
        }

        override fun playlistChanged(newStatus: MPDStatus, playlistVersion: Int) {
            Log.d(TAG, "playlistChanged:playlistVersion:$playlistVersion")
            status = newStatus

            updatePlaylist()

            listener?.onVolumeChanged(volume)
        }

        override fun trackChanged(newStatus: MPDStatus, track: Int) {
            Log.d(TAG, "trackChanged:track:$track")

            listener?.onPlaybackStateChanged(ExoPlayer.STATE_BUFFERING)

            status = newStatus
            preset = track

            updatePlaylist()

            listener?.onVolumeChanged(volume)

            coroutineScope.launch(Dispatchers.Main) {
                delay(500)
                listener?.onPlaybackStateChanged(ExoPlayer.STATE_READY)
                listener?.onIsPlayingChanged(isPlaying)
            }
        }

        override fun trackPositionChanged(newStatus: MPDStatus) {
            status = newStatus
            listener?.onIsPlayingChanged(isPlaying)
            listener?.onVolumeChanged(volume)
        }
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
        Log.d(TAG, "setMediaItems")
    }

    override fun seekTo(positionMs: Long) {
        Log.d(TAG, "seekTo: position: $positionMs")
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        Log.d(TAG, "seekTo: mediaItemIndex: $mediaItemIndex")

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
        Log.d(TAG, "prepare")

        coroutineScope.launch {
            val credentials = getRadioSettings(dataStore)
            if (helper.connect(credentials)) {
                Log.d(TAG, "connected")

                getPlaylist()

                status = try {
                    helper.getStatus()
                } catch (e: CommunicationException) {
                    helper.reconnect()
                    helper.getStatus()
                }

                helper.startMonitor()

                helper.playId(currentMediaItem.mediaId)

                listener?.onPlaybackStateChanged(ExoPlayer.STATE_READY)
                listener?.onVolumeChanged(volume)

                coroutineScope.launch(Dispatchers.Main) {
                    delay(500)
                    listener?.onIsPlayingChanged(isPlaying)
                }

            } else {
                Log.d(TAG, "connection failure")
                listener?.onPlayerError(
                    PlaybackException(
                        "Connection Failed",
                        null,
                        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                    )
                )
            }
        }
    }

    private fun getPlaylist() {
        playlist = try {
            helper.getPlaylist()
        } catch (e: CommunicationException) {
            helper.reconnect()
            helper.getPlaylist()
        }
        listener?.onMediaMetadataChanged(mediaMetadata)
    }
    private fun updatePlaylist() {
        playlist = try {
            helper.updatePlaylist()
        } catch (e: CommunicationException) {
            helper.reconnect()
            helper.updatePlaylist()
        }
        listener?.onMediaMetadataChanged(mediaMetadata)
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        Log.d(TAG, "setPlayWhenReady")
    }

    override fun getMediaItemCount(): Int {
        Log.d(TAG, "getMediaItemCount")
        return playlist?.size!!
    }

    override fun isPlaying(): Boolean {
        return status?.state == MPDState.PLAYING
    }

    override fun play() {
        Log.d(TAG, "play")
        coroutineScope.launch {
            helper.play()
        }
    }

    override fun pause() {
        Log.d(TAG, "pause")
        coroutineScope.launch {
            helper.pause()
        }
    }

    override fun seekToPreviousMediaItem() {
        Log.d(TAG, "seekToPreviousMediaItem")
        coroutineScope.launch {
            helper.previous()
        }
    }

    override fun seekToNextMediaItem() {
        Log.d(TAG, "seekToNextMediaItem")
        coroutineScope.launch {
            helper.next()
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
        Log.d(TAG, "stop")
        helper.stopMonitor()
        helper.disconnect()
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
        Log.d(TAG, "getCurrentMediaItemIndex")
        return preset
    }

    override fun getCurrentMediaItem(): MediaItem {
        Log.d(TAG, "getCurrentMediaItem")

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
        return 0
    }

    @Deprecated("Deprecated in Java", ReplaceWith("0"))
    override fun getPreviousWindowIndex(): Int {
        return 0
    }

    override fun getPreviousMediaItemIndex(): Int {
        return 0
    }

    override fun getMediaItemAt(index: Int): MediaItem {
        TODO("Not yet implemented")
    }

    override fun getDuration(): Long {
        Log.d(TAG, "getDuration")
        return status?.totalTime!!
    }

    override fun getCurrentPosition(): Long {
        Log.d(TAG, "getCurrentPosition")
        return status?.elapsedTime!! * 1000
    }

    override fun setVolume(audioVolume: Float) {
        Log.d(TAG, "setVolume:$audioVolume")
        coroutineScope.launch {
            helper.setVolume((audioVolume * 100).toInt())
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

    override fun setPriorityTaskManager(priorityTaskManager: PriorityTaskManager?) {
    }

    override fun isSleepingForOffload(): Boolean {
        return false
    }

    override fun isTunnelingEnabled(): Boolean {
        return false
    }
    init {
        helper.statusChangedListener = statusChangedListener
    }
}

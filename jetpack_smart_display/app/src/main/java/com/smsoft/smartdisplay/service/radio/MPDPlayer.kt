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

        listener?.onSeekProcessed()
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
        TODO("Not yet implemented")
    }
    override fun getApplicationLooper(): Looper {
        TODO("Not yet implemented")
    }
        override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun setMediaItem(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        TODO("Not yet implemented")
    }

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {
        TODO("Not yet implemented")
    }

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {
        TODO("Not yet implemented")
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun removeMediaItem(index: Int) {
        TODO("Not yet implemented")
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun clearMediaItems() {
        TODO("Not yet implemented")
    }

    override fun isCommandAvailable(command: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun canAdvertiseSession(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAvailableCommands(): Player.Commands {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun prepare(mediaSource: MediaSource) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun prepare(mediaSource: MediaSource, resetPosition: Boolean, resetState: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getPlaybackState(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlaybackSuppressionReason(): Int {
        TODO("Not yet implemented")
    }
    override fun getPlayerError(): ExoPlaybackException? {
        TODO("Not yet implemented")
    }
    override fun getPlayWhenReady(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRepeatMode(repeatMode: Int) {
        TODO("Not yet implemented")
    }

    override fun getRepeatMode(): Int {
        TODO("Not yet implemented")
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getShuffleModeEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLoading(): Boolean {
        TODO("Not yet implemented")
    }

    override fun seekToDefaultPosition() {
        TODO("Not yet implemented")
    }

    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        TODO("Not yet implemented")
    }
    override fun getSeekBackIncrement(): Long {
        TODO("Not yet implemented")
    }

    override fun seekBack() {
        TODO("Not yet implemented")
    }

    override fun getSeekForwardIncrement(): Long {
        TODO("Not yet implemented")
    }

    override fun seekForward() {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun hasPrevious(): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun hasPreviousWindow(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasPreviousMediaItem(): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun previous() {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun seekToPreviousWindow() {
        TODO("Not yet implemented")
    }
    override fun getMaxSeekToPreviousPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun seekToPrevious() {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun hasNext(): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun hasNextWindow(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasNextMediaItem(): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun next() {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun seekToNextWindow() {
        TODO("Not yet implemented")
    }

    override fun seekToNext() {
        TODO("Not yet implemented")
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        TODO("Not yet implemented")
    }

    override fun setPlaybackSpeed(speed: Float) {
        TODO("Not yet implemented")
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        TODO("Not yet implemented")
    }

    override fun stop() {
        Log.d(TAG, "stop")
        helper.stopMonitor()
        helper.disconnect()
    }

    @Deprecated("Deprecated in Java")
    override fun stop(reset: Boolean) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun getCurrentTracks(): Tracks {
        TODO("Not yet implemented")
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        TODO("Not yet implemented")
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        TODO("Not yet implemented")
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        TODO("Not yet implemented")
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        TODO("Not yet implemented")
    }

    override fun getCurrentManifest(): Any? {
        TODO("Not yet implemented")
    }

    override fun getCurrentTimeline(): Timeline {
        TODO("Not yet implemented")
    }

    override fun getCurrentPeriodIndex(): Int {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getCurrentWindowIndex(): Int {
        TODO("Not yet implemented")
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

    @Deprecated("Deprecated in Java")
    override fun getNextWindowIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getNextMediaItemIndex(): Int {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPreviousWindowIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getPreviousMediaItemIndex(): Int {
        TODO("Not yet implemented")
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
            0F
        }
    }

    override fun getBufferedPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getBufferedPercentage(): Int {
        TODO("Not yet implemented")
    }

    override fun getTotalBufferedDuration(): Long {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun isCurrentWindowDynamic(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCurrentMediaItemDynamic(): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun isCurrentWindowLive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCurrentMediaItemLive(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentLiveOffset(): Long {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun isCurrentWindowSeekable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCurrentMediaItemSeekable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPlayingAd(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentAdGroupIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        TODO("Not yet implemented")
    }

    override fun getContentDuration(): Long {
        TODO("Not yet implemented")
    }

    override fun getContentPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getContentBufferedPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getAudioAttributes(): AudioAttributes {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurface() {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurface(surface: Surface?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurface(surface: Surface?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {
        TODO("Not yet implemented")
    }

    override fun setVideoTextureView(textureView: TextureView?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoTextureView(textureView: TextureView?) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun isDeviceMuted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDeviceVolume(volume: Int) {
        TODO("Not yet implemented")
    }

    override fun increaseDeviceVolume() {
        TODO("Not yet implemented")
    }

    override fun decreaseDeviceVolume() {
        TODO("Not yet implemented")
    }

    override fun setDeviceMuted(muted: Boolean) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getAudioComponent(): ExoPlayer.AudioComponent? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getVideoComponent(): ExoPlayer.VideoComponent? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getTextComponent(): ExoPlayer.TextComponent? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getDeviceComponent(): ExoPlayer.DeviceComponent? {
        TODO("Not yet implemented")
    }

    override fun addAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {
        TODO("Not yet implemented")
    }

    override fun removeAudioOffloadListener(listener: ExoPlayer.AudioOffloadListener) {
        TODO("Not yet implemented")
    }

    override fun getAnalyticsCollector(): AnalyticsCollector {
        TODO("Not yet implemented")
    }

    override fun addAnalyticsListener(listener: AnalyticsListener) {
        TODO("Not yet implemented")
    }

    override fun removeAnalyticsListener(listener: AnalyticsListener) {
        TODO("Not yet implemented")
    }

    override fun getRendererCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getRendererType(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getRenderer(index: Int): Renderer {
        TODO("Not yet implemented")
    }

    override fun getTrackSelector(): TrackSelector? {
        TODO("Not yet implemented")
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

    @Deprecated("Deprecated in Java")
    override fun retry() {
        TODO("Not yet implemented")
    }

    override fun setMediaSources(mediaSources: MutableList<MediaSource>) {
        TODO("Not yet implemented")
    }

    override fun setMediaSources(mediaSources: MutableList<MediaSource>, resetPosition: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setMediaSources(
        mediaSources: MutableList<MediaSource>,
        startMediaItemIndex: Int,
        startPositionMs: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun setMediaSource(mediaSource: MediaSource) {
        TODO("Not yet implemented")
    }

    override fun setMediaSource(mediaSource: MediaSource, startPositionMs: Long) {
        TODO("Not yet implemented")
    }

    override fun setMediaSource(mediaSource: MediaSource, resetPosition: Boolean) {
        TODO("Not yet implemented")
    }

    override fun addMediaSource(mediaSource: MediaSource) {
        TODO("Not yet implemented")
    }

    override fun addMediaSource(index: Int, mediaSource: MediaSource) {
        TODO("Not yet implemented")
    }

    override fun addMediaSources(mediaSources: MutableList<MediaSource>) {
        TODO("Not yet implemented")
    }

    override fun addMediaSources(index: Int, mediaSources: MutableList<MediaSource>) {
        TODO("Not yet implemented")
    }

    override fun setShuffleOrder(shuffleOrder: ShuffleOrder) {
        TODO("Not yet implemented")
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setAudioSessionId(audioSessionId: Int) {
        TODO("Not yet implemented")
    }

    override fun getAudioSessionId(): Int {
        TODO("Not yet implemented")
    }

    override fun setAuxEffectInfo(auxEffectInfo: AuxEffectInfo) {
        TODO("Not yet implemented")
    }

    override fun clearAuxEffectInfo() {
        TODO("Not yet implemented")
    }

    override fun setPreferredAudioDevice(audioDeviceInfo: AudioDeviceInfo?) {
        TODO("Not yet implemented")
    }

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getSkipSilenceEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVideoScalingMode(videoScalingMode: Int) {
        TODO("Not yet implemented")
    }

    override fun getVideoScalingMode(): Int {
        TODO("Not yet implemented")
    }

    override fun setVideoChangeFrameRateStrategy(videoChangeFrameRateStrategy: Int) {
        TODO("Not yet implemented")
    }

    override fun getVideoChangeFrameRateStrategy(): Int {
        TODO("Not yet implemented")
    }

    override fun setVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {
        TODO("Not yet implemented")
    }

    override fun clearVideoFrameMetadataListener(listener: VideoFrameMetadataListener) {
        TODO("Not yet implemented")
    }

    override fun setCameraMotionListener(listener: CameraMotionListener) {
        TODO("Not yet implemented")
    }

    override fun clearCameraMotionListener(listener: CameraMotionListener) {
        TODO("Not yet implemented")
    }

    override fun createMessage(target: PlayerMessage.Target): PlayerMessage {
        TODO("Not yet implemented")
    }

    override fun setSeekParameters(seekParameters: SeekParameters?) {
        TODO("Not yet implemented")
    }

    override fun getSeekParameters(): SeekParameters {
        TODO("Not yet implemented")
    }

    override fun setForegroundMode(foregroundMode: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setPauseAtEndOfMediaItems(pauseAtEndOfMediaItems: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getPauseAtEndOfMediaItems(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAudioFormat(): Format? {
        TODO("Not yet implemented")
    }

    override fun getVideoFormat(): Format? {
        TODO("Not yet implemented")
    }

    override fun getAudioDecoderCounters(): DecoderCounters? {
        TODO("Not yet implemented")
    }

    override fun getVideoDecoderCounters(): DecoderCounters? {
        TODO("Not yet implemented")
    }

    override fun setHandleAudioBecomingNoisy(handleAudioBecomingNoisy: Boolean) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun setHandleWakeLock(handleWakeLock: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setWakeMode(wakeMode: Int) {
        TODO("Not yet implemented")
    }

    override fun setPriorityTaskManager(priorityTaskManager: PriorityTaskManager?) {
        TODO("Not yet implemented")
    }

    override fun experimentalSetOffloadSchedulingEnabled(offloadSchedulingEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun experimentalIsSleepingForOffload(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isTunnelingEnabled(): Boolean {
        TODO("Not yet implemented")
    }
    init {
        helper.statusChangedListener = statusChangedListener
    }
}

package net.blumia.pcm.privatecloudmusic

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * We use a PlayHolder to manage the MediaPlayer status and PlaybackInfoListener status
 * TODO: dealing with audio focus
 */
class PlayerHolder(context: Context): PlayerAdapter,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener {

    private var mMediaPlayer: MediaPlayer? = null
    private var mPlaybackInfoListener: PlaybackInfoListener? = null
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekbarPositionUpdateTask: Runnable? = null
    var mCurrentLoadedFileUrl: String = "File not loaded"

    companion object {
        private const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 250L
    }

    fun setPlaybackInfoListener(listener: PlaybackInfoListener) {
        mPlaybackInfoListener = listener
    }

    override fun init() {
        if (mMediaPlayer != null) return
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setOnCompletionListener(this)
        mMediaPlayer!!.setOnErrorListener(this)
        mMediaPlayer!!.setOnPreparedListener(this)
        mMediaPlayer!!.setOnBufferingUpdateListener(this)
    }

    override fun release() {
        if (mMediaPlayer == null) return
        mMediaPlayer!!.release()
        mMediaPlayer = null
    }

    override fun load(uri: String) {
        init()
        mMediaPlayer!!.reset()
        try {
            mMediaPlayer!!.setDataSource(uri)
        } catch (e: Exception) {
            Log.e("MediaPlayer", e.toString())
        }
        mCurrentLoadedFileUrl = uri
        mMediaPlayer!!.prepareAsync()
    }

    override fun play() {
        if (mMediaPlayer == null || mMediaPlayer!!.isPlaying) return
        mMediaPlayer!!.start()
        mPlaybackInfoListener?.onStateChanged(PlaybackInfoListener.PlaybackState.PLAYING)
        startUpdatingCallbackWithPosition()
    }

    override fun reset() {
        if (mMediaPlayer == null) return
        mMediaPlayer!!.reset()
        mPlaybackInfoListener?.onStateChanged(PlaybackInfoListener.PlaybackState.RESET)
        stopUpdatingCallbackWithPosition(true)
    }

    override fun pause() {
        if (mMediaPlayer == null || !mMediaPlayer!!.isPlaying) return
        mMediaPlayer!!.pause()
        mPlaybackInfoListener?.onStateChanged(PlaybackInfoListener.PlaybackState.PAUSED)
    }

    override fun seekTo(pos: Int) {
        if (mMediaPlayer == null) return
        mMediaPlayer!!.seekTo(pos)
    }

    override fun isPlaying(): Boolean{
        if (mMediaPlayer == null) return false
        return mMediaPlayer!!.isPlaying
    }

    private fun startUpdatingCallbackWithPosition() {
        if (mExecutor == null) mExecutor = Executors.newSingleThreadScheduledExecutor()
        if (mSeekbarPositionUpdateTask == null) mSeekbarPositionUpdateTask = Runnable {
            updateProgressCallbackTask()
        }
        mExecutor!!.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        )
    }

    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        if (mExecutor == null) return
        mExecutor!!.shutdown()
        mExecutor = null
        mSeekbarPositionUpdateTask = null
        if (resetUIPlaybackPosition) {
            mPlaybackInfoListener?.onPositionChanged(0, mMediaPlayer!!.duration)
        }
    }

    private fun updateProgressCallbackTask() {
        if (mMediaPlayer == null || !mMediaPlayer!!.isPlaying) return
        mPlaybackInfoListener?.onPositionChanged(
                mMediaPlayer!!.currentPosition,
                mMediaPlayer!!.duration)
    }

    //region MediaPlayer Listeners

    override fun onCompletion(mp: MediaPlayer?) {
        stopUpdatingCallbackWithPosition(true)
        mPlaybackInfoListener?.onStateChanged(PlaybackInfoListener.PlaybackState.COMPLETED)
        mPlaybackInfoListener?.onPlaybackCompleted()
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        val msg = when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "MEDIA ERROR SERVER DIED $extra"
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> "MEDIA ERROR UNKNOWN $extra"
            else -> "MEDIA ERROR WHAT=$what $extra"
        }
        mPlaybackInfoListener?.onError(msg)
        Log.e("PlayerHandler", msg)

        return false
    }

    override fun onPrepared(p0: MediaPlayer?) {
        play()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, pos: Int) {
        mPlaybackInfoListener?.onBufferUpdated(pos, mp!!.duration)
    }

    //endregion
}
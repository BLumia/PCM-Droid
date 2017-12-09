package net.blumia.pcm.privatecloudmusic

/**
 * Created by Gary Wang on 2017/12/9.
 */
class PlaybackInfoListener {

    enum class PlaybackState(val value: Int){
        INVALID(-1),
        PLAYING(0),
        PAUSED(1),
        RESET(2),
        COMPLETED(3)
    }

    fun onStateChanged(state: PlaybackState) {}

    fun onPlaybackCompleted() {}

    fun onPositionChanged(pos: Int) {}

    fun onBufferUpdated(bufferPos: Int, duration: Int) {}

    fun onError(msg: String) {}

}
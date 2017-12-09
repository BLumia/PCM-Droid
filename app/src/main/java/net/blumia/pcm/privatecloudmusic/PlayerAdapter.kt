package net.blumia.pcm.privatecloudmusic

/**
 * Created by Gary Wang on 2017/12/9.
 */
interface PlayerAdapter {
    fun init()
    fun release()
    fun load(uri: String)
    fun play()
    fun reset()
    fun pause()
    fun seekTo(pos: Int)
    fun isPlaying(): Boolean
}


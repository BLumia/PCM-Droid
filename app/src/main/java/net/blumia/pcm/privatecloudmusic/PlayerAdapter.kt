package net.blumia.pcm.privatecloudmusic

/**
 * Interfaces for MediaPlayer holder.
 */
interface PlayerAdapter {
    fun init()
    fun release()
    fun load(uri: String)
    fun play()
    fun reset()
    fun pause()
    fun seekTo(pos: Int)
    fun setLoop(loop: Boolean)
    fun isPlaying(): Boolean
}


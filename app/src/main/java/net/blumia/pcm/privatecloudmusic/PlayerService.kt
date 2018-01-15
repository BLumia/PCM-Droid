package net.blumia.pcm.privatecloudmusic

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class PlayerService: Service() {

    private var mPlayerHolder: PlayerHolder? = null
    private var mPlaylist: ArrayList<MusicItem>? = null
    private var mCurrentMusicItem: MusicItem? = null
    private var mTimeIntent: Intent? = null
    private var prefs: Prefs? = null

    companion object {
        const val DO_PLAY = 0
        const val DO_PAUSE = 1
        const val DO_NEXT = 2
        const val DO_PREV = 3
        const val DO_RESUME = 4
        const val DO_SEEK = 5
        const val DO_LOOP = 6
        const val ACTION_UPDATE_TIME = "net.blumia.pcm.privatecloudmusic.ACTION_UPDATE_TIME"
        //AudioPlayer Channel ID and notification ID
        private const val CHANNEL_ID = "net.blumia.pcm.MEDIA_PLAYBACK_CHANNEL"
        private const val NOTIFICATION_ID = 616
    }

    //region Service LocalBinder and onBind

    private var iBinder = LocalBinder()

    inner class LocalBinder: Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    //endregion

    //region create and destroy

    override fun onCreate() {
        super.onCreate()

        prefs = Prefs(this)

        mPlayerHolder = PlayerHolder(this)
        mPlayerHolder!!.setPlaybackInfoListener(ServicePlaybackInfoListener())
        mPlayerHolder!!.init()

        mTimeIntent = Intent(ACTION_UPDATE_TIME)
        registerPlayActionBroadcastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()

        mPlayerHolder?.pause()
        mPlayerHolder?.release()
    }

    //endregion

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            //An audio file is passed to the service through putExtra();
            mPlaylist = prefs!!.playlist
            mPlayerHolder!!.load(getFileUrl())
            Log.e("playback url: ", mPlayerHolder!!.mCurrentLoadedFileUrl)
            //mediaFileUriStr = intent.extras!!.getString("media")
        } catch (e: NullPointerException) {
            stopSelf()
        }

        //Request audio focus
        //if (!requestAudioFocus()) {
        //    //Could not gain focus
        //    stopSelf()
        //}

        return super.onStartCommand(intent, flags, startId)
    }

    //region Listeners

    inner class ServicePlaybackInfoListener: PlaybackInfoListener {
        override fun onPositionChanged(pos: Int, duration: Int) {
            super.onPositionChanged(pos, duration)
            if (mPlayerHolder == null) return
            mTimeIntent?.putExtra("isPlaying", mPlayerHolder!!.isPlaying())
            mTimeIntent?.putExtra("progress", pos)
            mTimeIntent?.putExtra("musicLength", duration)
            mTimeIntent?.putExtra("totalTime", toTime(duration))
            mTimeIntent?.putExtra("curTime", toTime(pos))
            mTimeIntent?.putExtra("songName", mCurrentMusicItem!!.name)
            sendBroadcast(mTimeIntent)
        }

        override fun onStateChanged(state: PlaybackInfoListener.PlaybackState) {
            super.onStateChanged(state)
            if (mPlayerHolder == null) return
            when (state) {
                PlaybackInfoListener.PlaybackState.PAUSED -> {
                    mTimeIntent?.putExtra("isPlaying", false)
                }
                else -> {}
            }
            sendBroadcast(mTimeIntent)
        }
    }

    //endregion

    //region BroadcastReceivers

    private fun registerPlayActionBroadcastReceiver() {
        val playActionBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (mPlayerHolder == null) return
                val action = intent!!.getIntExtra("do", DO_PLAY)
                if (action != DO_PLAY) {
                    when(action) {
                        DO_PAUSE -> mPlayerHolder!!.pause()
                        DO_NEXT -> {
                            if (mPlaylist!!.size <= prefs!!.curSongIndex + 1) return
                            if (mPlaylist!![prefs!!.curSongIndex + 1].type == MusicItemType.MUSIC) {
                                prefs!!.curSongIndex++
                                mPlayerHolder!!.load(getFileUrl())
                                mPlayerHolder!!.play()
                            }
                        }
                        DO_PREV -> {
                            if (prefs!!.curSongIndex < 1) return
                            if (mPlaylist!![prefs!!.curSongIndex - 1].type == MusicItemType.MUSIC) {
                                prefs!!.curSongIndex--
                                mPlayerHolder!!.load(getFileUrl())
                                mPlayerHolder!!.play()
                            }
                        }
                        DO_RESUME -> mPlayerHolder!!.play()
                        DO_SEEK -> {
                            mPlayerHolder!!.seekTo(intent.getIntExtra("pos", 0))
                        }
                        DO_LOOP -> mPlayerHolder!!.setLoop(intent.getBooleanExtra("loop", false))
                    }
                    return
                }
                // play audio
                if (prefs?.playlist != null) {
                    mPlayerHolder!!.load(getFileUrl())
                    mPlayerHolder!!.play()
                } else {
                    stopSelf()
                }
            }
        }
        val filter = IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playActionBroadcastReceiver, filter)
    }

    //endregion

    //region Utils

    private fun getFileUrl(): String {
        mCurrentMusicItem = mPlaylist!![prefs!!.curSongIndex]
        return prefs!!.curWebFileRootPath + prefs!!.curWebFileRelativePath + '/' + mCurrentMusicItem!!.filePathAndName
    }

    private fun toTime(time: Int): String {
        if (time < 0) return "OwO"
        val minute = time / 1000 / 60
        val s = time / 1000 % 60
        val mm = if (minute < 10)
            "0" + minute
        else
            minute.toString() + ""
        val ss = if (s < 10)
            "0" + s
        else
            "" + s
        return mm + ":" + ss
    }

    //endregion
}
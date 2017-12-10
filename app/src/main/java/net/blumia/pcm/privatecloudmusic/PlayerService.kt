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
    private var prefs: Prefs? = null

    companion object {
        const val DO_PLAY = 0
        const val DO_PAUSE = 1
        const val DO_NEXT = 2
        const val DO_PREV = 3
        const val ACTION_UPDATE_TIME = "net.blumia.pcm.privatecloudmusic.ACTION_UPDATE_TIME"
        //AudioPlayer Channel ID and notification ID
        private const val CHANNEL_ID = "net.blumia.pcm.MEDIA_PLAYBACK_CHANNEL"
        private const val NOTIFICATION_ID = 616
        private const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 250L
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
        //playbackInfoListener
        mPlayerHolder!!.init()
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

    //region BroadcastReceivers

    private fun registerPlayActionBroadcastReceiver() {
        val playActionBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.getIntExtra("do", DO_PLAY)
                if (action != DO_PLAY) {
                    when(action) {
                        DO_PAUSE -> mPlayerHolder!!.pause()
                        DO_NEXT -> {}
                        DO_PREV -> {}
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

    //endregion
}
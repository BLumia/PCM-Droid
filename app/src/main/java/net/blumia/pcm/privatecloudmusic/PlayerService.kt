package net.blumia.pcm.privatecloudmusic

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.media.MediaPlayer
import android.media.AudioManager
import android.util.Log
import java.io.IOException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.media.session.MediaSessionManager
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.graphics.BitmapFactory
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.media.AudioAttributes
import android.telephony.TelephonyManager
import android.telephony.PhoneStateListener
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/**
 * Created by wzc78 on 2017/12/4.
 */
// TODO: google recommend use a PlayerHolder to control(hold) MediaPlayer instance.
// refer to https://github.com/googlesamples/android-SimpleMediaPlayer/blob/master/app/src/main/java/com/example/android/mediaplayersample/MediaPlayerHolder.java
class PlayerService : Service(),
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    // Binder given to clients
    private val iBinder = LocalBinder()
    private var mExecutor: ScheduledExecutorService? = null
    private var mSeekbarPositionUpdateTask: Runnable? = null
    private var mTimeIntent: Intent? = null

    private var mediaPlayer: MediaPlayer? = null
    private var mBufferPercent: Int = 0
    //Used to pause/resume MediaPlayer
    private var resumePosition: Int = 0
    //path to the audio file
    private var mediaFileItem: MusicItem? = null
    private var mediaFileUriStr: String? = null
    //audio focus
    private var audioManager: AudioManager? = null
    private var playlist: ArrayList<MusicItem>? = null
    private var prefs: Prefs? = null

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    //MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    enum class PlaybackStatus {
        PLAYING,
        PAUSED
    }

    companion object {
        const val ACTION_PLAY = "net.blumia.pcm.privatecloudmusic.ACTION_PLAY"
        const val ACTION_PAUSE = "net.blumia.pcm.privatecloudmusic.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "net.blumia.pcm.privatecloudmusic.ACTION_PREVIOUS"
        const val ACTION_NEXT = "net.blumia.pcm.privatecloudmusic.ACTION_NEXT"
        const val ACTION_STOP = "net.blumia.pcm.privatecloudmusic.ACTION_STOP"
        const val ACTION_UPDATE_TIME = "net.blumia.pcm.privatecloudmusic.ACTION_UPDATE_TIME"
        //AudioPlayer Channel ID and notification ID
        private const val CHANNEL_ID = "net.blumia.pcm.MEDIA_PLAYBACK_CHANNEL"
        private const val NOTIFICATION_ID = 616
        private const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 250L
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio()

        mTimeIntent = Intent(ACTION_UPDATE_TIME)
        prefs = Prefs(this)
    }

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    //The system calls this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            //An audio file is passed to the service through putExtra();
            playlist = prefs!!.playlist
            mediaFileUriStr = getFileUrl()
            Log.e("playback url: ", mediaFileUriStr)
            //mediaFileUriStr = intent.extras!!.getString("media")
        } catch (e: NullPointerException) {
            stopSelf()
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }

        if (mediaFileUriStr != null && mediaFileUriStr !== "") {
            try {
                initMediaSession()
                initMediaPlayer()
                buildNotification(PlaybackStatus.PLAYING)
            } catch(e: RemoteException ) {
                e.printStackTrace()
                stopSelf()
            }
        }
        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            stopMedia()
            mediaPlayer!!.release()
        }
        removeAudioFocus()
    }

    override fun onCompletion(mp: MediaPlayer) {
        //Invoked when playback of a media source has completed.
        stopMedia()
        //stop the service
        stopSelf()
    }

    //Handle errors
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra)
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra)
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra)
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        playMedia()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        mBufferPercent = percent * mp!!.duration /100
    }

    private fun getFileUrl(): String {
        mediaFileItem = playlist!![prefs!!.curSongIndex]
        return prefs!!.curWebFileRootPath + prefs!!.curWebFileRelativePath + '/' + mediaFileItem!!.filePathAndName
    }

    //region media playback

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        //Set up MediaPlayer event listeners
        mediaPlayer!!.setOnCompletionListener(this)
        mediaPlayer!!.setOnErrorListener(this)
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnBufferingUpdateListener(this)
        //mediaPlayer!!.setOnSeekCompleteListener(this)
        //mediaPlayer!!.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer!!.reset()

        mediaPlayer!!.setAudioAttributes(AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        try {
            // Set the data source to the mediaFile location
            mediaPlayer!!.setDataSource(mediaFileUriStr)
        } catch (e: IOException) {
            e.printStackTrace()
            stopSelf()
        }

        mediaPlayer!!.prepareAsync()
    }

    private fun playMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            startUpdatingCallbackWithPosition()
        }
    }

    private fun stopMedia() {
        if (mediaPlayer == null) return
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            stopUpdatingCallbackWithPosition(true)
        }
    }

    private fun pauseMedia() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            resumePosition = mediaPlayer!!.currentPosition
            stopUpdatingCallbackWithPosition(false)
        }
    }

    private fun resumeMedia() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.seekTo(resumePosition)
            mediaPlayer!!.start()
            startUpdatingCallbackWithPosition()
        }
    }

    //endregion

    inner class LocalBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    override fun onAudioFocusChange(focusState: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null)
                    initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
                mediaPlayer!!.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.setVolume(0.1f, 0.1f)
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // https://stackoverflow.com/questions/9227645/is-there-any-equivalent-function-of-requestaudiofocus-in-android-api-level-7
        val result = audioManager!!.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        //Could not gain focus
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager!!.abandonAudioFocus(this)
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return  //mediaSessionManager exists

        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession!!.controller.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession!!.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                //Stop the service
                stopSelf()
            }
        })
    }

    private fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(resources,
                R.drawable.notification_bg) //replace with medias albumArt
        // Update the current metadata
        mediaSession!!.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "activeAudio.getArtist()")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "activeAudio.getAlbum()")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "activeAudio.getTitle()")
                .build())
    }

    private fun skipToNext() {

        // do switch to next song

        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }

    private fun skipToPrevious() {

        // do switch to prev song

        stopMedia()
        //reset mediaPlayer
        mediaPlayer!!.reset()
        initMediaPlayer()
    }

    //Becoming noisy
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                //if at least one call exists or the phone is ringing
                //pause the MediaPlayer
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager!!.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE)
    }

    private val playNewAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //Get the new media index form SharedPreferences
            //audioIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (true/*audioIndex !== -1 && audioIndex < audioList.size()*/) {
                //index is in a valid range
                //activeAudio = audioList.get(audioIndex)
                mediaFileUriStr = getFileUrl()
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia()
            mediaPlayer!!.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING)
        }
    }

    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    private fun buildNotification(playbackStatus: PlaybackStatus) {

        var notificationAction = android.R.drawable.ic_media_pause//needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus === PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playbackStatus === PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        val largeIcon = BitmapFactory.decodeResource(resources,
                R.drawable.notification_bg) //replace with your own image

        // Create a new Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        val nowPlayingItem = playlist!![prefs!!.curSongIndex]
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession!!.sessionToken)
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(nowPlayingItem.name)
                .setContentTitle("Private Cloud Music")
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2)) as NotificationCompat.Builder

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val mNotificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // The id of the channel.
        val id = CHANNEL_ID
        // The user-visible name of the channel.
        val name = "Media playback" as CharSequence
        // The user-visible description of the channel.
        val description = "Media playback controls"
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(id, name, importance)
        // Configure the notification channel.
        mChannel.description = description
        mChannel.setShowBadge(false)
        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager.createNotificationChannel(mChannel)
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, PlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            else -> {
            }
        }
        return null
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return

        val actionString = playbackAction.action
        when {
            actionString!!.equals(ACTION_PLAY, ignoreCase = true) -> transportControls!!.play()
            actionString.equals(ACTION_PAUSE, ignoreCase = true) -> transportControls!!.pause()
            actionString.equals(ACTION_NEXT, ignoreCase = true) -> transportControls!!.skipToNext()
            actionString.equals(ACTION_PREVIOUS, ignoreCase = true) -> transportControls!!.skipToPrevious()
            actionString.equals(ACTION_STOP, ignoreCase = true) -> transportControls!!.stop()
        }
    }

    private fun startUpdatingCallbackWithPosition() {
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (mSeekbarPositionUpdateTask == null) {
            mSeekbarPositionUpdateTask = Runnable { updateProgressCallbackTask(false) }
        }
        mExecutor!!.scheduleAtFixedRate(mSeekbarPositionUpdateTask, 0, PLAYBACK_POSITION_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS)
    }

    // Reports media playback position to mPlaybackProgressCallback.
    private fun stopUpdatingCallbackWithPosition(resetUIPlaybackPosition: Boolean) {
        Log.e("stop", "stopUpdatingCallbackWithPosition($resetUIPlaybackPosition)")

        if (mExecutor != null) {
            mExecutor!!.shutdownNow()
            mExecutor = null
            mSeekbarPositionUpdateTask = null

            updateProgressCallbackTask(resetUIPlaybackPosition)
        }
    }

    private fun updateProgressCallbackTask(resetUIPlaybackPosition: Boolean) {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            val currentPosition = mediaPlayer!!.currentPosition
            val musicLength = mediaPlayer!!.duration

            mTimeIntent?.putExtra("isPlaying", if (!resetUIPlaybackPosition) mediaPlayer!!.isPlaying else false)
            mTimeIntent?.putExtra("bufferPercent", mBufferPercent)
            mTimeIntent?.putExtra("progress", if (resetUIPlaybackPosition) musicLength else currentPosition)
            mTimeIntent?.putExtra("musicLength", musicLength)
            mTimeIntent?.putExtra("totalTime", toTime(musicLength))
            mTimeIntent?.putExtra("curTime", toTime(currentPosition))
            mTimeIntent?.putExtra("songName", mediaFileItem!!.name)
            sendBroadcast(mTimeIntent)
        }
    }

    private fun toTime(time: Int): String {
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
}
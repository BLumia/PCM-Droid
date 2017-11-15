package net.blumia.pcm.privatecloudmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by wzc78 on 2017/11/15.
 */

public class PlayerService extends Service {

    public static MediaPlayer mp;
    public static final String ACTION_PLAY = "play";

    @Override
    public void onCreate() {
        mp = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "service " + action);
        if (action.equals(ACTION_PLAY));
            //processPlayRequest();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // ??????????????????????????
        return null;
    }
}

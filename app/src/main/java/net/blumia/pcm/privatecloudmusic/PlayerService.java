package net.blumia.pcm.privatecloudmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
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
    private final IBinder mBinder = new PlayerServiceIBinder();

    public interface OnStateChangeListenr {

        void onPlayProgressChange(MusicItem item);
        void onPlay(MusicItem item);
        void onPause(MusicItem item);
    }

    public class PlayerServiceIBinder extends Binder {

    }

    @Override
    public void onCreate() {
        mp = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (ACTION_PLAY.equals(action)) {
                    //dosomething
                }

            }
            Log.d(TAG, "service " + action);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}

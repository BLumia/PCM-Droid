package net.blumia.pcm.privatecloudmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by wzc78 on 2017/11/15.
 */

public class PlayerService extends Service {

    public static MediaPlayer mp;
    public static final String ACTION_PLAY = "play";
    private final IBinder mBinder = new PlayerServiceIBinder();
    public final static int MSG_TIME_INFO_SEC = 0;

    public interface OnStateChangeListenr {
        void onPlayProgressChange(MusicItem item);
        void onPlay(MusicItem item);
        void onPause(MusicItem item);
    }

    private List<OnStateChangeListenr> mListenerList = new ArrayList<OnStateChangeListenr>();

    public class PlayerServiceIBinder extends Binder {
        public void play() {
            playInner();
        }
        public void regOnStateChangedListener(OnStateChangeListenr l) {
            mListenerList.add(l);
        }
        public void unregOnStateChangedListener(OnStateChangeListenr l) {
            mListenerList.remove(l);
        }
    }

    private void prepareToPlay(MusicItem item) {
        try {
            mp.reset();
            mp.setDataSource(PlayerService.this, Uri.parse(item.musicUrl));
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playInner() {
        prepareToPlay(new MusicItem("https://pcm.blumia.cn/%E6%B5%8B%E8%AF%95/Resonate.mp3",
                "Resonate.mp3", 123123, 123123));
        mp.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mp = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListenerList.clear();
        mp.release();
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

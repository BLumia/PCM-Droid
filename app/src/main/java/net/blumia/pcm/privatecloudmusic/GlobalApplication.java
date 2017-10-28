package net.blumia.pcm.privatecloudmusic;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by wzc78 on 2017/10/25.
 */

public class GlobalApplication extends Application {

    private static GlobalApplication instance;

    public static GlobalApplication getInstance(){
        return instance;
    }

    public SQLiteDatabase serverDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        serverDatabase = SQLiteUtils.InitSQLiteDatabase(this);
    }
}

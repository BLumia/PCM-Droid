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

        serverDatabase = openOrCreateDatabase("PCM_SRV_DB", MODE_PRIVATE, null);
        serverDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS SrvList(" +
                    "ServerID INTEGER PRIMARY KEY," +
                    "ServerName VARCHAR NOT NULL," +
                    "APIUrl VARCHAR NOT NULL," +
                    "FileRootUrl VARCHAR NOT NULL," +
                    "Password VARCHAR" +
                ");"
        );
    }
}

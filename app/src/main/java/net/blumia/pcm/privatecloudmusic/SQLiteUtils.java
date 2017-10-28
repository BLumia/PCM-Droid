package net.blumia.pcm.privatecloudmusic;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wzc78 on 2017/10/25.
 */

public class SQLiteUtils {

    public static List<PCMServerInfo> GetServerInfoList() {

        List<PCMServerInfo> retList = new ArrayList<PCMServerInfo>();

        String[] colArgs = {"ServerID", "ServerName", "APIUrl", "FileRootUrl", "Password"};
        Cursor c = GlobalApplication.getInstance().serverDatabase.query("SrvList", colArgs, null, null, null, null, null);
        if (c.getCount() != 0) {
            while(c.moveToNext()) {
                int _id = c.getInt(0);
                String _srvName = c.getString(1);
                String _apiUrl = c.getString(2);
                String _fileRootUrl = c.getString(3);
                String _password = c.getString(4);
                retList.add(new PCMServerInfo(_id, _srvName, _apiUrl, _fileRootUrl, _password));
            }
        }
        return retList;
    }

    public static SQLiteDatabase InitSQLiteDatabase(Application application) {
        SQLiteDatabase serverDatabase;
        serverDatabase = application.openOrCreateDatabase("PCM_SRV_DB", Application.MODE_PRIVATE, null);
        serverDatabase.execSQL(
                "CREATE TABLE IF NOT EXISTS SrvList(" +
                        "ServerID INTEGER PRIMARY KEY," +
                        "ServerName VARCHAR NOT NULL," +
                        "APIUrl VARCHAR NOT NULL," +
                        "FileRootUrl VARCHAR NOT NULL," +
                        "Password VARCHAR" +
                        ");"
        );
        return serverDatabase;
    }

    public static boolean InsertPCMServerInfo(PCMServerInfo info) {
        SQLiteDatabase db = GlobalApplication.getInstance().serverDatabase;
        ContentValues data = new ContentValues();
        data.put("ServerName", info.ServerName);
        data.put("APIUrl", info.APIUrl);
        data.put("FileRootUrl", info.FileRootUrl);
        data.put("password", info.Password);
        long retVal = db.insert("SrvList", null, data);
        return retVal != -1;
    }

}

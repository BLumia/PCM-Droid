package net.blumia.pcm.privatecloudmusic;

import android.database.Cursor;

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

}

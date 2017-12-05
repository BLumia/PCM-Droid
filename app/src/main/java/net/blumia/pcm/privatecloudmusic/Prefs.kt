package net.blumia.pcm.privatecloudmusic

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by wzc78 on 2017/12/2.
 */
class Prefs(context: Context) {

    companion object {
        const val PREFS_FILENAME = "net.blumia.pcm.privatecloudmusic.prefs"
        const val CUR_SRV_INDEX = "current_selected_server"
        const val CUR_WEB_FILE_PATH = "current_web_file_path"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var curSrvIndex: Int
        get() = prefs.getInt(CUR_SRV_INDEX, 0)
        set(value) = prefs.edit().putInt(CUR_SRV_INDEX, value).apply()
    var curWebFilePath: String
        get() = prefs.getString(CUR_WEB_FILE_PATH, "")
        set(value) = prefs.edit().putString(CUR_WEB_FILE_PATH, value).apply()
}
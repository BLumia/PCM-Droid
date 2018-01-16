package net.blumia.pcm.privatecloudmusic

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by wzc78 on 2017/12/2.
 */
class Prefs(context: Context) {

    companion object {
        const val PREFS_FILENAME = "net.blumia.pcm.privatecloudmusic.prefs"
        const val CUR_SRV_INDEX = "current_selected_server"
        const val CUR_WEB_FILE_ROOT_PATH = "current_web_file_root_path"
        const val CUR_WEB_FILE_RELATIVE_PATH = "current_web_file_relative_path"
        const val PLAY_LIST = "playlist"
        const val CUR_SONG_INDEX = "current_playing_song"
        const val GUI_LOOP_BTN = "gui_loop_btn"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var curSrvIndex: Int
        get() = prefs.getInt(CUR_SRV_INDEX, 0)
        set(value) = prefs.edit().putInt(CUR_SRV_INDEX, value).apply()
    var curWebFileRootPath: String
        get() = prefs.getString(CUR_WEB_FILE_ROOT_PATH, "")
        set(value) = prefs.edit().putString(CUR_WEB_FILE_ROOT_PATH, value).apply()
    var curWebFileRelativePath: String
        get() = prefs.getString(CUR_WEB_FILE_RELATIVE_PATH, "")
        set(value) = prefs.edit().putString(CUR_WEB_FILE_RELATIVE_PATH, value).apply()
    private var _playlist: String
        get() = prefs.getString(PLAY_LIST, "") // default value issue...
        set(value) = prefs.edit().putString(PLAY_LIST, value).apply()
    var playlist: ArrayList<MusicItem>
        get() {
            // cache the value using `field = ?`?
            return Gson().fromJson(_playlist, object : TypeToken<List<MusicItem>>(){}.type)
        }
        set(value) {
            _playlist = Gson().toJson(value)
        }
    var curSongIndex: Int
        get() = prefs.getInt(CUR_SONG_INDEX, 0)
        set(value) = prefs.edit().putInt(CUR_SONG_INDEX, value).apply()
    var guiLoopBtn: Boolean
        get() = prefs.getBoolean(GUI_LOOP_BTN, false)
        set(value) = prefs.edit().putBoolean(GUI_LOOP_BTN, value).apply()
}
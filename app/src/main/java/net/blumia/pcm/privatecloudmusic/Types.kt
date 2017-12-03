package net.blumia.pcm.privatecloudmusic

import java.net.URL

/**
 * Created by wzc78 on 2017/11/22.
 */
enum class ServerType {
    SRV_PCM, ADD_SRV
}

enum class PlaylistType {
    FOLDER, PLAYLIST
}

enum class MusicItemType {
    MUSIC, SUB_FOLDER, SUB_PLAYLIST
}

data class ServerItem (
        var index : Int,
        var serverName : String,
        var apiUrl : URL,
        var fileRootUrl : URL,
        var password : String,
        var type : ServerType = ServerType.SRV_PCM
)

data class PlaylistItem (
        var name : String,
        var folderPath : String, // should be "xxx/xxx/", leave blank if type != folder
        var type : PlaylistType = PlaylistType.FOLDER
)

data class MusicItem (
        var name : String,
        var filePathAndName : String, // only a filename is ok if using relative path
        var modifyTime : Long,
        var fileSize : Long,
        var useRelativePath : Boolean = true,
        var type : MusicItemType
)

class ServerAnkoItem (
        val index : Int,
        val serverName : String,
        val apiUrl : String,
        val fileRootUrl : String,
        val password : String
)
package net.blumia.pcm.privatecloudmusic;

/**
 * Created by wzc78 on 2017/11/1.
 */

public class MusicListInfo {
    public String displayName = "Placeholder Text";
    public String folderPath = null; // if null, treat MusicListInfo as playlist instead of FolderList
    public String apiUrl = null;

    public MusicListInfo(String apiUrl, String folderPath) {
        this.apiUrl = apiUrl;
        this.folderPath = folderPath;
        this.displayName = folderPath;
    }

    public MusicListInfo(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}

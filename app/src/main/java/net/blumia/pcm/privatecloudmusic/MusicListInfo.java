package net.blumia.pcm.privatecloudmusic;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
        try {
            this.displayName = URLDecoder.decode(folderPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public MusicListInfo(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}

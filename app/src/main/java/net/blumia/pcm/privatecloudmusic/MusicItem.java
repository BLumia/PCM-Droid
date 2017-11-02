package net.blumia.pcm.privatecloudmusic;

/**
 * Created by wzc78 on 2017/11/2.
 */

public class MusicItem {

    public String musicUrl;
    public String fileName;
    public long modifiedTime;
    public long fileSize;

    public MusicItem(String url, String songName, long modifiedTime, long fileSize) {
        musicUrl = url;
        fileName = songName;
        this.modifiedTime = modifiedTime;
        this.fileSize = fileSize;
    }
}

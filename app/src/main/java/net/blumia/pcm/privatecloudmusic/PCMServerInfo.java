package net.blumia.pcm.privatecloudmusic;

/**
 * Created by wzc78 on 2017/10/25.
 */

public class PCMServerInfo {

    public int ServerID;
    public String ServerName;
    public String APIUrl;
    public String FileRootUrl;
    public String Password;

    public PCMServerInfo(int id, String name, String url, String webRoot, String pass) {
        ServerID = id;
        ServerName = name;
        APIUrl = url;
        FileRootUrl = webRoot;
        Password = pass;
    }

}

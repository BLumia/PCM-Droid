package net.blumia.pcm.privatecloudmusic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnOpenDrawer;
    Button btnServerPopupMenu;
    DrawerLayout drawerSrvAndFolderList;
    ListView lvSongList;
    ListView lvFolderList;
    ListView lvServerIconList;

    private static final String TAG = "SQLite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenDrawer = (Button) findViewById(R.id.btn_openDrawer);
        btnServerPopupMenu = (Button) findViewById(R.id.btn_serverPopupMenu);
        drawerSrvAndFolderList = (DrawerLayout) findViewById(R.id.drawer_srvAndFolderList);
        lvServerIconList = (ListView) findViewById(R.id.lv_server_icon_list);
        lvSongList = (ListView) findViewById(R.id.lv_song_list);
        lvFolderList = (ListView) findViewById(R.id.lv_folder_list);

        List<PCMServerInfo> pcmSrvList = SQLiteUtils.GetServerInfoList();
        Log.d(TAG, "onCreate: Rows in database " + pcmSrvList.size());

        if (pcmSrvList.size() == 0) {
            Intent intent = new Intent(MainActivity.this, AddServerActivity.class);
            startActivity(intent);
        }

        final ArrayList<PCMServerInfo> infoList = (ArrayList<PCMServerInfo>)pcmSrvList;
        final ServerIconListAdapter serverIconListAdapter = new ServerIconListAdapter(this, infoList);
        lvServerIconList.setAdapter(serverIconListAdapter);
        infoList.add(new PCMServerInfo(1, "asd", null, null, null));
        infoList.add(new PCMServerInfo(2, "asd", null, null, null));

        final ArrayList<MusicListInfo> pcmFolderList = new ArrayList<>();
        final FolderListAdapter folderListAdapter = new FolderListAdapter(this, pcmFolderList);
        lvFolderList.setAdapter(folderListAdapter);
        pcmFolderList.add(new MusicListInfo("","folderA"));
        pcmFolderList.add(new MusicListInfo("","folderB"));

        final ArrayList<MusicItem> playlist = new ArrayList<>();
        final PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playlist);
        lvSongList.setAdapter(playlistAdapter);
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName2", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName3", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName4", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName5", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName6", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName7", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName8", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName9", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName0", 123123123, 123123));
        playlist.add(new MusicItem("https://pcm.blumia.cn/a/a.mp3", "SongName1", 123123123, 123123));
        lvServerIconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // test
                Log.d(TAG, "icon listview onClick: " + position);
                serverIconListAdapter.setSelectedIndex(position);
                PCMServerInfo info = serverIconListAdapter.getItem(position);
                serverIconListAdapter.notifyDataSetChanged();

                final ArrayList<MusicListInfo> infoList = new ArrayList<>();
                infoList.add(new MusicListInfo("","folderC"));
                infoList.add(new MusicListInfo("","folderD"));
                requestFolderList(info, folderListAdapter);
            }
        });

        lvFolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        btnOpenDrawer.setOnClickListener(this);
        btnServerPopupMenu.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_openDrawer:
                drawerSrvAndFolderList.openDrawer(Gravity.LEFT);
                break;
            case R.id.btn_serverPopupMenu:
                PopupMenu popup = new PopupMenu(this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.server_options, popup.getMenu());
                popup.setOnMenuItemClickListener(new ServerOptionsPopupMenu());
                popup.show();
                break;
        }
    }

    class ServerOptionsPopupMenu implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch  (item.getItemId()) {
                case R.id.mi_update_server_info:
                    Toast.makeText(getApplicationContext(), "R.id.mi_update_server_info", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.mi_delete_server:
                    Toast.makeText(getApplicationContext(), "R.id.mi_delete_server", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        }
    }

    public int requestFolderList(final PCMServerInfo info, final FolderListAdapter adapter) {
        //info.APIUrl;
        OkHttpClient httpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("do", "getfilelist")
                .build();
        Request request = new Request.Builder()
                .url("https://pcm.blumia.cn/api.php")
                .post(formBody)
                .build();
        final ArrayList<MusicListInfo> folderList = new ArrayList<>();

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                ArrayList<MusicListInfo> info = (ArrayList<MusicListInfo>)msg.obj;
                freshFolderList(folderList, adapter);
            }
        };

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //JSONParser parser = new JSONParser();

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject result = json.getJSONObject("result");
                    JSONObject data = result.getJSONObject("data");
                    JSONArray folders = data.getJSONArray("subFolderList");
                    for(int i = 0; i < folders.length(); i++) {
                        String folderName = (String)folders.get(i);
                        Log.d(TAG, folderName);
                        folderList.add(new MusicListInfo(info.APIUrl, folderName));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.obj = folderList;
                handler.sendMessage(msg);
            }
        });
        return 0;
    }

    public int freshFolderList(ArrayList<MusicListInfo> folderList, final FolderListAdapter adapter) {

        adapter.setInfoArrayList(folderList);
        adapter.notifyDataSetChanged();

        return 0;
    }
}

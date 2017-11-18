package net.blumia.pcm.privatecloudmusic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
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
    TextView tvCurSrvName;
    ListView lvSongList;
    ListView lvFolderList;
    ListView lvServerIconList;
    PCMServerInfo curSelectedSrvInfo;
    PlayerService.PlayerServiceIBinder mPlayerServiceIBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerServiceIBinder = (PlayerService.PlayerServiceIBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // triggered if boooooooooooooooooooooooooooooooom
            // this will NOT triggered if user unbind manually.
        }
    };

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
        tvCurSrvName = (TextView) findViewById(R.id.tv_cur_server_name);

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

        final ArrayList<MusicItem> playlist = new ArrayList<>();
        final PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playlist);
        lvSongList.setAdapter(playlistAdapter);
        lvServerIconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // test
                Log.d(TAG, "icon listview onClick: " + position);
                serverIconListAdapter.setSelectedIndex(position);
                curSelectedSrvInfo = serverIconListAdapter.getItem(position);
                tvCurSrvName.setText(curSelectedSrvInfo.ServerName);
                serverIconListAdapter.notifyDataSetChanged();

                requestFolderList(curSelectedSrvInfo, folderListAdapter);
            }
        });

        lvFolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicListInfo info = folderListAdapter.getItem(position);
                folderListAdapter.notifyDataSetChanged();

                requestFileList(info, playlistAdapter);
            }
        });

        lvSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // should not start service here.
                mPlayerServiceIBinder.play();
            }
        });

        btnOpenDrawer.setOnClickListener(this);
        btnServerPopupMenu.setOnClickListener(this);

        //Intent serviceIntent = new Intent("android.intent.action.PLAY");
        //serviceIntent.setPackage("net.blumia.pcm.privatecloudmusic");
        //startService(serviceIntent);
        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent); // start it first to avoid service exit when this activity destory.
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
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

    public int requestFileList(final MusicListInfo info, final PlaylistAdapter adapter) {

        OkHttpClient httpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("do", "getfilelist")
                .add("folder", "Test")
                .build();
        Request request = new Request.Builder()
                .url("https://pcm.blumia.cn/api.php")
                .post(formBody)
                .build();
        final ArrayList<MusicItem> fileList = new ArrayList<>();
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //ArrayList<MusicItem> info = (ArrayList<MusicItem>)msg.obj;
                freshFileList(fileList, adapter);
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
                    JSONArray files = data.getJSONArray("musicList");
                    for(int i = 0; i < folders.length(); i++) {
                        String folderName = (String)folders.get(i);
                        Log.d(TAG, folderName);
                        //fileList.add(new MusicItem(info.APIUrl, folderName));
                    }
                    for(int i = 0; i < files.length(); i++) {
                        JSONObject fileItem = files.getJSONObject(i);
                        MusicItem mi = new MusicItem("https://pcm.blumia.cn/Test/" + /*"filePath" +*/ fileItem.getString("fileName"),
                                URLDecoder.decode(fileItem.getString("fileName"), "UTF-8"),
                                fileItem.getLong("modifiedTime"),
                                fileItem.getLong("fileSize"));
                        fileList.add(mi);
                        Log.d(TAG, "music url: " + mi.musicUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Message msg = new Message();
                msg.obj = fileList;
                handler.sendMessage(msg);
            }
        });
        return 0;
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
                //ArrayList<MusicListInfo> info = (ArrayList<MusicListInfo>)msg.obj;
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

    public int freshFileList(ArrayList<MusicItem> fileList, final PlaylistAdapter adapter) {

        adapter.setInfoArrayList(fileList);
        adapter.notifyDataSetChanged();

        return 0;
    }

    public int freshFolderList(ArrayList<MusicListInfo> folderList, final FolderListAdapter adapter) {

        adapter.setInfoArrayList(folderList);
        adapter.notifyDataSetChanged();

        return 0;
    }
}

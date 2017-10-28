package net.blumia.pcm.privatecloudmusic;

import android.app.Application;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnOpenDrawer;
    DrawerLayout drawerSrvAndFolderList;
    ListView lvSongList;
    ListView lvServerIconList;

    public static ArrayAdapter<String> adapter;

    private static final String TAG = "SQLite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenDrawer = (Button) findViewById(R.id.btn_openDrawer);
        drawerSrvAndFolderList = (DrawerLayout) findViewById(R.id.drawer_srvAndFolderList);
        lvServerIconList = (ListView) findViewById(R.id.lv_server_icon_list);
        lvSongList = (ListView) findViewById(R.id.lv_song_list);

        List<PCMServerInfo> pcmSrvList = SQLiteUtils.GetServerInfoList();
        Log.d(TAG, "onCreate: Rows in database " + pcmSrvList.size());

        if (pcmSrvList.size() == 0) {
            Intent intent = new Intent(MainActivity.this, AddServerActivity.class);
            startActivity(intent);
        }

        ArrayList<PCMServerInfo> infoList = (ArrayList<PCMServerInfo>)pcmSrvList;
        lvServerIconList.setAdapter(new ServerIconListAdapter(this, infoList));
        infoList.add(new PCMServerInfo(1, "asd", null, null, null));
        infoList.add(new PCMServerInfo(2, "asd", null, null, null));

        btnOpenDrawer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_openDrawer:
                drawerSrvAndFolderList.openDrawer(Gravity.LEFT);
                break;
        }
    }
}

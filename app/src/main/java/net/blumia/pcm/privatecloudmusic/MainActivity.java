package net.blumia.pcm.privatecloudmusic;

import android.app.Application;
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

    public static ArrayAdapter<String> adapter;

    private static final String TAG = "SQLite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenDrawer = (Button) findViewById(R.id.btn_openDrawer);
        drawerSrvAndFolderList = (DrawerLayout) findViewById(R.id.drawer_srvAndFolderList);
        lvSongList = (ListView) findViewById(R.id.lv_song_list);

        List<PCMServerInfo> pcmSrvList = SQLiteUtils.GetServerInfoList();
        Log.d(TAG, "onCreate: Rows in database " + pcmSrvList.size());

        ArrayList<String> myStringArray1 = new ArrayList<String>();
        myStringArray1.add("Fuck this shit.mp3");
        adapter = new ArrayAdapter<String>(this, R.layout.drawer_server_icon_item);
        lvSongList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        btnOpenDrawer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_openDrawer:
                drawerSrvAndFolderList.openDrawer(Gravity.LEFT);
        }
    }
}

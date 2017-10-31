package net.blumia.pcm.privatecloudmusic;

import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnOpenDrawer;
    Button btnServerPopupMenu;
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
        btnServerPopupMenu = (Button) findViewById(R.id.btn_serverPopupMenu);
        drawerSrvAndFolderList = (DrawerLayout) findViewById(R.id.drawer_srvAndFolderList);
        lvServerIconList = (ListView) findViewById(R.id.lv_server_icon_list);
        lvSongList = (ListView) findViewById(R.id.lv_song_list);

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

        lvServerIconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // test
                Log.d(TAG, "icon listview onClick: " + position);
                serverIconListAdapter.setSelectedIndex(position);
                serverIconListAdapter.notifyDataSetChanged();
                //ServerIconListAdapter.ViewHolder holder = (ServerIconListAdapter.ViewHolder) view.getTag();
                //holder.mImageView.setBackgroundColor(Color.BLACK);
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
}

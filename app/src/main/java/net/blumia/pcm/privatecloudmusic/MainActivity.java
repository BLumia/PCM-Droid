package net.blumia.pcm.privatecloudmusic;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnOpenDrawer;
    DrawerLayout drawerSrvAndFolderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenDrawer = (Button) findViewById(R.id.btn_openDrawer);
        drawerSrvAndFolderList = (DrawerLayout) findViewById(R.id.drawer_srvAndFolderList);

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

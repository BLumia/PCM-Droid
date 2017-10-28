package net.blumia.pcm.privatecloudmusic;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;

public class AddServerActivity extends AppCompatActivity implements View.OnClickListener{

    FloatingActionButton mAddServerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        mAddServerButton = (FloatingActionButton) findViewById(R.id.btn_add_server);
        mAddServerButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_server:
                // do insert
                boolean insertResult = SQLiteUtils.InsertPCMServerInfo(new PCMServerInfo("TestInsert","","",""));
                if (insertResult) {
                    finish();
                    // call main activity to refresh?
                } else {
                    // alert for insert failed
                }
                break;
        }
    }

}

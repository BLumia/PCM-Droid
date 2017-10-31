package net.blumia.pcm.privatecloudmusic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartupScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_screen);

        timerThread.start();

    }

    Thread timerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Intent intent = new Intent(StartupScreen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    });
}

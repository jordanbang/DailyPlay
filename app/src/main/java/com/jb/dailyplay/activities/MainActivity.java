package com.jb.dailyplay.activities;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jb.dailyplay.R;
import com.jb.dailyplay.GooglePlayMusicApi.impl.GoogleMusicAPI;
import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.managers.DailyMusicManager;

import java.io.File;
import java.util.Collection;


public class MainActivity extends Activity {
    private DailyMusicManager mDailyMusicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        music();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void music() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String password = "bangiajobigbang1";
                String username = "jordan.bangia@gmail.com";
                mDailyMusicManager = new DailyMusicManager();
                mDailyMusicManager.login(username, password);
                mDailyMusicManager.getRandomSongs(5, getBaseContext());
            }
        });
        thread.start();
    }
}

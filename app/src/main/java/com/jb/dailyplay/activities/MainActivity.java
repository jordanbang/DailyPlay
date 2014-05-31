package com.jb.dailyplay.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.jb.dailyplay.R;
import com.jb.dailyplay.impl.GoogleMusicAPI;
import com.jb.dailyplay.model.Song;

import java.io.File;
import java.util.Collection;


public class MainActivity extends Activity {

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
                String password = "***REMOVED***";
                String username = "george.doe231@gmail.com";
                GoogleMusicAPI api = new GoogleMusicAPI();
                try {
                    api.login(username, password);
//                    Collection<Song> songs = api.getAllSongs();

                    Song song = new Song();
                    song.setId("172db441-7711-360b-8faa-cc2e88dfb965");
                    File file = api.downloadSong(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

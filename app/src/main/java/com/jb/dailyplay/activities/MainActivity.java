package com.jb.dailyplay.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
                String password = "bangiajobigbang1";
                String username = "jordan.bangia@gmail.com";
                GoogleMusicAPI api = new GoogleMusicAPI();
                try {
                    api.login(username, password);
                    Collection<Song> songs = api.getAllSongs();

//                    MediaPlayer mp = new MediaPlayer();
//                    mp.setDataSource(api.getSongURL(songs.iterator().next()).toString());
//                    mp.prepare();
//                    mp.start();
                    Song temp = null;
                    int count = 0;
                    for (Song song : songs) {
                        count++;
                        if (count == 20) {
                            temp = song;
                        }

                    }
                    final File file = api.downloadSong(temp, getBaseContext());
                    MediaScannerConnection.scanFile(getBaseContext(), new String[] {file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String s, Uri uri) {
                            Log.i("External Storage", "Scanned " + s);
                            Log.i("External Storage", "Uri " + uri);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

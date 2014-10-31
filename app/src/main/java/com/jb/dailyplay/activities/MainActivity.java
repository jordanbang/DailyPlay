package com.jb.dailyplay.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.jb.dailyplay.R;
import com.jb.dailyplay.adapters.SongListAdapter;
import com.jb.dailyplay.alarmreceiver.DailyPlayAlarmReceiver;
import com.jb.dailyplay.listeners.GetDownloadedSongListListener;
import com.jb.dailyplay.listeners.SongListOnItemClickListener;
import com.jb.dailyplay.managers.DailyPlayMusicManager;
import com.jb.dailyplay.managers.LoginManager;
import com.jb.dailyplay.models.Song;
import com.jb.dailyplay.tasks.GetDownloadedSongListTask;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.dailyplay.utils.LogUtils;

import java.util.ArrayList;


public class MainActivity extends Activity {
    private ListView mListView;
    private DailyPlayAlarmReceiver mAlarm = new DailyPlayAlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DailyPlaySharedPrefUtils.init(getApplication());

        mListView = (ListView) findViewById(R.id.song_list);
        mListView.setOnItemClickListener(new SongListOnItemClickListener(this));
        updateListView();

        Button button = (Button) findViewById(R.id.test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });


        mAlarm.setAlarm(this);
        LogUtils.appendLog("App boot @ " + System.currentTimeMillis());
        LoginManager.getManager(this).promptForUserInformationIfNoneExists();
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
        switch(id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_login:
                LoginManager.getManager(this).promptForNewUserInformation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListView() {
        GetDownloadedSongListListener listener = new GetDownloadedSongListListener() {
            @Override
            public void onComplete(ArrayList<Song> songs) {
                SongListAdapter adapter;
                if (mListView.getAdapter() == null) {
                    adapter = new SongListAdapter(MainActivity.this, songs);
                    mListView.setAdapter(adapter);
                } else {
                    adapter = (SongListAdapter) mListView.getAdapter();
                }
                adapter.notifyDataSetChanged(songs);
            }
        };
        new GetDownloadedSongListTask().execute(listener);
    }

    private void test() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DailyPlayMusicManager dailyPlayMusicManager = DailyPlayMusicManager.getInstance();
                try {
                    dailyPlayMusicManager.login();
                    dailyPlayMusicManager.test(MainActivity.this);
                } catch (Exception e) {
                    Log.e("DailyPlay - test error", e.toString());
                    LogUtils.appendLog(e);
                }
            }
        });
        thread.start();
    }
}



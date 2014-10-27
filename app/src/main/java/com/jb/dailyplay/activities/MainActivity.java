package com.jb.dailyplay.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jb.dailyplay.R;
import com.jb.dailyplay.adapters.SongListAdapter;
import com.jb.dailyplay.alarmreceiver.DailyPlayAlarmReceiver;
import com.jb.dailyplay.listeners.CheckUserCredentialsListener;
import com.jb.dailyplay.managers.DailyPlayMusicManager;
import com.jb.dailyplay.managers.LoginManager;
import com.jb.dailyplay.models.SongFile;
import com.jb.dailyplay.tasks.CheckUserCredentialsTask;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.dailyplay.utils.LogUtils;
import com.jb.dailyplay.utils.SharedPref;
import com.jb.dailyplay.utils.StringUtils;

import java.util.Collection;


public class MainActivity extends Activity {
    private TextView mUpdateTextView;
    private ListView mListView;
    private DailyPlayAlarmReceiver mAlarm = new DailyPlayAlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DailyPlaySharedPrefUtils.init(getApplication());
        mUpdateTextView = (TextView) findViewById(R.id.update);

        mListView = (ListView) findViewById(R.id.song_list);
        updateListView();
        mAlarm.setAlarm(this);
        LogUtils.appendLog("App boot @ " + System.currentTimeMillis());

        Button button = (Button) findViewById(R.id.test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
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
        Collection<SongFile> downloadedSongs = DailyPlayMusicManager.getInstance().getDownloadedSongs();
        if (downloadedSongs == null) {
            return;
        }

        if (mListView.getAdapter() == null) {
            mListView.setAdapter(new SongListAdapter(this, downloadedSongs));
        } else {
            SongListAdapter adapter = (SongListAdapter) mListView.getAdapter();
            adapter.notifyDataSetChanged(downloadedSongs);
        }
    }

    private void test() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DailyPlayMusicManager dailyPlayMusicManager = DailyPlayMusicManager.getInstance();
                try {
                    dailyPlayMusicManager.login();
                    dailyPlayMusicManager.test();
                } catch (Exception e) {
                    Log.e("DailyPlay - test error", e.toString());
                    LogUtils.appendLog(e);
                }
            }
        });
        thread.start();
    }
}



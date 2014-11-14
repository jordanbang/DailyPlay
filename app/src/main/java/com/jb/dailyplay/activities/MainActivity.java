package com.jb.dailyplay.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
            //TODO: Remove this test item
            case R.id.test:
                test();
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

    //TODO: Remove this function
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
//        sendNotification("Hi", "Testing");
    }

    private void sendNotification(String title, String message) {
        if (!DailyPlaySharedPrefUtils.shouldShowNotifications()) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent result = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(result);
        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}



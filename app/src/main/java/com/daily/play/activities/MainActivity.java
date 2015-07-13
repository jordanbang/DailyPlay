package com.daily.play.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.daily.play.R;
import com.daily.play.adapters.SongListAdapter;
import com.daily.play.alarmreceiver.DailyPlayAlarmReceiver;
import com.daily.play.api.models.Track;
import com.daily.play.fragments.InformationDialogFragment;
import com.daily.play.fragments.LoginDialogFragment;
import com.daily.play.listeners.GetDownloadedSongListListener;
import com.daily.play.listeners.SongListOnItemClickListener;
import com.daily.play.managers.DailyPlayMusicManager;
import com.daily.play.tasks.GetDownloadedSongListTask;
import com.daily.play.utils.DailyPlaySharedPrefUtils;
import com.daily.play.utils.LogUtils;
import com.daily.play.utils.LoginUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


public class MainActivity extends Activity implements
        LoginDialogFragment.OnContinueSelectedListener, InformationDialogFragment.OnLoginClickListener {
    private ListView mListView;
    private DailyPlayAlarmReceiver mAlarm = new DailyPlayAlarmReceiver();

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DailyPlaySharedPrefUtils.init(getApplication());

        mListView = (ListView) findViewById(R.id.song_list);
        mListView.setOnItemClickListener(new SongListOnItemClickListener(this));
        mListView.setEmptyView(findViewById(R.id.empty_list));
        updateListView();

        mAlarm.setAlarm(this);
        LogUtils.appendLog("App boot @ " + System.currentTimeMillis());

        if(DailyPlaySharedPrefUtils.isFirstOpen()) {
            FragmentManager fm = this.getFragmentManager();
            InformationDialogFragment infoDialogFragment = InformationDialogFragment.newInstance(true);
            infoDialogFragment.show(fm, "info_dialog_fragment");
        } else if (LoginUtils.isLoggedIn()){
            FragmentManager fm = this.getFragmentManager();
            LoginDialogFragment loginDialogFragment = LoginDialogFragment.newInstance();
            loginDialogFragment.show(fm, "login_dialog_fragment");
        }
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
            case R.id.action_logout:
                DailyPlayMusicManager.getInstance().logout();
                recreate();
                return true;
            case R.id.action_info:
                FragmentManager fm = this.getFragmentManager();
                InformationDialogFragment infoDialogFragment = InformationDialogFragment.newInstance(false);
                infoDialogFragment.show(fm, "info_dialog_fragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListView() {
        GetDownloadedSongListListener listener = new GetDownloadedSongListListener() {
            @Override
            public void onComplete(Collection<Track> songs) {
                SongListAdapter adapter;
                if (mListView.getAdapter() == null) {
                    adapter = new SongListAdapter(MainActivity.this, new ArrayList<>(songs));
                    mListView.setAdapter(adapter);
                } else {
                    adapter = (SongListAdapter) mListView.getAdapter();
                }
                adapter.notifyDataSetChanged(new ArrayList<>(songs));
                if (adapter.isEmpty()) {
                    TextView emptyView = (TextView) findViewById(R.id.empty_list);
                    emptyView.setText(R.string.empty_list);
                }
            }
        };
        new GetDownloadedSongListTask().execute(listener);
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

    private void startLoginFlow() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT){
            if (resultCode == RESULT_OK) {
                Log.i("DailyPlay", "getting token");
                final String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        String token = "";
                        try {
                            token = GoogleAuthUtil.getToken(MainActivity.this, email, "sj");
                            Log.i("DailyPlay", "Got the token");
                            DailyPlayMusicManager.getInstance().login(token);
                        } catch (UserRecoverableAuthException e) {
                            e.printStackTrace();
                            MainActivity.this.handleException(e);
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                            MainActivity.this.handleException(e);
                        } catch (IOException e) {
                            MainActivity.this.handleException(e);
                            e.printStackTrace();
                        }
                        return token;
                    }
                }.execute();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    int statusCode = ((GooglePlayServicesAvailabilityException) e).getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, MainActivity.this, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    Intent intent = ((UserRecoverableAuthException) e).getIntent();
                    startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    @Override
    public void onContinueSelected() {
        startLoginFlow();
    }

    @Override
    public void onLoginClick() {
        startLoginFlow();
    }
}





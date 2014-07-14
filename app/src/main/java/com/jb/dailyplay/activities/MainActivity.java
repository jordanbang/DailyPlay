package com.jb.dailyplay.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.jb.dailyplay.R;
import com.jb.dailyplay.adapters.SongListAdapter;
import com.jb.dailyplay.alarmreceiver.DailyPlayAlarmReceiver;
import com.jb.dailyplay.managers.DailyMusicManager;
import com.jb.dailyplay.models.SongFile;
import com.jb.dailyplay.utils.ConnectionUtils;
import com.jb.dailyplay.utils.LogUtils;
import com.jb.dailyplay.utils.SharedPref;

import java.util.Collection;


public class MainActivity extends Activity {
    private static final int PICK_ACCOUNT_REQUEST = 0;
    private static final String mScope = "";
    private static final String TAG = "MainActivity";
    private String mAccountName;
    private String mAuthToken;
    private TextView mUpdateTextView;
    private ListView mListView;
    private DailyPlayAlarmReceiver mAlarm = new DailyPlayAlarmReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPref.initSharedPref(this, getResources().getString(R.string.app_name));
        mUpdateTextView = (TextView) findViewById(R.id.update);

        mListView = (ListView) findViewById(R.id.song_list);
        updateListView();
        mAlarm.setAlarm(this);
        LogUtils.appendLog("App boot @ " + System.currentTimeMillis());
    }

    public void chooseGoogleAccount() {
        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, "Select account with Google Play Music", null, null, null);
        startActivityForResult(googlePicker, PICK_ACCOUNT_REQUEST);
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
//            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
//            try {
//                mAuthToken = GoogleAuthUtil.getToken(this, mAccountName, null);
//            } catch (Exception e) {
//                Log.e(TAG, "getting auth token failed");
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private void getDailyPlayList() {
        if (ConnectionUtils.isConnectedWifi(this)) {

        } else {
            mUpdateTextView.setText("Your device is not connected to Wi-fi.  Please connect to wi-fi to continue using DailyPlay.");
        }
    }

    private void updateListView() {
        Collection<SongFile> downloadedSongs = DailyMusicManager.getInstance().getDownloadedSongs();
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

    public class GetDailyPlayMusicTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUpdateTextView.setText("Starting to get DailyPlay list");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String password = "GgfoDPxNSVH0Aqwx8MIt";
            String username = "george.doe231@gmail.com";
            DailyMusicManager.getInstance().login(username, password);
            publishProgress("Completed login");
//            mDailyMusicManager.getDailyPlayMusic(5, getBaseContext(), this);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mUpdateTextView.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateListView();
        }
    }
}

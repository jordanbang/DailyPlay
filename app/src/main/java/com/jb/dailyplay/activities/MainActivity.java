package com.jb.dailyplay.activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.jb.dailyplay.R;
import com.jb.dailyplay.managers.DailyMusicManager;


public class MainActivity extends Activity {
    private DailyMusicManager mDailyMusicManager;
    private static final int PICK_ACCOUNT_REQUEST = 0;
    private static final String mScope = "";
    private static final String TAG = "MainActivity";
    private String mAccountName;
    private String mAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chooseGoogleAccount();

//        music();
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

    public void music() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String password = "***REMOVED***";
                String username = "george.doe231@gmail.com";
                mDailyMusicManager = new DailyMusicManager();
                mDailyMusicManager.login(username, password);
                mDailyMusicManager.getRandomSongs(5, getBaseContext());
            }
        });
        thread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            mAccountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            try {
                mAuthToken = GoogleAuthUtil.getToken(this, mAccountName, null);
            } catch (Exception e) {
                Log.e(TAG, "getting auth token failed");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

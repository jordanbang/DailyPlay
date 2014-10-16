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
import com.jb.dailyplay.managers.DailyMusicManager;
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
        promptForUserInformation(false);
    }

    private void promptForUserInformation(boolean isReprompt) {
        if (DailyPlaySharedPrefUtils.doesUserInformationExist()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = !isReprompt ? "Login" : "Login Again";
        builder.setTitle(title);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.login_dialog, null);
        final EditText usernameEditText = (EditText) view.findViewById(R.id.login_email);
        final EditText passwordEditText = (EditText) view.findViewById(R.id.login_password);


        builder.setView(view);
        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (StringUtils.isEmptyString(username) || !StringUtils.isValidEmail(username)) {
                    usernameEditText.setError("Please enter valid email address.");
                } else if (StringUtils.isEmptyString(password)) {
                    passwordEditText.setError("Please enter your password.");
                } else {
                    dialogInterface.dismiss();
                    SharedPref.setString(DailyPlaySharedPrefUtils.USERNAME, username);
                    SharedPref.setString(DailyPlaySharedPrefUtils.PASSWORD, password);
                    CheckUserCredentialsListener listener = new CheckUserCredentialsListener() {
                        @Override
                        public void onComplete(boolean isSuccessful) {
                            if (isSuccessful) {
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT);
                            } else {
                                promptForUserInformation(true);
                            }
                        }
                    };
                    new CheckUserCredentialsTask().execute(listener);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.create().show();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void test() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                DailyMusicManager dailyMusicManager = DailyMusicManager.getInstance();
                dailyMusicManager.login();
                try {
                    dailyMusicManager.test();
                } catch (Exception e) {
                    Log.e("DailyPlay - test error", e.toString());
                    LogUtils.appendLog(e);
                }
            }
        });
        thread.start();
    }
}



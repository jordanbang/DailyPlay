package com.jb.dailyplay.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.jb.dailyplay.R;
import com.jb.dailyplay.managers.DailyMusicManager;

/**
 * Created by Jordan on 7/12/2014.
 */
public class SettingsActivity extends Activity {
    CheckBox mDownloadBySongs;
    CheckBox mDownloadByTime;
    CheckBox mShowNotifications;
    CheckBox mKeepPlayList;
    EditText mNumberOfSongs;
    EditText mTimeOfList;
    TextView mNumberOfSongsText;
    TextView mTimeOfListText;

    public static class DownloadOptions {
        public static final int SONGS = 0;
        public static final int TIME = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDownloadBySongs = (CheckBox) findViewById(R.id.download_by_songs);
        mDownloadByTime = (CheckBox) findViewById(R.id.download_by_time);
        mShowNotifications = (CheckBox) findViewById(R.id.show_notifications);
        mKeepPlayList = (CheckBox) findViewById(R.id.keep_dailyplay_lists);

        mNumberOfSongs = (EditText) findViewById(R.id.number_of_songs);
        mTimeOfList = (EditText) findViewById(R.id.length_of_playlist);

        mNumberOfSongsText = (TextView) findViewById(R.id.text_number_of_songs);
        mTimeOfListText = (TextView) findViewById(R.id.text_length_of_list);


        mDownloadBySongs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    onlyOneOptionChecked(DownloadOptions.SONGS);
                    DailyMusicManager.getInstance().saveDownloadOption(DownloadOptions.SONGS);
                }
            }
        });

        mDownloadByTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    onlyOneOptionChecked(DownloadOptions.TIME);
                    DailyMusicManager.getInstance().saveDownloadOption(DownloadOptions.SONGS);
                }
            }
        });
    }

    private void onlyOneOptionChecked(int downloadOption) {
        if (downloadOption == DownloadOptions.SONGS) {
            //downloading by number
            mDownloadByTime.setChecked(false);
            mTimeOfList.setEnabled(false);
            mTimeOfListText.setEnabled(false);
            mNumberOfSongs.setEnabled(true);
            mNumberOfSongsText.setEnabled(true);
        } else {
            //downloading by time
            mDownloadBySongs.setChecked(false);
            mNumberOfSongsText.setEnabled(false);
            mNumberOfSongs.setEnabled(false);
            mTimeOfList.setEnabled(true);
            mTimeOfListText.setEnabled(true);
        }
    }
}

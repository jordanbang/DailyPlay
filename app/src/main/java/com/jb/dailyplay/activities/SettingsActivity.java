package com.jb.dailyplay.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jb.dailyplay.R;
import com.jb.dailyplay.managers.DailyPlayMusicManager;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;

/**
 * Created by Jordan on 7/12/2014.
 */
public class SettingsActivity extends Activity {
    RadioGroup mDownloadByGroup;
    RadioButton mDownloadBySongs;
    RadioButton mDownloadByTime;
    CheckBox mShowNotifications;
    CheckBox mKeepPlayList;
    EditText mNumberOfSongs;
    EditText mTimeOfList;

    private String mNumberOfSongsString;
    private String mTimeOfListString;

    private int mDownloadOption;
    private View downloadBySongsOptions;
    private View downloadByTimeOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        downloadBySongsOptions = findViewById(R.id.by_amount_group);
        downloadByTimeOptions = findViewById(R.id.by_time_group);

        mDownloadByGroup = (RadioGroup) findViewById(R.id.radio_group);
        mDownloadByGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int radioButton) {
                switch(radioButton) {
                    case R.id.download_by_songs:
                        mDownloadOption = DailyPlayMusicManager.DownloadOptions.SONGS;
                        mNumberOfSongsString = mNumberOfSongs.getText().toString();
                        break;
                    case R.id.download_by_time:
                        mDownloadOption = DailyPlayMusicManager.DownloadOptions.TIME;
                        mTimeOfListString = mTimeOfList.getText().toString();
                        break;
                }
                setViewForDownloadOption(false);
            }
        });

        mShowNotifications = (CheckBox) findViewById(R.id.show_notifications);
        mKeepPlayList = (CheckBox) findViewById(R.id.keep_dailyplay_lists);

        mNumberOfSongs = (EditText) findViewById(R.id.number_of_songs);
        mTimeOfList = (EditText) findViewById(R.id.length_of_playlist);

        loadSavedSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveInfo();
    }

    private void saveInfo() {
        DailyPlaySharedPrefUtils.saveDownloadOption(mDownloadOption);
        String playListLengthNumber = mNumberOfSongs.getText().toString();
        String playListLengthTime = mTimeOfList.getText().toString();
        DailyPlaySharedPrefUtils.saveLengthOfPlayListNumber(playListLengthNumber);
        DailyPlaySharedPrefUtils.saveLengthOfPlayListTime(playListLengthTime);
        DailyPlaySharedPrefUtils.saveShowNotifications(mShowNotifications.isChecked());
        DailyPlaySharedPrefUtils.saveKeepPlayList(mKeepPlayList.isChecked());
    }

    private void loadSavedSettings() {
        mDownloadOption = DailyPlaySharedPrefUtils.getDownloadOption();
        setViewForDownloadOption(true);
        mShowNotifications.setChecked(DailyPlaySharedPrefUtils.getShowNotifications());
        mKeepPlayList.setChecked(DailyPlaySharedPrefUtils.getKeepPlayList());
        mNumberOfSongsString = Integer.toString(DailyPlaySharedPrefUtils.getLengthOfPlayListForDownloadOption(DailyPlayMusicManager.DownloadOptions.SONGS));
        mTimeOfListString = Integer.toString(DailyPlaySharedPrefUtils.getLengthOfPlayListForDownloadOption(DailyPlayMusicManager.DownloadOptions.TIME));
        mNumberOfSongs.setText(mNumberOfSongsString);
        mTimeOfList.setText(mTimeOfListString);
    }

    private void setViewForDownloadOption(final boolean setChecked) {
        switch(mDownloadOption) {
            case DailyPlayMusicManager.DownloadOptions.SONGS:
                downloadBySongsOptions.setVisibility(View.VISIBLE);
                downloadByTimeOptions.setVisibility(View.GONE);
                if (setChecked) {
                    RadioButton downloadBySongs = (RadioButton) findViewById(R.id.download_by_songs);
                    downloadBySongs.setChecked(true);
                }

                break;
            case DailyPlayMusicManager.DownloadOptions.TIME:
                downloadBySongsOptions.setVisibility(View.GONE);
                downloadByTimeOptions.setVisibility(View.VISIBLE);
                if (setChecked) {
                    RadioButton downloadByTime = (RadioButton) findViewById(R.id.download_by_time);
                    downloadByTime.setChecked(true);
                }
                break;
        }
    }
}


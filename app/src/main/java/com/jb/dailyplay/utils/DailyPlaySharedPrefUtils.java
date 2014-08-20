package com.jb.dailyplay.utils;

import android.content.Context;

import com.jb.dailyplay.managers.DailyMusicManager;
import com.jb.jblibs.SharedPref;
import com.jb.jblibs.StringUtils;

/**
 * Created by Jordan on 7/12/2014.
 */
public class DailyPlaySharedPrefUtils {
    public static final String INIT = "DailyPlay";

    public static final String LAST_SONG_LIST_SYNC = "last_sync";
    public static final String SONG_LIST = "song_list";
    public static final String DOWNLOADED_SONG_LIST = "downloaded_song_list";
    public static final String DOWNLOAD_OPTION = "download_option";
    public static final String NUMBER_OF_SONGS_TO_DOWNLOAD = "number_of_songs_to_download";
    public static final String TIME_OF_SONGS_TO_DOWNLOAD = "time_of_songs_to_download";
    public static final String SHOW_NOTIFICATIONS = "show_notifications";
    public static final String KEEP_PLAYLIST = "keep_playlist";

    private DailyPlaySharedPrefUtils() {
    }

    public static void init(Context context) {
        SharedPref.initSharedPref(context, INIT);
    }

    public static void saveDownloadOption(int downloadOption) {
        SharedPref.setInt(DOWNLOAD_OPTION, downloadOption);
    }

    public static int getDownloadOption() {
        return SharedPref.getInt(DOWNLOAD_OPTION, DailyMusicManager.DownloadOptions.SONGS);
    }

    public static void saveLengthOfPlayList(String playListLength) {
        if (StringUtils.isEmptyString(playListLength)) {
            return;
        }

        int downloadOption = getDownloadOption();
        switch (downloadOption) {
            case DailyMusicManager.DownloadOptions.SONGS:
                SharedPref.setInt(NUMBER_OF_SONGS_TO_DOWNLOAD, Integer.parseInt(playListLength));
                break;
            case DailyMusicManager.DownloadOptions.TIME:
                SharedPref.setInt(TIME_OF_SONGS_TO_DOWNLOAD, Integer.parseInt(playListLength));
                break;
        }
    }


    public static int getLengthOfPlayList() {
        int downloadOption = getDownloadOption();
        switch (downloadOption) {
            case DailyMusicManager.DownloadOptions.TIME:
                return SharedPref.getInt(TIME_OF_SONGS_TO_DOWNLOAD, DailyMusicManager.DEF_TIME_OF_PLAY_LIST);
            case DailyMusicManager.DownloadOptions.SONGS:
            default:
                return SharedPref.getInt(NUMBER_OF_SONGS_TO_DOWNLOAD, DailyMusicManager.DEF_NUMBER_OF_SONGS);
        }
    }

    public static void saveShowNotifications(boolean showNotifications) {
        SharedPref.setBoolean(SHOW_NOTIFICATIONS, showNotifications);
    }

    public static void saveKeepPlayList(boolean keepPlayList) {
        SharedPref.setBoolean(KEEP_PLAYLIST, keepPlayList);
    }

    public static String getDownloadedSongList() {
        return SharedPref.getString(DailyPlaySharedPrefUtils.DOWNLOADED_SONG_LIST);
    }

    public static void setDownloadedSongList(String downloadedSongList) {
        SharedPref.setString(DailyPlaySharedPrefUtils.DOWNLOADED_SONG_LIST, downloadedSongList);
    }

    public static void setLastSongListSyncTime() {
        SharedPref.setLong(LAST_SONG_LIST_SYNC, System.currentTimeMillis());
    }

    public static long getLastSongListSyncTime() {
        return SharedPref.getLong(LAST_SONG_LIST_SYNC, 0);
    }

    public static void setSongList(String songList) {
        SharedPref.setString(SONG_LIST, songList);
    }

    public static String getSongList() {
        return SharedPref.getString(SONG_LIST);
    }

    public static boolean getShowNotifications() {
        return SharedPref.getBoolean(SHOW_NOTIFICATIONS, true);
    }

    public static boolean getKeepPlayList() {
        return SharedPref.getBoolean(KEEP_PLAYLIST, true);
    }
}

package com.jb.dailyplay.managers;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jb.dailyplay.GooglePlayMusicApi.impl.GoogleMusicAPI;
import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.listeners.ProgressUpdateListener;
import com.jb.dailyplay.models.SongFile;
import com.jb.dailyplay.utils.ConnectionUtils;
import com.jb.dailyplay.utils.SharedPref;
import com.jb.dailyplay.utils.StringUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by Jordan on 6/3/2014.
 * All functions that make network calls assume that they are being run on a background thread
 */
public class DailyMusicManager {
    private final GoogleMusicAPI mApi;
    private Collection<Song> mSongList;
    private Collection<SongFile> mDownloadedFiles;
    private int mSongCount = 0;

    private static final String LAST_SONG_LIST_SYNC = "last_sync";
    private static final String SONG_LIST = "song_list";
    private static final String DOWNLOADED_SONG_LIST = "downloaded_song_list";
    private static final long ONE_WEEK = DateUtils.WEEK_IN_MILLIS;
    private static final long MEGABYTE = 1024L;
    private static final long TEN_MEGABYTES = 10*MEGABYTE;

    public DailyMusicManager() {
        mApi = new GoogleMusicAPI();
    }

    private ArrayList<Integer> getRandomNumbers(int number) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < number; i++) {
            Random rand = new Random();
            ret.add(rand.nextInt(mSongCount + 1));
        }
        return ret;
    }

    private ArrayList<Song> getSongForRandomIndices(List<Integer> randomNumbers) {
        ArrayList<Song> songs = new ArrayList<Song>();

        int count = 0;
        for (Song song : mSongList) {
            if (randomNumbers.contains(count)){
                songs.add(song);
            }
            count++;
        }
        return songs;
    }

    private void scanMediaFiles(Collection<SongFile> downloadFiles, Context context) {
        for (SongFile file : downloadFiles) {
            MediaScannerConnection.scanFile(context, new String[]{file.getFile().getPath()}, new String[]{"audio/mpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.i("External Storage", "Scanned " + s);
                    Log.i("External Storage", "Uri " + uri);
                }
            });
        }
    }

    /*Below calls are synchronous, need to be run on a background thread*/
    private void loadSongList() {
            if (songListIsOutDated()) {
                downloadSongList();
            } else {
                loadSongListFromSharedPref();
            }
    }

    private void downloadSongList() {
        try {
            mSongList = mApi.getAllSongs();
            mSongCount = mSongList.size();
            SharedPref.setLong(LAST_SONG_LIST_SYNC, System.currentTimeMillis());
            Gson gson = new Gson();
            SharedPref.setString(SONG_LIST, gson.toJson(mSongList));
        } catch (Exception e) {
            Log.e("Get All Songs error", e.getMessage());
        }
    }

    public void getDailyPlayMusic(int number, Context context, ProgressUpdateListener listener) {
        if (!ConnectionUtils.isConnectedWifi(context)) {
            updateListener("Your device is not connected to Wi-fi.  Please connect to wi-fi to continue using DailyPlay.", listener);
            return;
        }

        if (!isFreeSpace(number)) {
            updateListener("Not enough space available to download the requested number of songs", listener);
            return;
        }

        if (mSongList == null || mSongList.size() == 0) {
            updateListener("Downloading song list", listener);
            loadSongList();
            updateListener("Downloaded song list", listener);
        }
        deleteOldDailyPlayList(context);
        List<Integer> randomNumbers = getRandomNumbers(number);
        updateListener("Getting random list of songs", listener);
        Collection<Song> downloadList = getSongForRandomIndices(randomNumbers);
        try {
            updateListener("Starting to download songs", listener);
            mDownloadedFiles = mApi.downloadSongs(downloadList, context);
            updateListener("Songs downloaded", listener);
            saveDailyPlayList();
            scanMediaFiles(mDownloadedFiles, context);
        } catch (Exception e) {
            Log.e("Music Manager", e.getMessage());
            updateListener("A problem has occurred, please try again later", listener);
        }
    }

    public void login(String username, String password) {
        try {
            mApi.login(username, password);
        } catch (Exception e) {
            Log.e("Login Error", e.getMessage());
        }
    }

    private void loadSongListFromSharedPref() {
        String songListAsString = SharedPref.getString(SONG_LIST);
        if (StringUtils.isEmptyString(songListAsString)) {
            downloadSongList();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<Collection<Song>>(){}.getType();
            mSongList = gson.fromJson(songListAsString, type);
            mSongCount = mSongList.size();
        }

    }

    /*Below calls don't need to be asynchronous*/
    private boolean songListIsOutDated() {
        long currentTime = System.currentTimeMillis();
        long lastSync = SharedPref.getLong(LAST_SONG_LIST_SYNC, 0);
        return (currentTime - lastSync) > ONE_WEEK;
    }

    private boolean isFreeSpace(int numberOfSongs) {
        File musicSaveFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        long freeSpace = musicSaveFolder.getFreeSpace();
        long requiredSpace = numberOfSongs * TEN_MEGABYTES;
        return freeSpace > requiredSpace;
    }

    private void saveDailyPlayList() {
        if (mDownloadedFiles == null) {
            return;
        }

        Gson gson = new Gson();
        SharedPref.setString(DOWNLOADED_SONG_LIST, gson.toJson(mDownloadedFiles));
    }

    private void deleteOldDailyPlayList(Context context) {
        String oldDailyPlayList = SharedPref.getString(DOWNLOADED_SONG_LIST);
        if (StringUtils.isEmptyString(oldDailyPlayList)) {
            return;
        }

        Gson gson = new Gson();
        Collection<SongFile> downloadedFiles = null;
        try {
            Type type = new TypeToken<Collection<SongFile>>(){}.getType();
            downloadedFiles = gson.fromJson(oldDailyPlayList, type);
        } catch(Exception e) {
            Log.e("Music Manager", e.getMessage());
        }
        for(SongFile downloadedFile : downloadedFiles) {
            File file = downloadedFile.getFile();
            file.delete();
        }
        SharedPref.setString(DOWNLOADED_SONG_LIST, "");
        scanMediaFiles(downloadedFiles, context);
    }

    private void updateListener(String text, ProgressUpdateListener listener) {
        if (listener == null) {
            return;
        }
        Log.i("Music Manager", text);
        listener.updateProgress(text);
    }
}

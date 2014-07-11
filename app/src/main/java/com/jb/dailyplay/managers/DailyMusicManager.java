package com.jb.dailyplay.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jb.dailyplay.GooglePlayMusicApi.impl.GoogleMusicAPI;
import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.exceptions.NoSpaceException;
import com.jb.dailyplay.exceptions.NoWifiException;
import com.jb.dailyplay.models.SongFile;
import com.jb.dailyplay.utils.ConnectionUtils;
import com.jb.dailyplay.utils.SharedPref;
import com.jb.dailyplay.utils.StringUtils;
import com.noveogroup.android.log.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
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

    private static final String TAG = "DailyMusicManager";
    private static final String LAST_SONG_LIST_SYNC = "last_sync";
    private static final String SONG_LIST = "song_list";
    private static final String DOWNLOADED_SONG_LIST = "downloaded_song_list";
    private static final long ONE_WEEK = DateUtils.WEEK_IN_MILLIS;
    private static final long MEGABYTE = 1024L;

    public DailyMusicManager() {
        mApi = new GoogleMusicAPI();
    }

    private static final long TEN_MEGABYTES = 10*MEGABYTE;

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

    private void scanMediaFiles(final Collection<SongFile> downloadFiles, Context context) {
        for (SongFile file : downloadFiles) {
            Uri uri = Uri.fromFile(file.getFile());
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
            Log.i("Scanned file:" + file.getFile().getName());
        }

    }

    /*Below calls are synchronous, need to be run on a background thread*/
    private void loadSongList() throws IOException, URISyntaxException {
            if (songListIsOutDated()) {
                downloadSongList();
            } else {
                loadSongListFromSharedPref();
            }
    }

    private void downloadSongList() throws IOException, URISyntaxException {
        mSongList = mApi.getAllSongs();
        mSongCount = mSongList.size();
        SharedPref.setLong(LAST_SONG_LIST_SYNC, System.currentTimeMillis());
        Gson gson = new Gson();
        SharedPref.setString(SONG_LIST, gson.toJson(mSongList));
    }

    public void getDailyPlayMusic(int number, Context context) throws Exception {
        if (!ConnectionUtils.isConnectedWifi(context)) {
            throw(new NoWifiException());
        }

        if (!isFreeSpace(number)) {
            throw(new NoSpaceException());
        }

        if (mSongList == null || mSongList.size() == 0) {
            Log.i("Downloading song list");
            loadSongList();
            Log.i("Downloaded song list");
        }
        deleteOldDailyPlayList(context);
        List<Integer> randomNumbers = getRandomNumbers(number);
        Log.i("Getting random list of songs");
        Collection<Song> downloadList = getSongForRandomIndices(randomNumbers);

        Log.i("Starting to download songs");
        mDownloadedFiles = mApi.downloadSongs(downloadList, context);
        Log.i("Songs downloaded");
        saveDailyPlayList();
        scanMediaFiles(mDownloadedFiles, context);
    }

    public void login(String username, String password) {
        try {
            mApi.login(username, password);
        } catch (Exception e) {
            Log.e(e);
        }
    }

    private void loadSongListFromSharedPref() throws IOException, URISyntaxException {
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
        Type type = new TypeToken<Collection<SongFile>>(){}.getType();
        downloadedFiles = gson.fromJson(oldDailyPlayList, type);
        for(SongFile downloadedFile : downloadedFiles) {
            File file = downloadedFile.getFile();
            file.delete();
            Log.i("Deleting file: " + file.getName());
        }
        SharedPref.setString(DOWNLOADED_SONG_LIST, "");
        scanMediaFiles(downloadedFiles, context);
    }

    private void getDownloadedFilesFromSharedPref() {
        String oldDailyPlayList = SharedPref.getString(DOWNLOADED_SONG_LIST);
        if (StringUtils.isEmptyString(oldDailyPlayList)) {
            return;
        }

        Gson gson = new Gson();
        Collection<SongFile> downloadedFiles = null;
        Type type = new TypeToken<Collection<SongFile>>(){}.getType();
        downloadedFiles = gson.fromJson(oldDailyPlayList, type);
        mDownloadedFiles = downloadedFiles;
    }

    public Collection<SongFile> getDownloadedSongs() {
        return mDownloadedFiles;
    }
}

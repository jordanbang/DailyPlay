package com.jb.dailyplay.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jb.dailyplay.GooglePlayMusicApi.impl.GoogleMusicAPI;
import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.exceptions.NoSpaceException;
import com.jb.dailyplay.exceptions.NoWifiException;
import com.jb.dailyplay.models.SongFile;
import com.jb.dailyplay.utils.ConnectionUtils;
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.dailyplay.utils.SharedPref;
import com.jb.dailyplay.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by Jordan on 6/3/2014.
 * All functions that make network calls assume that they are being run on a background thread.
 * In general, object should be made not on the UI thread, and continued to be used not on the UI thread,
 * as there are accesses to SharePref/network calls/long loops that can stall.
 */
public class DailyPlayMusicManager {
    public static final int DEF_NUMBER_OF_SONGS = 10;
    public static final int DEF_TIME_OF_PLAY_LIST = 10;

    private static final long ONE_WEEK = DateUtils.WEEK_IN_MILLIS;
    private static final long MEGABYTE = 1024L;
    private static final long TEN_MEGABYTES = 10*MEGABYTE;

    private static DailyPlayMusicManager mInstance;
    private final GoogleMusicAPI mApi;
    private ArrayList<Song> mSongList;
    private ArrayList<SongFile> mDownloadedFiles;

    private int mSongCount = 0;

    public static class DownloadOptions {
        public static final int SONGS = 0;
        public static final int TIME = 1;
    }

    public static DailyPlayMusicManager getInstance() {
        if (mInstance == null) {
            mInstance = new DailyPlayMusicManager();
        }
        return mInstance;
    }

    private DailyPlayMusicManager() {
        mApi = new GoogleMusicAPI();
    }

    public void getDailyPlayMusic(Context context) throws Exception {
        if (!ConnectionUtils.isConnectedWifi(context)) {
            throw(new NoWifiException());
        }

        loadSongList();
        Collection<Song> downloadList = getSongList();

        if (!isFreeSpace(downloadList.size())) {
            throw(new NoSpaceException());
        }

        deleteOldDailyPlayList(context);
        Log.i("DailyPlay", "Starting to download songs");
        mDownloadedFiles = mApi.downloadSongs(downloadList, context);
        Log.i("DailyPlay", "Songs downloaded");
        saveDailyPlayList();
        scanMediaFiles(mDownloadedFiles, context);
    }

    private ArrayList<Song> getSongListForCount(int numberOfSongs) {
        if (numberOfSongs >= mSongList.size()) {
            return mSongList;
        }

        ArrayList<Song> songs = new ArrayList<Song>();
        for (int i = 0; i < numberOfSongs; i++) {
            Random rand = new Random();
            int nextSongIndex = rand.nextInt(mSongCount);
            while(songs.contains(mSongList.get(nextSongIndex))) {
                nextSongIndex = rand.nextInt(mSongCount);
            }
            songs.add(mSongList.get(nextSongIndex));
        }
        return songs;
    }

    private Collection<Song> getSongListForTime(int timeOfPlayListMin) {
        ArrayList<Song> songsToDownload = new ArrayList<Song>();
        while (timeOfPlayListMin > 0 && songsToDownload.size() != mSongList.size()) {
            Random rand = new Random();
            Song songToBeAdded = mSongList.get(rand.nextInt(mSongCount));
            while (songsToDownload.contains(songToBeAdded)) {
                songToBeAdded = mSongList.get(rand.nextInt(mSongCount));
            }
            songsToDownload.add(songToBeAdded);
            timeOfPlayListMin -= (songToBeAdded.getDurationMillis()/(60*1000));
        }
        return songsToDownload;
    }

    private void scanMediaFiles(final Collection<SongFile> downloadFiles, Context context) {
        for (SongFile file : downloadFiles) {
            Uri uri = Uri.fromFile(file.getFile());
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
            Log.i("DailyPlay - Scanned file", file.getFile().getName());
        }

    }

    private void deleteOldDailyPlayList(Context context) {
        String oldDailyPlayList = DailyPlaySharedPrefUtils.getDownloadedSongList();
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
            Log.i("DailyPlay - Deleting file",  file.getName());
        }
        DailyPlaySharedPrefUtils.setDownloadedSongList("");
        scanMediaFiles(downloadedFiles, context);
    }

    public Collection<SongFile> getDownloadedSongs() {
        return mDownloadedFiles;
    }

    private Collection<Song> getSongList() {
        int downloadOption = DailyPlaySharedPrefUtils.getDownloadOption();
        Collection<Song> songs = null;
        int playListLength = DailyPlaySharedPrefUtils.getLengthOfPlayList();
        switch(downloadOption) {
            case DownloadOptions.SONGS:
                songs = getSongListForCount(playListLength);
                break;
            case DownloadOptions.TIME:
                songs = getSongListForTime(playListLength);
                break;
            case -1:
                throw(new IllegalArgumentException());
        }
        return songs;
    }

    private void loadSongList() throws IOException, URISyntaxException {
        if (mSongList == null || mSongList.size() == 0) {
            Log.i("DailyPlay", "Downloading song list");
            if (songListIsOutDated()) {
                downloadSongList();
            } else {
                loadSongListFromSharedPref();
            }
            Log.i("DailyPlay", "Downloaded song list");
        }

    }

    private void downloadSongList() throws IOException, URISyntaxException {
        mSongList = mApi.getAllSongs();
        mSongCount = mSongList.size();
        DailyPlaySharedPrefUtils.setLastSongListSyncTime();
        Gson gson = new Gson();
        DailyPlaySharedPrefUtils.setSongList(gson.toJson(mSongList));
    }

    public void login() throws Exception {
        String username = SharedPref.getString(DailyPlaySharedPrefUtils.USERNAME);
        String password = SharedPref.getString(DailyPlaySharedPrefUtils.PASSWORD);
        login(username, password);
    }

    public void login(String username, String password) throws Exception {
        mApi.login(username, password);
    }

    private void loadSongListFromSharedPref() throws IOException, URISyntaxException {
        String songListAsString = DailyPlaySharedPrefUtils.getSongList();
        if (StringUtils.isEmptyString(songListAsString)) {
            downloadSongList();
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<Collection<Song>>(){}.getType();
            mSongList = gson.fromJson(songListAsString, type);
            mSongCount = mSongList.size();
        }

    }

    private boolean songListIsOutDated() {
        long currentTime = System.currentTimeMillis();
        long lastSync = DailyPlaySharedPrefUtils.getLastSongListSyncTime();
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
        DailyPlaySharedPrefUtils.setDownloadedSongList(gson.toJson(mDownloadedFiles));
    }

    public void test() {
        try {
            loadSongList();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Collection<Song> songs = getSongList();
        for (Song song : songs) {
            Log.i("DailyPlay", "Test - getSongList - song: " + song.getName() + " - " + song.getArtist());
        }
    }
}

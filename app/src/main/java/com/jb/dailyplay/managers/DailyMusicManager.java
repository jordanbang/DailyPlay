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
import com.jb.dailyplay.utils.DailyPlaySharedPrefUtils;
import com.jb.jblibs.ConnectionUtils;
import com.jb.jblibs.StringUtils;
import com.noveogroup.android.log.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Jordan on 6/3/2014.
 * All functions that make network calls assume that they are being run on a background thread
 */
public class DailyMusicManager {
    public static final int DEF_NUMBER_OF_SONGS = 10;
    public static final int DEF_TIME_OF_PLAY_LIST = 10;

    private static final long ONE_WEEK = DateUtils.WEEK_IN_MILLIS;
    private static final long MEGABYTE = 1024L;

    private static DailyMusicManager mInstance;
    private final GoogleMusicAPI mApi;
    private Collection<Song> mSongList;
    private Collection<SongFile> mDownloadedFiles;

    private int mSongCount = 0;

    public static class DownloadOptions {
        public static final int SONGS = 0;
        public static final int TIME = 1;
    }

    public static DailyMusicManager getInstance() {
        if (mInstance == null) {
            mInstance = new DailyMusicManager();
        }
        return mInstance;
    }

    private DailyMusicManager() {
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

    private Collection<Song> getSongFromPlaylistLength(int timeOfPlayListMin) {
        ArrayList<Song> songsToDownload = new ArrayList<Song>();
        List<Song> songList = (List<Song>) mSongList;
        Collections.shuffle(songList);

        int index = 0;
        while (timeOfPlayListMin > 0) {
            Song songToBeAdded = songList.get(index);
            songsToDownload.add(songToBeAdded);
            timeOfPlayListMin -= (songToBeAdded.getDurationMillis()/(60*1000));
            index++;
        }
        return songsToDownload;
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
            Log.i("Deleting file: " + file.getName());
        }
        DailyPlaySharedPrefUtils.setDownloadedSongList("");
        scanMediaFiles(downloadedFiles, context);
    }

    private void getDownloadedFilesFromSharedPref() {
        String oldDailyPlayList = DailyPlaySharedPrefUtils.getDownloadedSongList();
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

    /*Below calls are synchronous, need to be run on a background thread*/
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
        Log.i("Starting to download songs");
        mDownloadedFiles = mApi.downloadSongs(downloadList, context);
        Log.i("Songs downloaded");
        saveDailyPlayList();
        scanMediaFiles(mDownloadedFiles, context);
    }

    private Collection<Song> getSongList() {
        int downloadOption = DailyPlaySharedPrefUtils.getDownloadOption();
        Collection<Song> songs = null;
        int playListLength = DailyPlaySharedPrefUtils.getLengthOfPlayList();
        switch(downloadOption) {
            case DownloadOptions.SONGS:
                List<Integer> randomNumbers = getRandomNumbers(playListLength);
                songs = getSongForRandomIndices(randomNumbers);
                break;
            case DownloadOptions.TIME:
                songs = getSongFromPlaylistLength(playListLength);
                break;
            case -1:
                throw(new IllegalArgumentException());
        }
        return songs;
    }

    private void loadSongList() throws IOException, URISyntaxException {
        if (mSongList == null || mSongList.size() == 0) {
            Log.i("Downloading song list");
            if (songListIsOutDated()) {
                downloadSongList();
            } else {
                loadSongListFromSharedPref();
            }
            Log.i("Downloaded song list");
        }

    }

    private void downloadSongList() throws IOException, URISyntaxException {
        mSongList = mApi.getAllSongs();
        mSongCount = mSongList.size();
        DailyPlaySharedPrefUtils.setLastSongListSyncTime();
        Gson gson = new Gson();
        DailyPlaySharedPrefUtils.setSongList(gson.toJson(mSongList));
    }

    public void login(String username, String password) {
        try {
            mApi.login(username, password);
        } catch (Exception e) {
            Log.e(e);
        }
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
        songListIsOutDated();
    }
}

package com.jb.dailyplay.managers;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.jb.dailyplay.GooglePlayMusicApi.impl.GoogleMusicAPI;
import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
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
    private int mSongCount = 0;

    public DailyMusicManager() {
        mApi = new GoogleMusicAPI();
    }

    public Collection<Song> getSongList() {
        return mSongList;
    }

    private ArrayList<Integer> getRandomNumbers(int number) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < number; i++) {
            Random rand = new Random();
            ret.add(rand.nextInt(mSongCount + 1));
        }
        return ret;
    }

    private Collection<Song> getSongForRandomIndices(List<Integer> randomNumbers) {
        Collection<Song> songs = new ArrayList<Song>();

        int count = 0;
        for (Song song : mSongList) {
            if (randomNumbers.contains(count)){
                songs.add(song);
            }
            count++;
        }
        return songs;
    }

    private void addFilesToMusicList(Collection<File> downloadFiles, Context context) {
        for (File file : downloadFiles) {
            MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{"audio/mpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                    Log.i("External Storage", "Scanned " + s);
                    Log.i("External Storage", "Uri " + uri);
                }
            });
        }
    }

    /*Below calls need to be asynchronous*/
    public void downloadSongList() {
        try {
            mSongList = mApi.getAllSongs();
            mSongCount = mSongList.size();
        } catch (Exception e) {
            Log.e("Get All Songs error", e.getMessage());
        }
    }

    public void getRandomSongs(int number, Context context) {
        if (mSongList == null || mSongList.size() == 0) {
            downloadSongList();
        }
        List<Integer> randomNumbers = getRandomNumbers(number);
        Collection<Song> downloadList = getSongForRandomIndices(randomNumbers);
        try {
            Collection<File> downloadFiles = mApi.downloadSongs(downloadList, context);
//            Mp3File file = new Mp3File(downloadFiles.iterator().next());
            addFilesToMusicList(downloadFiles, context);
        } catch (Exception e) {
            Log.e("Download files failed", e.getMessage());
        }
    }

    public void login(String username, String password) {
        try {
            mApi.login(username, password);
        } catch (Exception e) {
            Log.e("Login Error", e.getMessage());
        }
    }
}

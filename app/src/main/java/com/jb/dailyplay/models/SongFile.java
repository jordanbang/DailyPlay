package com.jb.dailyplay.models;

import android.net.Uri;

import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.GooglePlayMusicApi.model.Tune;

import java.io.File;

/**
 * Created by Jordan on 6/7/2014.
 * A wrapper to hold both the file + song for downloaded files.
 */
public class SongFile extends Object {
    private File mFile;
    private Song mSong;

    public SongFile(File file, Song song) {
        mFile = file;
        mSong = song;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public File getFile() {
        return mFile;
    }

    public void setSong(Song song) {
        mSong = song;
    }

    public Tune getSong() {
        return mSong;
    }

    public Uri getUri() {
        if (mFile == null) {
            return null;
        }
        return Uri.fromFile(mFile);
    }
}



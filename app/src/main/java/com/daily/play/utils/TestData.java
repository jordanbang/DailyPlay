package com.daily.play.utils;

import com.daily.play.GooglePlayMusicApi.model.Song;
import com.daily.play.models.SongFile;

import java.util.ArrayList;

/**
 * Created by Jordan on 11/1/2014.
 */
public class TestData {

    public static ArrayList<SongFile> getTestDownloadedFiles() {
        ArrayList<SongFile> testDownloadedFiles = new ArrayList<SongFile>();
        for (int i = 0; i < 10; i++) {
            Song song = new Song();
            song.setAlbum("This is an Album");
            song.setArtist("This is an artist");
            song.setTitle("This is song #" + i);
            SongFile songFile = new SongFile(null, song);
            testDownloadedFiles.add(songFile);
        }
        return testDownloadedFiles;
    }
}

package com.daily.play.api.models;

import android.net.Uri;

import java.io.File;

/**
 * Created by Jordan on 6/26/2015.
 */
public class Track {
    private String id;
    private boolean deleted;
    private String title;
    private String artist;
    private String album;
    private String durationMillis;
    private String estimatedSize;
    private File file;

    public String getSongId() {
        return id;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Uri getUri() {
        if (file == null) {
            return null;
        }
        return Uri.fromFile(file);
    }

    public File getFile() {
        return file;
    }

    public Long getDurationMillis() {
        return Long.valueOf(durationMillis);
    }
}

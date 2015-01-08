package com.daily.play.models;

import android.net.Uri;

/**
 * Created by Jordan on 10/29/2014.
 */
public class Song {
    private String artist;
    private String title;
    private String album;
    private Uri uri;

    public Song(String title, String artist, String album, Uri uri) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.uri = uri;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlubm(String album) {
        this.album = album;
    }

    public Uri getUri() {return uri;}

    public void setUri(Uri uri) { this.uri = uri;}
}

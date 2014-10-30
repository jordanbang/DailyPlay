package com.jb.dailyplay.models;

/**
 * Created by Jordan on 10/29/2014.
 */
public class Song {
    private String artist;
    private String title;
    private String album;

    public Song(String title, String artist, String album) {
        this.title = title;
        this.artist = artist;
        this.album = album;
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
}

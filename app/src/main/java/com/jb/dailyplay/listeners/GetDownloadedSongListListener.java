package com.jb.dailyplay.listeners;

import com.jb.dailyplay.models.Song;

import java.util.ArrayList;

/**
 * Created by Jordan on 10/29/2014.
 */
public interface GetDownloadedSongListListener {
    public void onComplete(ArrayList<Song> songs);
}

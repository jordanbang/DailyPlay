package com.daily.play.api;

import android.content.Context;

import com.daily.play.api.models.Track;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jordan on 7/6/2015.
 */
public interface MusicApi {
    public Collection<Track> downloadSongs(Collection<Track> downloadList, Context context);
    public ArrayList<Track> getAllSongs();
    public void login(String token);
    public void login();
    public void logout();
}

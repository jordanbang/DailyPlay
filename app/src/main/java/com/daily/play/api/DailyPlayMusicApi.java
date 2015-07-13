package com.daily.play.api;

import android.content.Context;

import com.daily.play.api.models.Track;
import com.daily.play.utils.DailyPlaySharedPrefUtils;
import com.daily.play.utils.LoginUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jordan on 7/1/2015.
 */
public class DailyPlayMusicApi implements MusicApi{
    Context mContext;

    public DailyPlayMusicApi() {
    }

    /**
     * Syncrhonously downloads the songs in the provided list
     * @param downloadList
     * @param context
     * @return
     */
    public Collection<Track> downloadSongs(Collection<Track> downloadList, Context context) {
        for (Track track : downloadList) {
            if(!GooglePlayMusicApi.downloadSong(track, context, getToken())) {
                downloadList.remove(track);
            }
        }
        return downloadList;
    }

    /**
     * Synchronously gets the list of all tracks on Google Play.
     * @return
     */
    public ArrayList<Track> getAllSongs() {
        return GooglePlayMusicApi.getSongList(getToken());
    }

    public void login() {
        if (LoginUtils.isLoggedIn()) {
            return;
        }
    }

    public void login(String token) {
        DailyPlaySharedPrefUtils.saveToken(token);
        return;
    }

    public void logout() {
        DailyPlaySharedPrefUtils.saveToken("");
    }

    private String getToken() {
        return DailyPlaySharedPrefUtils.getToken();
    }
}

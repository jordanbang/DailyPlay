package com.daily.play.listeners;

import com.daily.play.api.models.Track;

import java.util.Collection;

/**
 * Created by Jordan on 10/29/2014.
 */
public interface GetDownloadedSongListListener {
    public void onComplete(Collection<Track> songs);
}

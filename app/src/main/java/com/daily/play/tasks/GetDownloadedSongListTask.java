package com.daily.play.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.daily.play.api.models.Track;
import com.daily.play.listeners.GetDownloadedSongListListener;
import com.daily.play.managers.DailyPlayMusicManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jordan on 10/29/2014.
 */
public class GetDownloadedSongListTask extends AsyncTask<GetDownloadedSongListListener, Void, Collection<Track>> {
    private GetDownloadedSongListListener mListener;

    @Override
    protected Collection<Track> doInBackground(GetDownloadedSongListListener... params) {
        mListener = params[0];
        Collection<Track> downloadedSongs = DailyPlayMusicManager.getInstance().getDownloadedSongs();
        if (downloadedSongs == null) {
            return new ArrayList<Track>();
        }
        Log.i("DailyPlay", "" + downloadedSongs.size());
        return downloadedSongs;

    }

    @Override
    protected void onPostExecute(Collection<Track> songs) {
        super.onPostExecute(songs);
        Log.i("DailyPlay", "Got the currently downloaded song list");
        mListener.onComplete(songs);
    }
}

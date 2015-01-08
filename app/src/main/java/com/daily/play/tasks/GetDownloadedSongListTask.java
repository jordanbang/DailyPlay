package com.daily.play.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.daily.play.listeners.GetDownloadedSongListListener;
import com.daily.play.managers.DailyPlayMusicManager;
import com.daily.play.models.Song;

import java.util.ArrayList;

/**
 * Created by Jordan on 10/29/2014.
 */
public class GetDownloadedSongListTask extends AsyncTask<GetDownloadedSongListListener, Void, ArrayList<Song>> {
    private GetDownloadedSongListListener mListener;

    @Override
    protected ArrayList<Song> doInBackground(GetDownloadedSongListListener... params) {
        mListener = params[0];
        ArrayList<Song> downloadedSongs = DailyPlayMusicManager.getInstance().getDownloadedSongsAsSongs();
        if (downloadedSongs == null) {
            return new ArrayList<Song>();
        }
        return downloadedSongs;

    }

    @Override
    protected void onPostExecute(ArrayList<Song> songs) {
        super.onPostExecute(songs);
        Log.i("DailyPlay", "Got the currently downloaded song list");
        mListener.onComplete(songs);
    }
}

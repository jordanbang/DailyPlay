package com.daily.play.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.daily.play.api.models.Track;


/**
 * Created by Jordan on 10/31/2014.
 */
public class SongListOnItemClickListener implements AdapterView.OnItemClickListener {
    private Context mContext;

    public SongListOnItemClickListener(Context context) {
        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Track song = (Track) parent.getItemAtPosition(position);
        if (song == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(song.getUri(), "audio/*");
        mContext.startActivity(intent);
}
}

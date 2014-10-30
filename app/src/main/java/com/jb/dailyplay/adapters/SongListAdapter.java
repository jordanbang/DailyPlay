package com.jb.dailyplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jb.dailyplay.R;
import com.jb.dailyplay.models.Song;

import java.util.ArrayList;

/**
 * Created by Jordan on 7/8/2014.
 */
public class SongListAdapter extends ArrayAdapter<Song> {
    private final Context mContext;
    private ArrayList<Song> mSongs;

    static class ViewHolder {
        public TextView titleView;
        public TextView artistView;
        public TextView albumView;
    }

    public SongListAdapter(Context context, ArrayList<Song> songs) {
        super(context, R.layout.list_item_song, songs);
        mContext = context;
        mSongs = songs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.list_item_song, parent, false);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) row.findViewById(R.id.list_item_title);
            viewHolder.artistView = (TextView) row.findViewById(R.id.list_item_artist);
            viewHolder.albumView = (TextView) row.findViewById(R.id.list_item_album);
            row.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) row.getTag();
        Song song = mSongs.get(position);
        holder.titleView.setText(song.getTitle());
        holder.artistView.setText(song.getArtist());
        holder.albumView.setText(song.getAlbum());

        return row;
    }

    public void notifyDataSetChanged(ArrayList<Song> songs) {
        mSongs = songs;
        super.notifyDataSetChanged();
    }
}

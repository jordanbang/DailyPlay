package com.jb.dailyplay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jb.dailyplay.GooglePlayMusicApi.model.Song;
import com.jb.dailyplay.R;
import com.jb.dailyplay.models.SongFile;

import java.util.Collection;

/**
 * Created by Jordan on 7/8/2014.
 */
public class SongListAdapter extends ArrayAdapter<SongFile> {
    private final Context mContext;
    private SongFile[] mSongs;

    static class ViewHolder {
        public TextView titleView;
        public TextView artistView;
        public TextView albumView;
    }

    public SongListAdapter(Context context, Collection<SongFile> songs) {
        super(context, R.layout.list_item_song);
        mContext = context;
        mSongs = songs.toArray(new SongFile[songs.size()]);
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
        Song song = (Song) mSongs[position].getSong();
        holder.titleView.setText(song.getTitle());
        holder.artistView.setText(song.getArtist());
        holder.albumView.setText(song.getAlbum());

        return row;
    }

    public void notifyDataSetChanged(Collection<SongFile> songs) {
        mSongs = songs.toArray(new SongFile[songs.size()]);
        super.notifyDataSetChanged();
    }
}

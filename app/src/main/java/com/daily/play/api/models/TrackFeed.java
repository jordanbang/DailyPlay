package com.daily.play.api.models;

import java.util.ArrayList;

/**
 * Created by Jordan on 6/26/2015.
 */
public class TrackFeed {
    private String kind;
    private Data data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public ArrayList<Track> flatten() {
        ArrayList<Track> tracks = data.getTracks();
        for (Track track : tracks) {
            if (track.isDeleted()) {
                tracks.remove(track);
            }
        }
        return tracks;
    }
}

/*******************************************************************************
 * Copyright (c) 2012 Jens Kristian Villadsen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Jens Kristian Villadsen - initial API and implementation
 ******************************************************************************/
package com.daily.play.GooglePlayMusicApi.interfaces;

import android.content.Context;

import com.daily.play.GooglePlayMusicApi.impl.InvalidCredentialsException;
import com.daily.play.GooglePlayMusicApi.model.AddPlaylist;
import com.daily.play.GooglePlayMusicApi.model.DeletePlaylist;
import com.daily.play.GooglePlayMusicApi.model.Playlist;
import com.daily.play.GooglePlayMusicApi.model.Playlists;
import com.daily.play.GooglePlayMusicApi.model.QueryResponse;
import com.daily.play.GooglePlayMusicApi.model.Song;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * http://readthedocs.org/docs/unofficial-google-music-api/en/latest/
 * https://github.com/simon-weber/Unofficial-Google-Music-API
 *
 * @author JKidd
 */
public interface IGoogleMusicAPI
{
    final String HTTPS_WWW_GOOGLE_COM_ACCOUNTS_CLIENT_LOGIN = "https://www.google.com/accounts/ClientLogin";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_SEARCH = "https://play.google.com/music/services/search";
    // final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADALLTRACKS =
    // "https://play.google.com/music/services/loadalltracks";
    // final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADPLAYLIST =
    // "https://play.google.com/music/services/loadplaylist";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADALLTRACKS = "https://play.google.com/music/services/streamingloadalltracks";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADPLAYLIST = "https://play.google.com/music/services/streamingloadplaylist";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_DELETEPLAYLIST = "https://play.google.com/music/services/deleteplaylist";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_ADDPLAYLIST = "https://play.google.com/music/services/addplaylist";
    final String HTTPS_PLAY_GOOGLE_COM_MUSIC_PLAY_SONGID = "https://play.google.com/music/play?u=0&songid=%1$s&pt=e";

    void login(String email, String password) throws IOException, URISyntaxException, InvalidCredentialsException;

    Collection<Song> getAllSongs() throws IOException, URISyntaxException;

    AddPlaylist addPlaylist(String playlistName) throws Exception;

    Playlists getAllPlaylists() throws IOException, URISyntaxException;

    Playlist getPlaylist(String plID) throws IOException, URISyntaxException;

    URI getSongURL(Song song) throws URISyntaxException, IOException;

    DeletePlaylist deletePlaylist(String id) throws Exception;

    Collection<com.daily.play.models.SongFile> downloadSongs(Collection<Song> songs, Context context) throws MalformedURLException, IOException, URISyntaxException, IllegalArgumentException, InvalidDataException, NotSupportedException, UnsupportedTagException;

    com.daily.play.models.SongFile downloadSong(Song song, Context context) throws MalformedURLException, IOException, URISyntaxException, IllegalArgumentException, InvalidDataException, NotSupportedException, UnsupportedTagException;

    QueryResponse search(String query) throws Exception;

    void uploadSong(File song);
}

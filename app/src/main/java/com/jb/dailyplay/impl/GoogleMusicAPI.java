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
package com.jb.dailyplay.impl;

import android.content.Context;
import android.os.Environment;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jb.dailyplay.comm.FormBuilder;
import com.jb.dailyplay.comm.JSON;
import com.jb.dailyplay.interfaces.IGoogleHttpClient;
import com.jb.dailyplay.interfaces.IGoogleMusicAPI;
import com.jb.dailyplay.interfaces.IJsonDeserializer;
import com.jb.dailyplay.model.AddPlaylist;
import com.jb.dailyplay.model.DeletePlaylist;
import com.jb.dailyplay.model.Playlist;
import com.jb.dailyplay.model.Playlists;
import com.jb.dailyplay.model.QueryResponse;
import com.jb.dailyplay.model.Song;
import com.jb.dailyplay.model.SongUrl;
import com.jb.dailyplay.model.Tune;
import com.jb.dailyplay.comm.HttpUrlConnector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleMusicAPI implements IGoogleMusicAPI {
    public static final String MP3 = ".mp3";
    protected final IGoogleHttpClient client;
    protected final IJsonDeserializer deserializer;
    protected final File storageDirectory;

    public GoogleMusicAPI() {
        this(new HttpUrlConnector(), new JSON(), new File("."));
    }

    public GoogleMusicAPI(final IGoogleHttpClient httpClient, final IJsonDeserializer jsonDeserializer, final File file) {
        client = httpClient;
        deserializer = jsonDeserializer;
        storageDirectory = file;
    }

    @Override
    public final void login(final String email, final String password) throws IOException, URISyntaxException, InvalidCredentialsException {
        final Map<String, String> fields = new HashMap<String, String>();
        fields.put("service", "sj");
        fields.put("Email", email);
        fields.put("Passwd", password);

        final FormBuilder form = new FormBuilder();
        form.addFields(fields);
        form.close();

        try {
            client.dispatchPost(new URI(HTTPS_WWW_GOOGLE_COM_ACCOUNTS_CLIENT_LOGIN), form);
        } catch (final IllegalStateException ise) {
            throw new InvalidCredentialsException(ise, "Provided credentials: '" + email + "' and '" + password + "' where insufficient");
        }
    }

    @Override
    public final Collection<Song> getAllSongs() throws IOException, URISyntaxException {
        final Collection<Song> chunkedCollection = new ArrayList<Song>();

        // Map<String, String> fields = new HashMap<String, String>();
        // fields.put("json", "{\"continuationToken\":\"" + continuationToken +
        // "\"}");

        final FormBuilder form = new FormBuilder();
        // form.addFields(fields);
        form.close();

        final String response = client.dispatchPost(new URI(HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADALLTRACKS), form);

        final List<String> jsSongCollectionWrappers = getJsSongCollectionWrappers(response);

        final Gson gson = new Gson();
        final JsonParser parser = new JsonParser();

        for (final String songCollectionWrapperJson : jsSongCollectionWrappers) {
            final JsonArray songCollectionWrapper = parser.parse(new StringReader(songCollectionWrapperJson))
                    .getAsJsonArray();

            // the song collection is the first element of the "wrapper"
            final JsonArray songCollection = songCollectionWrapper.get(0)
                    .getAsJsonArray();

            // each element of the songCollection is an array of song values
            for (final JsonElement songValues : songCollection) {
                // retrieve the songValues as an Array for parsing to a Song
                // object
                final JsonArray values = songValues.getAsJsonArray();

                final Song s = new Song();
                s.setId(gson.fromJson(values.get(0), String.class));
                s.setTitle(gson.fromJson(values.get(1), String.class));
                s.setName(gson.fromJson(values.get(1), String.class));
                if (!Strings.isNullOrEmpty(gson.fromJson(values.get(2),
                        String.class))) {
                    s.setAlbumArtUrl("https:"
                            + gson.fromJson(values.get(2), String.class));
                }
                s.setArtist(gson.fromJson(values.get(3), String.class));
                s.setAlbum(gson.fromJson(values.get(4), String.class));
                s.setAlbumArtist(gson.fromJson(values.get(5), String.class));
                s.setGenre(gson.fromJson(values.get(11), String.class));
                s.setDurationMillis(gson.fromJson(values.get(13), Long.class));
                s.setType(gson.fromJson(values.get(16), Integer.class));
                s.setYear(gson.fromJson(values.get(18), Integer.class));
                s.setPlaycount(gson.fromJson(values.get(22), Integer.class));
                s.setRating(gson.fromJson(values.get(23), String.class));

                if (!Strings.isNullOrEmpty(gson.fromJson(values.get(24), String.class))) {
                    s.setCreationDate(gson.fromJson(values.get(24), Float.class) / 1000);
                }
                if (!Strings.isNullOrEmpty(gson.fromJson(values.get(36), String.class))) {
                    s.setUrl("https:" + gson.fromJson(values.get(36), String.class));
                }

                chunkedCollection.add(s);
            }
        }

        return chunkedCollection;
    }

    @Override
    public final AddPlaylist addPlaylist(final String playlistName) throws Exception {
        final Map<String, String> fields = new HashMap<String, String>();
        fields.put("json", "{\"title\":\"" + playlistName + "\"}");

        final FormBuilder form = new FormBuilder();
        form.addFields(fields);
        form.close();

        return deserializer.deserialize(client.dispatchPost(new URI(HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_ADDPLAYLIST), form), AddPlaylist.class);
    }

    @Override
    public final Playlists getAllPlaylists() throws IOException,  URISyntaxException {
        return deserializer.deserialize(getPlaylistAssist("{}"), Playlists.class);
    }

    @Override
    public final Playlist getPlaylist(final String plID) throws IOException, URISyntaxException {
        return deserializer.deserialize(getPlaylistAssist("{\"id\":\"" + plID + "\"}"), Playlist.class);
    }

    protected final URI getTuneURL(final Tune tune) throws URISyntaxException, IOException {
        return new URI(deserializer.deserialize(client.dispatchGet(new URI(String.format(HTTPS_PLAY_GOOGLE_COM_MUSIC_PLAY_SONGID, tune.getId()))), SongUrl.class).getUrl());
    }

    @Override
    public URI getSongURL(final Song song) throws URISyntaxException, IOException {
        return getTuneURL(song);
    }

    @Override
    public final DeletePlaylist deletePlaylist(final String id) throws Exception {
        final Map<String, String> fields = new HashMap<String, String>();
        fields.put("json", "{\"id\":\"" + id + "\"}");

        final FormBuilder form = new FormBuilder();
        form.addFields(fields);
        form.close();

        return deserializer.deserialize(client.dispatchPost(new URI(HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_DELETEPLAYLIST), form), DeletePlaylist.class);
    }

    private final String getPlaylistAssist(final String jsonString) throws IOException, URISyntaxException {
        final Map<String, String> fields = new HashMap<String, String>();
        fields.put("json", jsonString);

        final FormBuilder builder = new FormBuilder();
        builder.addFields(fields);
        builder.close();
        return client.dispatchPost(new URI(HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_LOADPLAYLIST), builder);
    }

    /**
     * Locate each "window.parent['slat_process']" statement and extract the
     * JSON representing a song collection. For a provided response, there may
     * be one or more separate song collections.
     *
     * @param response the HTML response from LOADALLTRACKS
     * @return a "wrapper" JSON object with a list of song collection JSON
     * objects as its first element
     */
    private List<String> getJsSongCollectionWrappers(final String response) {
        final List<String> songCollectionList = new ArrayList<String>();

        // locate the contents of: window.parent['slat_process']( );
        // where the song collection JSON is between the parentheses
        final Pattern p = Pattern.compile("window.parent\\['slat_process'\\]\\((.*?)\\);", Pattern.DOTALL);
        final Matcher m = p.matcher(response);
        while (m.find()) {
            final String songCollectionWrapperJson = m.group(1);
            songCollectionList.add(songCollectionWrapperJson);
        }

        return songCollectionList;
    }

    @Override
    public Collection<File> downloadSongs(final Collection<Song> songs, final Context context) throws MalformedURLException, IOException, URISyntaxException {
        final Collection<File> files = new ArrayList<File>();
        for (final Song song : songs) {
            files.add(downloadSong(song, context));
        }
        return files;
    }

    @Override
    public File downloadSong(final Song song, final Context context) throws MalformedURLException, IOException, URISyntaxException {
        return downloadTune(song, context);
    }

    @Override
    public QueryResponse search(final String query) throws IOException, URISyntaxException {
        if (Strings.isNullOrEmpty(query)) {
            throw new IllegalArgumentException("query is null or empty");
        }

        final Map<String, String> fields = new HashMap<String, String>();
        fields.put("json", "{\"q\":\"" + query + "\"}");

        final FormBuilder form = new FormBuilder();
        form.addFields(fields);
        form.close();

        final String response = client.dispatchPost(new URI(HTTPS_PLAY_GOOGLE_COM_MUSIC_SERVICES_SEARCH), form);

        return deserializer.deserialize(response, QueryResponse.class);
    }

    protected File downloadTune(final Tune song, final Context context) throws MalformedURLException, IOException, URISyntaxException {
//        final File file = new File(storageDirectory.getAbsolutePath() + System.getProperty("path.separator") + song.getId() + MP3);
//        final File file = new File(context.getFilesDir(), song.getId() + ".mp3");
        String fileName = song.getTitle() + MP3;
//        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), fileName);
//        if (!file.exists()) {
//            Files.write(Resources.toByteArray(getTuneURL(song).toURL()), file);
//        }
//        return file;
        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" + fileName;
        URL url = getTuneURL(song).toURL();
        URLConnection connection = url.openConnection();
        connection.connect();
        InputStream input = new BufferedInputStream(url.openStream());
        OutputStream output = new FileOutputStream(fileName);
        int count;
        byte data[] = new byte[1024];
        long total = 0;
        while ((count = input.read(data)) != -1) {
            total += count;
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        input.close();

        File file = new File(Environment.getExternalStorageDirectory(), fileName);

        return file;

    }

    @Override
    public void uploadSong(final File song) {

    }
}

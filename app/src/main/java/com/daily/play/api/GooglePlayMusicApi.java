package com.daily.play.api;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import com.daily.play.api.models.Track;
import com.daily.play.api.models.TrackFeed;
import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v1Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Jordan on 7/6/2015.
 */
public class GooglePlayMusicApi {

    public static ArrayList<Track> getSongList(String token) {
        StringBuffer buffer = new StringBuffer()
                .append("https://mclients.googleapis.com/sj/v1.11/trackfeed")
                .append("?")
                .append("alt=json&")
                .append("include-tracks=true&")
                .append("updated-min=0");

        String body = "{\"max-results\": \"20000\"}";

        try {
            URL url = new URL(buffer.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Authorization", "GoogleLogin auth=" + token);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.getOutputStream().write(body.getBytes());

            if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                TrackFeed trackFeed = new Gson().fromJson(sb.toString(), TrackFeed.class);
                return trackFeed.flatten();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean downloadSong(Track song, Context context, String token) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String[] saltsig = getSaltAndSignature(song.getSongId());
        try {
            StringBuffer buffer = new StringBuffer()
                    .append("https://android.clients.google.com/music/mplay")
                    .append("?")
                    .append("opt=hi&")
                    .append("pt=e&")
                    .append("net=mob&")
                    .append("songid=" + song.getSongId() + "&")
                    .append("slt=" + saltsig[0] + "&")
                    .append("sig=" + saltsig[1]);
            URL url = new URL(buffer.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Authorization", "GoogleLogin auth=" + token);
            urlConnection.setRequestProperty("X-Device-ID", androidId.toUpperCase());

            //TODO: change this to use the song's name
            String fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" + song.getTitle() + ".mp3";
            String fullTmpPath = context.getFilesDir().getPath() + "/" + "temp.mp3";
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            OutputStream output = new FileOutputStream(fullTmpPath);

            int count;
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = in.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            in.close();

            Mp3File file = null;
            try {
                file = new Mp3File(fullTmpPath);
                if (!file.hasId3v1Tag()) {
                    ID3v1 tags = new ID3v1Tag();
                    file.setId3v1Tag(tags);
                    tags.setAlbum(song.getAlbum());
                    tags.setArtist(song.getArtist());
                    tags.setTitle(song.getTitle());
                    file.save(fullPath);
                }
            } catch (UnsupportedTagException e) {
                e.printStackTrace();
                return false;
            } catch (NotSupportedException e) {
                e.printStackTrace();
                return false;
            } catch (InvalidDataException e) {
                e.printStackTrace();
                return false;
            }

            Log.i("DailyPlay", "I downloaded a song");
            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String[] getSaltAndSignature(String songId) {
        String[] ret = new String[2];

//        byte[] s1 = Base64.decode("VzeC4H4h+T2f0VI180nVX8x+Mb5HiTtGnKgH52Otj8ZCGDz9jRWyHb6QXK0JskSiOgzQfwTY5xgLLSdUSreaLVMsVVWfxfa8Rw==", Base64.DEFAULT);
//        byte[] s2 = Base64.decode("ZAPnhUkYwQ6y5DdQxWThbvhJHN8msQ1rqJw0ggKdufQjelrKuiGGJI30aswkgCWTDyHkTGK9ynlqTkJ5L4CiGGUabGeo8M6JTQ==", Base64.DEFAULT);
//
//        String key = "";
//        for (int i = 0; i < s1.length; i++) {
//            byte c = (byte) (s1[i] ^ s2[i]);
//            key += Byte.toString(c);
//        }


        try {
            String key = "34ee7983-5ee6-4147-aa86-443ea062abf774493d6a-2a15-43fe-aace-e78566927585\n";

            String salt = Long.toString(System.currentTimeMillis() * 1000);
            String msg = songId + salt;
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HMACSHA1");
            Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(keySpec);
            byte[] sig = mac.doFinal(msg.getBytes("US-ASCII"));

            ret[0] = salt;
            ret[1] = sig == null ? "" : removeEnding(new String(Base64.encode(sig, Base64.URL_SAFE), Charset.forName("US-ASCII")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private static String removeEnding(String in) {
        return in.substring(0, in.length()-2);
    }
}

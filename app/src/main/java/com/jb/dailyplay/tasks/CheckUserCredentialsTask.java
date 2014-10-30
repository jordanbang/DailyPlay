package com.jb.dailyplay.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.jb.dailyplay.listeners.CheckUserCredentialsListener;
import com.jb.dailyplay.managers.DailyPlayMusicManager;
import com.jb.dailyplay.utils.LogUtils;

/**
 * Created by Jordan on 10/12/2014.
 */
public class CheckUserCredentialsTask extends AsyncTask<Object, Void, Boolean> {
    private CheckUserCredentialsListener mListener;

    @Override
    protected Boolean doInBackground(Object... params) {
        mListener = (CheckUserCredentialsListener) params[0];
        String username = (String) params[1];
        String password = (String) params[2];

        boolean ret = true;
        try {
            DailyPlayMusicManager.getInstance().login(username, password);
        } catch(Exception e) {
            LogUtils.appendLog(e);
            Log.e("DailyPlay", e.toString());
            ret = false;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Boolean loginSuccessful) {
        super.onPostExecute(loginSuccessful);
        Log.e("DailyPlay", "Login Task: login was successful = " + loginSuccessful);
        mListener.onComplete(loginSuccessful);
    }
}
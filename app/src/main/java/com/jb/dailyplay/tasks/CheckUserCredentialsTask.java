package com.jb.dailyplay.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.jb.dailyplay.listeners.CheckUserCredentialsListener;
import com.jb.dailyplay.managers.DailyMusicManager;
import com.jb.dailyplay.utils.LogUtils;

/**
 * Created by Jordan on 10/12/2014.
 */
public class CheckUserCredentialsTask extends AsyncTask<CheckUserCredentialsListener, Void, Boolean> {
    private CheckUserCredentialsListener mListener;

    @Override
    protected Boolean doInBackground(CheckUserCredentialsListener... listeners) {
        mListener = listeners[0];
        boolean ret = true;
        try {
            DailyMusicManager.getInstance().login();
        } catch(Exception e) {
            LogUtils.appendLog(e);
            ret = false;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Boolean loginSuccessful) {
        super.onPostExecute(loginSuccessful);
        Log.e("DailyPlay - Login Task", "login was successful = " + loginSuccessful);
        mListener.onComplete(loginSuccessful);
    }
}
package com.jb.dailyplay.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public static void initSharedPref(Context current, String prefName){
        if (settings == null) {
            settings = current.getSharedPreferences(prefName,0);
            editor = settings.edit();
        }
    }

    public static String getString(String VarName, String DefValue) {
        return settings.getString(VarName, DefValue);
    }

    public static void setString(String VarName, String nValue) {
        editor.putString(VarName, nValue);
        editor.commit();
    }

    public static void setInt(String VarName, int val) {
        editor.putInt(VarName, val);
        editor.commit();
    }

    public static void setLong(String Varname, long val) {
        editor.putLong(Varname, val);
        editor.commit();
    }

    public static int getInt(String VarName, int DefValue) {
        return settings.getInt(VarName, DefValue);
    }

    public static long getLong(String VarName, int DefValue) {
        return settings.getLong(VarName, DefValue);
    }

    public static String getString(String VarName) {
        return getString(VarName, "");
    }
}

package com.daily.play.utils;

/**
 * Created by Jordan on 7/6/2015.
 */
public class LoginUtils {

    public static boolean isLoggedIn() {
        return StringUtils.isEmptyString(DailyPlaySharedPrefUtils.getToken());
    }

    public static boolean isNotLoggedIn() {
        return !isLoggedIn();
    }
}

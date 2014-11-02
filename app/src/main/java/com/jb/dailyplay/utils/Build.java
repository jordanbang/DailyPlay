package com.jb.dailyplay.utils;

/**
 * Created by Jordan on 11/1/2014.
 */
public class Build {

    public enum Builds {
        PROD, DEBUG_PHONE, DEBUG_EMULATOR, RELEASE
    }

    private static final Builds sBuild = Builds.DEBUG_EMULATOR;

    public static boolean isEmulatorDebug() {
        return Builds.DEBUG_EMULATOR == sBuild;
    }

    public static boolean isRelease() {
        return Builds.RELEASE == sBuild;
    }

    public static boolean isPhoneDebug() {
        return Builds.DEBUG_PHONE == sBuild;
    }

    public static boolean isDebug() {
        return Builds.DEBUG_EMULATOR == sBuild || Builds.DEBUG_PHONE == sBuild;
    }
}

package com.tcl.statistics.agent;

import android.content.Context;

import com.tcl.statistics.util.PreferenceUtils;

public class StatisticsConfig {
    private static final String CATCH_EXCEPTION = "catch_exception";
    private static final String SESSION_TIME_OUT = "session_time_out";
    private static final String LOCATION_INFO = "location_info";
    private static final String APP_VERSION = "app_version";

    public static boolean isCatchExceptionEnable(Context context) {
        return PreferenceUtils.readBoolean(context, "catch_exception", true);
    }

    public static void setCatchExceptionEnable(Context context, boolean value) {
        PreferenceUtils.saveBoolean(context, "catch_exception", value);
    }

    public static long getSessionTimeOut(Context context) {
        return PreferenceUtils.readLong(context, "session_time_out", 30000L);
    }

    public static void setSessionTimeOut(Context context, long value) {
        PreferenceUtils.saveLong(context, "session_time_out", value);
    }

    public static void saveLocationinfo(Context context, String loc) {
        PreferenceUtils.saveString(context, "location_info", loc);
    }

    public static String getLocationinfo(Context context) {
        return PreferenceUtils.readString(context, "location_info");
    }

    public static void saveAPPVersion(Context context, int appVersion) {
        PreferenceUtils.saveInt(context, "app_version", appVersion);
    }

    public static int getAPPVersion(Context context) {
        return PreferenceUtils.readInt(context, "app_version");
    }
}
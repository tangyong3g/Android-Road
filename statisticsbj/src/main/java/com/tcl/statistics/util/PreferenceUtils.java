package com.tcl.statistics.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {
    private static final String APP_FILE_NAME = "statistics";

    public static boolean saveString(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String readString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getString(key, "");
    }

    public static long readLong(Context context, String key, long defaultvalue) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getLong(key, defaultvalue);
    }

    public static boolean saveInt(Context context, String key, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static boolean saveLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static boolean saveBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean readBoolean(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getBoolean(key, false);
    }

    public static boolean readBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getBoolean(key, value);
    }

    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.contains(key);
    }

    public static int readInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getInt(key, 0);
    }

    public static int readInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences("statistics", 0);
        return sp.getInt(key, defaultValue);
    }

    public static void remove(Context context, String key) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.remove(key);
        editor.commit();
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("statistics", 0).edit();
        editor.clear();
        editor.commit();
    }
}
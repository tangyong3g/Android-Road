package com.tcl.statistics.util;

import android.util.Log;

public class LogUtils {
    public static boolean mDebug = true;
    public static String mLogTag = "statistics";

    public static void V(String msg) {
        if ((mDebug) && (msg != null))
            Log.v(mLogTag, msg);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void V(String msg, Throwable e) {
        if ((mDebug) && (msg != null))
            Log.v(mLogTag, msg, e);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void D(String msg) {
        if ((mDebug) && (msg != null))
            Log.d(mLogTag, msg);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void D(String msg, Throwable e) {
        if ((mDebug) && (msg != null))
            Log.d(mLogTag, msg, e);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void I(String msg) {
        if ((mDebug) && (msg != null))
            Log.i(mLogTag, msg);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void I(String msg, Throwable e) {
        if ((mDebug) && (msg != null))
            Log.i(mLogTag, msg, e);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void W(String msg) {
        if ((mDebug) && (msg != null))
            Log.w(mLogTag, msg);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void W(String msg, Throwable e) {
        if ((mDebug) && (msg != null))
            Log.w(mLogTag, msg, e);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void E(String msg) {
        if ((mDebug) && (msg != null))
            Log.e(mLogTag, msg);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }

    public static void E(String msg, Throwable e) {
        if ((mDebug) && (msg != null))
            Log.e(mLogTag, msg, e);
        else
            Log.e(mLogTag, "LogUtil msg is NULL");
    }
}
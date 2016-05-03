package com.tcl.statistics.agent;

import android.content.Context;
import android.text.TextUtils;

import com.tcl.statistics.bean.EventItem;
import com.tcl.statistics.util.CheckUtils;
import com.tcl.statistics.util.LogUtils;

import java.util.HashMap;
import java.util.Map;

public class StatisticsAgent {
    private static boolean mInitCatchException = false;

    static {
        mInitCatchException = false;
    }

    public static synchronized void onResume(Context context) {
        if (context == null) {
            LogUtils.D("onResume-- context is null");
            return;
        }
        init(context);
        StatisticsHandler.getInstance().sendMessage(0, context);
    }

    public static synchronized void onPause(Context context) {
        if (context == null) {
            LogUtils.D("onPause-- context is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(1, context);
    }

    public static synchronized void onPageStart(String pageName) {
        if (TextUtils.isEmpty(pageName)) {
            LogUtils.D("onPageStart-- pageName is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(5, pageName);
    }

    public static synchronized void onPageEnd(String pageName) {
        if (TextUtils.isEmpty(pageName)) {
            LogUtils.D("onPageEnd-- pageName is null");
            return;
        }
        StatisticsHandler.getInstance().sendMessage(6, pageName);
    }

    public static synchronized void onEvent(Context context, String eventName) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName + " is not legal,only letter,number and underline is valid");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName);
        StatisticsHandler.getInstance().sendMessage(4, event);
    }

    public static synchronized void onEvent(Context context, String eventName, HashMap<String, String> map) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName + " is not legal,only letter,number and underline is valid");

        if (!(CheckUtils.isLegalParamKeyAndValue(map)))
            throw new RuntimeException("error!map is not legal");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName, map);
        StatisticsHandler.getInstance().sendMessage(4, event);
    }

    public static synchronized void onEvent(Context context, String eventName, Map<String, String> map, int value) {
        if (!(CheckUtils.isLegalEventName(eventName)))
            throw new RuntimeException("error!eventName:" + eventName + " is not legal,only letter,number and underline is valid");

        if (!(CheckUtils.isLegalParamKeyAndValue(map)))
            throw new RuntimeException("error!map is not legal");

        EventItem event = new EventItem(System.currentTimeMillis(), eventName, map, value);
        StatisticsHandler.getInstance().sendMessage(4, event);
    }

    public static void onExit(Context context) {
        StatisticsHandler.getInstance().sendMessage(2);
    }

    protected static void onErrorExit(Context context) {
        if (StatisticsConfig.isCatchExceptionEnable(context))
            StatisticsHandler.getInstance().sendMessage(3);
    }

    public static void init(Context context) {
        initExceptionCatcher(context);
    }

    private static void initExceptionCatcher(Context context) {
        synchronized (StatisticsAgent.class) {
            if (!(mInitCatchException)) {
                StatisticsHandler.getInstance().sendMessage(7, context);
                mInitCatchException = true;
            }
        }
    }

    public static void setCatchException(Context context, boolean catchException) {
        StatisticsConfig.setCatchExceptionEnable(context, catchException);
    }

    public static void setSessionTimeOut(Context context, long value) {
        StatisticsConfig.setSessionTimeOut(context, value);
    }

    public static void setDebugMode(boolean debug) {
        LogUtils.mDebug = debug;
    }

    public static void onKillProcess(Context context) {
        StatisticsHandler.getInstance().onKillProcess();
    }

    public static synchronized void sendLog() {
        StatisticsHandler.getInstance().sendMessage(999);
    }
}
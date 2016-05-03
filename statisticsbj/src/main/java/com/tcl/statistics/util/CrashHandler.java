package com.tcl.statistics.util;

import android.content.Context;

import com.tcl.statistics.agent.StatisticsHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 处理错误异常的handler
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    //TAG 标记
    public static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler mInstance = new CrashHandler();
    private Context mContext;


    /**
     * 对外接口得到实例
     *
     * @return
     */
    public static CrashHandler getInstance() {
        return mInstance;
    }


    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        this.mContext = context;

        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    /**
     * 系统异常没有被瞄捕捉
     *
     * @param thread
     * @param ex
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        LogUtils.I("出错了");
        handleException(thread, ex);
    }

    private void handleException(Thread thread, Throwable ex) {
        if (ex == null) {
            return;
        }

        ex.printStackTrace();
        saveCrashInfo(ex);
        StatisticsHandler.getInstance().onErrorExit();

        if (!(this.mDefaultHandler.equals(this)))
            this.mDefaultHandler.uncaughtException(thread, ex);

        throw new RuntimeException(ex);
    }


    private void saveCrashInfo(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        if (cause != null)
            StatisticsHandler.mExcetpionCause = cause.toString();
        else {
            StatisticsHandler.mExcetpionCause = writer.toString().split("\n")[0];
        }

        StatisticsHandler.mExceptionMessage = writer.toString();
        LogUtils.D("错误原因:" + StatisticsHandler.mExcetpionCause + "\n" + "错误消息:" + StatisticsHandler.mExceptionMessage);
    }
}
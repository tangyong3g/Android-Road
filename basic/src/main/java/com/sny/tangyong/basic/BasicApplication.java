package com.sny.tangyong.basic;

import android.app.Application;

import com.tcl.mailfeedback.CrashReport;

/**
 * Created by Administrator on 2016/5/17.
 */
public class BasicApplication extends Application {

    public static final String TAG = "tyler.tang";
    static BasicApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
//        Logger.init(TAG);
        // StatisticsAgent.init(getApplicationContext());
        // StatisticsAgent.setDebugMode(true);
        // 异常启动
        new CrashReport().start(getApplicationContext());
        instance = this;
    }

    public static BasicApplication getInstance() {
        return instance;
    }
}

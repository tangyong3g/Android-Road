package com.sny.tangyong.basic;

import android.app.Application;
import com.orhanobut.logger.Logger;
import com.tcl.statistics.agent.StatisticsAgent;

/**
 * Created by Administrator on 2016/5/17.
 */
public class BasicApplication extends Application{

    public static final String TAG = "tyler.tang";

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(TAG);

        StatisticsAgent.init(getApplicationContext());
        StatisticsAgent.setDebugMode(true);
    }
}

package com.sny.tangyong.demostart.bean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.logging.Logger;

/**
 * Created by Administrator on 2016/3/7.
 */
public class StartBrodcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionStart = intent.getAction();
        if (actionStart == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("Tyler.tang", "Intent.ACTION_BOOT_COMPLETED");
        }
        final Logger ty = Logger.getLogger("Tyler.tang");

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                ty.info("tyler..");
            }
        };

        thread.start();

    }
}

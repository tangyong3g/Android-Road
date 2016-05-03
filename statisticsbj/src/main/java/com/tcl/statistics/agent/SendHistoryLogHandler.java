package com.tcl.statistics.agent;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.tcl.statistics.bean.StatisticsResult;
import com.tcl.statistics.util.FileSerializableUtils;
import com.tcl.statistics.util.LogUtils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 一个单独的线程来处理，日志上传
 */
public class SendHistoryLogHandler {
    private static SendHistoryLogHandler instance = new SendHistoryLogHandler();
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public static SendHistoryLogHandler getInstance() {
        return instance;
    }

    private SendHistoryLogHandler() {
        startThread();
    }

    private void startThread() {
        this.mHandlerThread =
                new HandlerThread("sendHistoryHandler", 10);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
    }

    private void checkThreadAlive() {
        if ((this.mHandlerThread == null) || (!(this.mHandlerThread.isAlive())))
            startThread();
    }

    public void sendHistoryLogs(final Context context) {
        checkThreadAlive();

        this.mHandler.post(new Runnable() {

            @Override
            public void run() {
                HashMap<Serializable, String> statisticsResults = FileSerializableUtils
                        .getInstence().getHistoryLogs(context);

                if (statisticsResults != null && statisticsResults.size() > 0) {
                    for (Map.Entry<Serializable, String> entry : statisticsResults.entrySet()) {
                        StatisticsResult statisticsResult = (StatisticsResult) entry.getKey();
                        LogUtils.D("准备发送历史日志");
                        boolean reportResult = StatisticsHandler.getInstance().reportResult(statisticsResult);
                        LogUtils.I(entry.getValue() + "日志发送结果:" + reportResult);
                        if (reportResult) {
                            File file = new File(entry.getValue());
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                }
            }
        });
    }
}



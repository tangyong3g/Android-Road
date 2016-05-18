package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.view.View;

import com.orhanobut.logger.Logger;

public class UnCatchExceptionActivity extends Activity {

    private static final int OCCURS_PROBLEM = 1;
    private HandlerThread mSubThread = null;
    private Handler mSubHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_catch_exception);
        initComponent();
//        initSubThread();
//        init();
    }

    private void initSubThread() {

        Logger.d("初始化子线程的捕获异常Handler");

        mSubThread = new HandlerThread("subThread", Process.THREAD_PRIORITY_BACKGROUND);
        mSubThread.start();
        mSubHandler = new Handler(mSubThread.getLooper(), new SubHandlerCallback());

        mSubThread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Logger.d("捕获了来自" + thread.getName() + ":\t的异常。");
                throw new RuntimeException();
            }
        });

    }

    private void init() {
        Logger.d("init coming ...");
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Logger.d("主线程的catch捕获");
            }
        });

    }

    private void initComponent() {

        findViewById(R.id.btn_main_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**  在主线程中出现异常*/
                occursProblem();
            }
        });

        findViewById(R.id.btn_sub_error).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** 在子线和中出现异常*/

                if (mSubHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        occursProblem();
                    }
                })) ;

            }
        });
    }

    /**
     * 模拟出现异常
     */
    private void occursProblem() {
        int result = 1 / 0;
    }

    /**
     * 子线程中的Handler.Callback
     */
    class SubHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;

            switch (what) {
                case OCCURS_PROBLEM:
                    occursProblem();
                    break;
                default:
            }
            return false;
        }
    }

}

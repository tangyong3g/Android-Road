package com.sny.tangyong.common;


import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

/**
 * <br> 1: 用来对外提供线程。
 * <br> 2：可以监控当前程序中运行的线程。
 * <br> 3: 可以更有效的停止当前运行的线程。
 * <br> 4: 可以控制线程的数量。
 * <br> 5: 从里面获取新的线程，我不用关注创建线程的概念，也不用关注创建和回收的问题。
 * <br> 6: 可以和UI线程进行交互。
 */
public class ThreadControl {


    private static ThreadControl sInstance;

    private Handler mHandler;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();       // Max pool size
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private BlockingQueue<Runnable> mQueue;
    private ThreadPoolExecutor mExecutor;

    private ThreadControl() {

        mQueue = new LinkedBlockingDeque<Runnable>();
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mQueue);

    }


    public ThreadControl getInstance() {

        synchronized (sInstance) {
            if (sInstance == null) {
                sInstance = new ThreadControl();
            }
        }
        return sInstance;
    }


    private void initThreadControl() {


    }

    public void showCurrentState() {

    }


    public boolean shutDownThread(){

        if(mExecutor == null){
            return false;
        }

        mExecutor.shutdown();
        return true;
    }


    public int getAliveThreads(){

        if(mExecutor == null){
            return 0;
        }

        return mExecutor.getActiveCount();
    }


}

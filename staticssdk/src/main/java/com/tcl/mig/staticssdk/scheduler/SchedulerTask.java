package com.tcl.mig.staticssdk.scheduler;

import android.app.PendingIntent;
//CHECKSTYLE:OFF
import android.content.Context;

/**
 * 
 * <br>类描述:定时任务的基类
 * <br>功能详细描述:
 * 
 * @author  WANGZHUOBIN
 * @date  [2013-3-22]
 */
public abstract class SchedulerTask {
	/**
	 * 任务开始的执行时间
	 */
	private long mStartTime = 0;
	/**
	 * 任务执行的间隔时间，如果小于等于0，则任务只执行一次
	 */
	private long mIntervalTime = 0;
	/**
	 * 任务的唯一标识键值
	 */
	private String mKey;
	/**
	 * 是否已经停止的标识
	 */
	private boolean mIsStop = false;
	/**
	 * 在AlarmManager里面对应的PendingIntent
	 */
	private PendingIntent mPendingIntent;

	public void setStartTime(long startTime) {
		mStartTime = startTime;
	}

	public long getStartTime() {
		return mStartTime;
	}

	public void setIntervalTime(long intervalTime) {
		mIntervalTime = intervalTime;
	}

	public long getIntervalTime() {
		return mIntervalTime;
	}

	public void setKey(Context context, String key) {
		mKey = context.getPackageName() + key;
	}

	public String getKey() {
		return mKey;
	}

	public boolean isStop() {
		return mIsStop;
	}

	public void setIsStop(boolean isStop) {
		this.mIsStop = isStop;
	}

	public PendingIntent getPendingIntent() {
		return mPendingIntent;
	}

	public void setPendingIntent(PendingIntent pendingIntent) {
		this.mPendingIntent = pendingIntent;
	}

	/**
	 * <br>功能简述:销毁的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void destory() {
		this.mPendingIntent = null;
	}

	/**
	 * <br>功能简述:任务执行的方法
	 * <br>功能详细描述:在主线程中被调用，耗时的操作要开启线程
	 * <br>注意:
	 */
	public abstract void execute();

}

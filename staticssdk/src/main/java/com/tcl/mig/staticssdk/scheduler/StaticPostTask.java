package com.tcl.mig.staticssdk.scheduler;

import android.content.Context;

import com.tcl.mig.staticssdk.StatisticsManager;
import com.tcl.mig.staticssdk.utiltool.UtilTool;
//CHECKSTYLE:OFF

public class StaticPostTask extends SchedulerTask {
	private static final String KEY = "funid:";
	private StaticPostCallBack mCallBack;
	private static final long TIME = 1000 * 20;

	public StaticPostTask(Context context, Long intervalTime, String funId, int index) {
		setIntervalTime(intervalTime);
		setStartTime(System.currentTimeMillis() + intervalTime + TIME * index * 1);
		setKey(context, KEY + funId);
		if (UtilTool.isEnableLog()) {
			UtilTool.log(StatisticsManager.TAG, "task construct:" + funId);
		}
	}

	@Override
	public void execute() {
		if (UtilTool.isEnableLog()) {
			UtilTool.log(StatisticsManager.TAG, "task execute:" + getKey() + "interval:"
					+ getIntervalTime());
		}
		if (mCallBack != null) {
			String key = getKey();
			key = key.substring(KEY.length(), key.length());
			String[] keys = key.split(":");
			mCallBack.onFinish(keys[1]);
		}
	}

	public void setCallBack(StaticPostCallBack callBack) {
		this.mCallBack = callBack;
	}

	public interface StaticPostCallBack {
		/**
		 * 
		 * @param key
		 */
		public void onFinish(String key);
	}
}

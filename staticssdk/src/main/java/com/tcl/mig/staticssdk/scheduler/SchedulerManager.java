package com.tcl.mig.staticssdk.scheduler;

import java.util.HashMap;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

//CHECKSTYLE:OFF

/**
 * 
 * <br>
 * 类描述:定时任务管理器 <br>
 * 功能详细描述:
 * 
 * @author WANGZHUOBIN
 * @date [2013-3-22]
 */
public class SchedulerManager {
	private static final String ACTION_SCHEDULER_MANAGER = "com.action.broadreceiver.scheduler.manager";
	private static final String KEY_SCHEDULER_TASK = "scheduler_task_key";
	private static SchedulerManager sInstance;
	private Context mContext;
	private AlarmManager mAlarmManager;
	private BroadcastReceiver mBroadcastReceiver;
	private HashMap<String, SchedulerTask> mHashMap;

	private SchedulerManager(Context context) {
		if (context != null) {
			mContext = context;
			mAlarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			mHashMap = new HashMap<String, SchedulerTask>();
			initBroadcastReceiver(context);
		}
	}

	private void initBroadcastReceiver(Context context) {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ACTION_SCHEDULER_MANAGER.equals(action)) {
					String taskKey = intent.getStringExtra(KEY_SCHEDULER_TASK);
					if (!TextUtils.isEmpty(taskKey) && mHashMap != null) {
						SchedulerTask task = null;
						synchronized (mHashMap) {
							task = mHashMap.get(taskKey);
						}
						if (task == null || task.isStop()) {
							return;
						}
						task.execute();

						long intervalTime = task.getIntervalTime();
						if (intervalTime > 0) {
							task.setStartTime(System.currentTimeMillis()
									+ task.getIntervalTime());
							executeTask(task);
						} else {
							synchronized (mHashMap) {
								mHashMap.remove(taskKey);
								task.setIsStop(true);
								task.destory();
							}
						}
					}
				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_SCHEDULER_MANAGER);
		intentFilter.addDataScheme("download");
		context.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	public synchronized static SchedulerManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new SchedulerManager(context);
		}
		return sInstance;
	}

	/**
	 * <br>
	 * 功能简述:开启定时任务的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param task
	 */
	public void executeTask(SchedulerTask task) {
		if (mContext == null || mAlarmManager == null || task == null
				|| mHashMap == null) {
			return;
		}
		synchronized (mHashMap) {
			if (mHashMap.get(task.getKey()) != null) {
				mHashMap.remove(task.getKey());
			}
			mHashMap.put(task.getKey(), task);
		}
		try {
			Intent intent = new Intent(ACTION_SCHEDULER_MANAGER);
			intent.setData(Uri.parse("download://" + task.getKey()));
			intent.putExtra(KEY_SCHEDULER_TASK, task.getKey());
			intent.setPackage(mContext.getPackageName());
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
					0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			task.setPendingIntent(pendingIntent);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, task.getStartTime(),
					pendingIntent);
		} catch (Exception e) {
		}
	}

	public void stopTask(String taskKey) {
		if (TextUtils.isEmpty(taskKey)) {
			return;
		}
		SchedulerTask task = null;
		if (mHashMap != null) {
			synchronized (mHashMap) {
				task = mHashMap.remove(taskKey);
			}
			if (task != null) {
				task.setIsStop(true);
				PendingIntent pendingIntent = task.getPendingIntent();
				if (pendingIntent != null && mAlarmManager != null) {
					mAlarmManager.cancel(pendingIntent);
				}
				task.destory();
			}
		}
	}

	public void stopTask(SchedulerTask task) {
		if (task != null) {
			stopTask(task.getKey());
		}
	}

	public void cleanup() {
		if (mContext != null && mBroadcastReceiver != null) {
			mContext.unregisterReceiver(mBroadcastReceiver);
		}
		mBroadcastReceiver = null;
		if (mHashMap != null) {
			synchronized (mHashMap) {
				Set<String> keySet = mHashMap.keySet();
				for (String key : keySet) {
					SchedulerTask task = mHashMap.get(key);
					if (task != null) {
						// if (!(task instanceof StaticPostTask)) {
						task.setIsStop(true);
						PendingIntent pendingIntent = task.getPendingIntent();
						if (pendingIntent != null && mAlarmManager != null) {
							mAlarmManager.cancel(pendingIntent);
						}
						task.destory();
						// }
					}
				}
				mHashMap.clear();
			}
		}
		mAlarmManager = null;
		mContext = null;
	}

	public void stopPostDataTask() {
		if (mHashMap != null) {
			synchronized (mHashMap) {
				Set<String> keySet = mHashMap.keySet();
				for (String key : keySet) {
					SchedulerTask task = mHashMap.get(key);
					if (task != null) {
						if (task instanceof StaticPostTask) {
							task.setIsStop(true);
							PendingIntent pendingIntent = task
									.getPendingIntent();
							if (pendingIntent != null && mAlarmManager != null) {
								mAlarmManager.cancel(pendingIntent);
							}
							task.destory();
						}
					}
				}
			}
		}
	}

	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.cleanup();
			sInstance = null;
		}
	}
}

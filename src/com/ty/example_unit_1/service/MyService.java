package com.ty.example_unit_1.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * Cycle of Service
 * 
 * @author tangyong
 * 
 */
public class MyService extends Service {
	
	private static int mCount = 0;

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("cycle","onBind");
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.i("cycle","onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("cycle","onDestroy");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("cycle","onStart");
		super.onStart(intent, startId);
		
		final boolean state = intent.getBooleanExtra("state",false);
		Log.i("tyler.tang","MyService 线程编号:\t"+Thread.currentThread().getId());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("cycle","onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onLowMemory() {
		Log.i("cycle","onLowMemory");
		super.onLowMemory();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i("cycle","onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.i("cycle","onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onRebind(Intent intent) {
		Log.i("cycle","onRebind");
		super.onRebind(intent);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.i("cycle","onTaskRemoved");
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onTrimMemory(int level) {
		Log.i("cycle","onTrimMemory");
		super.onTrimMemory(level);
	}

}
